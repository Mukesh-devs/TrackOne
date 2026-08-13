package com.dev.trackone.cloud

import android.content.Context
import android.util.Log
import com.dev.trackone.backup.LocalSnapshotSerializer
import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.core.network.auth.AuthRepository
import com.dev.trackone.core.network.auth.AuthSession
import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.core.network.model.ExpenseItemDto
import com.dev.trackone.core.network.model.ExpiryItemDto
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.credential.data.mapper.toDomain as credentialToDomain
import com.dev.trackone.feature.credential.data.mapper.toRequest as credentialToRequest
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expense.data.mapper.toDomain as expenseToDomain
import com.dev.trackone.feature.expense.data.mapper.toRequest as expenseToRequest
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.feature.expiry.data.local.ExpiryEntity
import com.dev.trackone.feature.expiry.data.mapper.toDomain as expiryToDomain
import com.dev.trackone.feature.expiry.data.mapper.toRequest as expiryToRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns everything that has to happen *around* signing in and out, which the
 * network layer deliberately doesn't know about: the local Room cache.
 *
 * Three rules make the cloud-first model safe here:
 *
 * 1. **Signing out clears the cache.** Rows carry no account marker once they
 *    land on the device, so leaving them behind would show one account's data
 *    to the next person who signs in.
 * 2. **Signing in adopts pre-cloud data.** A database that predates the API
 *    would otherwise be wiped by the first `replaceAll` the moment the server
 *    answered with an empty list. So when the account is empty and the device
 *    is not, the local rows are uploaded first — and a JSON snapshot is written
 *    to app storage beforehand either way, as a last resort.
 * 3. **The cache remembers whose it is.** Signing out is not the only way a
 *    session ends: a token that expires or is rejected is dropped where it is,
 *    cache intact. Without rule 3 the next account to sign in would inherit
 *    those rows — and rule 2 would then upload them into that account. So the
 *    owning user id is stored beside the cache and checked on every sign-in.
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

    /**
     * Which account the cached rows belong to, or [NO_OWNER] when nothing has
     * ever claimed them — a pre-cloud database, which rule 2 adopts rather than
     * discards.
     *
     * Plain preferences: it is an account id, not a secret, and it has to
     * outlive the encrypted token store being cleared.
     */
    private val cacheOwnerPrefs = context.getSharedPreferences(OWNER_PREFS, Context.MODE_PRIVATE)

    private var cacheOwnerId: Long
        get() = cacheOwnerPrefs.getLong(KEY_CACHE_OWNER, NO_OWNER)
        set(value) = cacheOwnerPrefs.edit().putLong(KEY_CACHE_OWNER, value).apply()

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
        cacheOwnerId = NO_OWNER
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
            cacheOwnerId = NO_OWNER
            return false
        }
        if (valid) {
            // Backfill for a device that was already signed in before the cache
            // started recording an owner. Claiming it here, while the session is
            // known-good, is what stops the *next* account from inheriting it.
            authRepository.session.value?.userId?.let { cacheOwnerId = it }
        }
        return valid
    }

    private suspend fun onSessionEstablished() {
        val userId = authRepository.session.value?.userId ?: NO_OWNER

        // Rows left behind by a session that lapsed rather than signed out. They
        // belong to someone else; drop them before anything reads them — or,
        // via adoptPreCloudData, uploads them into this account.
        val owner = cacheOwnerId
        if (owner != NO_OWNER && owner != userId) {
            clearLocalCache()
        }
        cacheOwnerId = userId

        refreshCoordinator.invalidateAll()
        runCatching { adoptPreCloudData() }
            .onFailure { Log.w(TAG, "Could not upload pre-existing local data", it) }
        // Again after the upload: the rows created above came back with server
        // ids, and the cache is still holding the local ones.
        refreshCoordinator.invalidateAll()
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
                upload("expiry", row.id) {
                    apiCall<ExpiryItemDto> {
                        api.createExpiryItem(row.expiryToDomain().expiryToRequest())
                    }
                }
            }
        }
        if (expense.isNotEmpty() && fetchAllPages { l, o -> api.getExpenseItems(l, o) }.isEmpty()) {
            expense.forEach { row ->
                upload("expense", row.id) {
                    apiCall<ExpenseItemDto> {
                        api.createExpenseItem(row.expenseToDomain().expenseToRequest())
                    }
                }
            }
        }
        if (credential.isNotEmpty() && fetchAllPages { l, o -> api.getCredentialItems(l, o) }.isEmpty()) {
            credential.forEach { row ->
                upload("credential", row.id) {
                    apiCall<CredentialItemDto> {
                        api.createCredentialItem(row.credentialToDomain().credentialToRequest())
                    }
                }
            }
        }
    }

    /**
     * Uploads one adopted row, logging rather than throwing on failure.
     *
     * One rejected row (a validation error the local database never enforced,
     * say) must not abandon the rest of the adoption half-done — the rows that
     * do go up are the ones the user keeps. Whatever fails stays in the snapshot
     * written just before.
     */
    private suspend fun upload(kind: String, localId: Long, block: suspend () -> Unit) {
        runCatching { block() }
            .onFailure { Log.w(TAG, "Could not adopt $kind row $localId", it) }
    }

    /**
     * Dumps the local database next to the app's files as
     * `pre_cloud_backup.json` so it can be recovered by hand if an upload goes
     * wrong. Written once — a later sign-in must not overwrite the original
     * snapshot.
     */
    private fun writeSafetySnapshot(
        expiry: List<ExpiryEntity>,
        expense: List<ExpenseEntity>,
        credential: List<CredentialEntity>,
    ) {
        val file = File(context.filesDir, SNAPSHOT_FILE)
        if (file.exists()) return
        runCatching { file.writeText(LocalSnapshotSerializer.toJson(expiry, expense, credential)) }
            .onFailure { Log.w(TAG, "Could not write the pre-cloud snapshot", it) }
    }

    private companion object {
        const val TAG = "CloudAccountManager"
        const val SNAPSHOT_FILE = "pre_cloud_backup.json"
        const val OWNER_PREFS = "trackone_cache_owner"
        const val KEY_CACHE_OWNER = "cache_owner_user_id"

        /** No account has claimed the cache: either empty, or pre-cloud data. */
        const val NO_OWNER = 0L
    }
}
