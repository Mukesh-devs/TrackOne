package com.dev.trackone.sync

import android.content.Context
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

/**
 * Orchestrates backup and restore between local Room DB and Google Drive.
 */
class SyncManager(
    private val context: Context,
    private val expiryDao: ExpiryDao,
    private val expenseDao: ExpenseDao,
    private val credentialDao: CredentialDao
) {
    
    /**
     * Reads all local data, serializes to JSON, and uploads to Drive.
     * @return true if successful, false otherwise.
     */
    suspend fun backup(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val expiryItems = expiryDao.getAllOnce()
            val expenseItems = expenseDao.getAllOnce()
            val credentialItems = credentialDao.getAllOnce()
            
            val jsonString = BackupDataSerializer.toJson(expiryItems, expenseItems, credentialItems)
            
            val driveHelper = DriveServiceHelper(context, account)
            driveHelper.uploadBackup(jsonString)
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Downloads JSON from Drive, deserializes, clears local DB, and inserts new data.
     * @return true if successful, false otherwise.
     */
    suspend fun restore(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveHelper = DriveServiceHelper(context, account)
            val jsonString = driveHelper.downloadBackup()
            
            if (jsonString == null) {
                return@withContext Result.failure(Exception("No backup found in cloud."))
            }
            
            val parsedBackup = BackupDataSerializer.fromJson(jsonString)
            
            // Clear local data
            expiryDao.deleteAll()
            expenseDao.deleteAll()
            credentialDao.deleteAll()
            
            // Insert cloud data
            expiryDao.insertAll(parsedBackup.expiryItems)
            expenseDao.insertAll(parsedBackup.expenseItems)
            credentialDao.insertAll(parsedBackup.credentialItems)
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Gets the metadata of the backup.
     */
    suspend fun getBackupMetadata(account: GoogleSignInAccount): BackupMetadata? {
        return try {
            val driveHelper = DriveServiceHelper(context, account)
            driveHelper.getBackupMetadata()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Gets the latest updatedAt timestamp across all 3 modules.
     * Returns 0L if all tables are empty.
     */
    suspend fun getLatestDataEditTime(): Long = withContext(Dispatchers.IO) {
        val expiryLatest = expiryDao.getLatestUpdatedAt() ?: 0L
        val expenseLatest = expenseDao.getLatestUpdatedAt() ?: 0L
        val credentialLatest = credentialDao.getLatestUpdatedAt() ?: 0L
        maxOf(expiryLatest, expenseLatest, credentialLatest)
    }

    /**
     * Observes the latest updatedAt timestamp across all 3 modules in real-time.
     * Emits whenever any table is modified.
     */
    fun observeLatestDataEditTime(): Flow<Long> {
        return combine(
            expiryDao.observeLatestUpdatedAt(),
            expenseDao.observeLatestUpdatedAt(),
            credentialDao.observeLatestUpdatedAt()
        ) { expiry, expense, credential ->
            maxOf(expiry ?: 0L, expense ?: 0L, credential ?: 0L)
        }
    }
}
