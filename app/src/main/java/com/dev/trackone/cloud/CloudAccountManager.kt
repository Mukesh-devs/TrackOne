package com.dev.trackone.cloud

import android.content.Context
import android.util.Log
import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.core.network.auth.AuthRepository
import com.dev.trackone.core.network.auth.AuthSession
import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.core.network.model.ExpenseItemDto
import com.dev.trackone.core.network.model.ExpiryItemDto
import com.dev.trackone.core.network.model.RestoreResultDto
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.mapper.toDomain as credentialToDomain
import com.dev.trackone.feature.credential.data.mapper.toRequest as credentialToRequest
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expense.data.mapper.toDomain as expenseToDomain
import com.dev.trackone.feature.expense.data.mapper.toRequest as expenseToRequest
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.feature.expiry.data.mapper.toDomain as expiryToDomain
import com.dev.trackone.feature.expiry.data.mapper.toRequest as expiryToRequest
import com.dev.trackone.sync.BackupDataSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns everything that has to happen *around* signing in and out, which the
 * network layer deliberately doesn't know about: the local Room cache.
 *
 * Two rules make the cloud-first model safe here:
 *
 * 1. **Signing out clears the cache.** Rows carry no account marker once they
 *    land on the device, so leaving them behind would show one account's data
 *    to the next person who signs in.
 * 2. **Signing in adopts pre-cloud data.** A database that predates the API
 *    would otherwise be wiped by the first `replaceAll` the moment the server
 *    answered with an empty list. So when the account is empty and the device
 *    is not, the local rows are uploaded first — and a JSON snapshot is written
 *    to app storage beforehand either way, as a last resort.
 */
@Singleton
class CloudAccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val api: TrackOneApi,
    private val refreshCoordinator: RemoteRefreshCoordinator,
    private val expiryDao: ExpiryDao,
    private val expenseDao: ExpenseDao,
    private val credentialDao: CredentialDao,
) {

    val session: StateFlow<AuthSession?> = authRepository.session

    suspend fun signIn(email: String, password: String) {
        authRepository.signIn(email, password)
        onSessionEstablished()
    }

    suspend fun signUp(email: String, password: String) {
        authRepository.signUp(email, password)
        onSessionEstablished()
    }

    suspend fun signOut() {
        authRepository.clearSession()
        clearLocalCache()
        refreshCoordinator.invalidateAll()
    }

    /**
     * Verifies a restored token on app start. Returns false when the server says
     * the token is no longer good, in which case the local cache is dropped too.
     */
    suspend fun verifyRestoredSession(): Boolean {
        if (!authRepository.isSignedIn) return false
        val valid = authRepository.verifySession()
        if (!valid && !authRepository.isSignedIn) {
            // The interceptor already cleared the token on the 401.
            clearLocalCache()
        }
        return valid
    }

    /**
     * Uploads the current local database to the account, replacing whatever is
     * there.
     *
     * Needed after a Google Drive restore: that path writes straight to Room,
     * and with the API as the source of truth the next background refresh would
     * otherwise wipe the restored rows within seconds. Pushing them up first
     * makes the restore stick.
     */
    suspend fun pushLocalDatabaseToCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = BackupDataSerializer.toJson(
                expiryDao.getAllOnce(),
                expenseDao.getAllOnce(),
                credentialDao.getAllOnce(),
            )
            apiCall<RestoreResultDto> {
                api.restoreBackup(
                    mode = "replace",
                    payload = json.toRequestBody("application/json; charset=utf-8".toMediaType()),
                )
            }
            refreshCoordinator.invalidateAll()
        }
    }

    private suspend fun onSessionEstablished() {
        refreshCoordinator.invalidateAll()
        runCatching { adoptPreCloudData() }
            .onFailure { Log.w(TAG, "Could not upload pre-existing local data", it) }
    }

    private suspend fun clearLocalCache() = withContext(Dispatchers.IO) {
        expiryDao.deleteAll()
        expenseDao.deleteAll()
        credentialDao.deleteAll()
    }

    private suspend fun adoptPreCloudData() = withContext(Dispatchers.IO) {
        val expiry = expiryDao.getAllOnce()
        val expense = expenseDao.getAllOnce()
        val credential = credentialDao.getAllOnce()
        if (expiry.isEmpty() && expense.isEmpty() && credential.isEmpty()) return@withContext

        writeSafetySnapshot(expiry, expense, credential)

        // Only adopt into an *empty* account. A second device signing into an
        // account that already has rows must not duplicate them; it just syncs.
        // Explicit type arguments: inside a Unit-returning forEach, Kotlin would
        // otherwise try to infer apiCall's T as Unit.
        if (expiry.isNotEmpty() && fetchAllPages { l, o -> api.getExpiryItems(l, o) }.isEmpty()) {
            expiry.forEach { row ->
                apiCall<ExpiryItemDto> {
                    api.createExpiryItem(row.expiryToDomain().expiryToRequest())
                }
            }
        }
        if (expense.isNotEmpty() && fetchAllPages { l, o -> api.getExpenseItems(l, o) }.isEmpty()) {
            expense.forEach { row ->
                apiCall<ExpenseItemDto> {
                    api.createExpenseItem(row.expenseToDomain().expenseToRequest())
                }
            }
        }
        if (credential.isNotEmpty() && fetchAllPages { l, o -> api.getCredentialItems(l, o) }.isEmpty()) {
            credential.forEach { row ->
                apiCall<CredentialItemDto> {
                    api.createCredentialItem(row.credentialToDomain().credentialToRequest())
                }
            }
        }
    }

    /**
     * Dumps the local database next to the app's files as
     * `pre_cloud_backup.json`, in the same shape the Drive backup already uses,
     * so it can be restored through the existing path if an upload goes wrong.
     * Written once — a later sign-in must not overwrite the original snapshot.
     */
    private fun writeSafetySnapshot(
        expiry: List<com.dev.trackone.feature.expiry.data.local.ExpiryEntity>,
        expense: List<com.dev.trackone.feature.expense.data.local.ExpenseEntity>,
        credential: List<com.dev.trackone.feature.credential.data.local.CredentialEntity>,
    ) {
        val file = File(context.filesDir, SNAPSHOT_FILE)
        if (file.exists()) return
        runCatching { file.writeText(BackupDataSerializer.toJson(expiry, expense, credential)) }
            .onFailure { Log.w(TAG, "Could not write the pre-cloud snapshot", it) }
    }

    private companion object {
        const val TAG = "CloudAccountManager"
        const val SNAPSHOT_FILE = "pre_cloud_backup.json"
    }
}
