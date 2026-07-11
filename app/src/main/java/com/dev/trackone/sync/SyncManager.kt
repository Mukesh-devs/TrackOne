package com.dev.trackone.sync

import android.content.Context
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
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
     * Gets the last modified time of the backup in milliseconds.
     */
    suspend fun getLastBackupTime(account: GoogleSignInAccount): Long? {
        return try {
            val driveHelper = DriveServiceHelper(context, account)
            driveHelper.getBackupLastModifiedTime()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
