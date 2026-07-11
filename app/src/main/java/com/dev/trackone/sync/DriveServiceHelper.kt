package com.dev.trackone.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.api.client.http.ByteArrayContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Helper class for interacting with Google Drive API.
 */
class DriveServiceHelper(private val context: Context, private val account: GoogleSignInAccount) {

    private val driveService: Drive by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        
        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("TrackOne")
        .build()
    }

    /**
     * Uploads the backup string to Google Drive's appDataFolder.
     */
    suspend fun uploadBackup(jsonString: String) = withContext(Dispatchers.IO) {
        val existingFileId = getBackupFileId()

        val fileMetadata = File().apply {
            name = BACKUP_FILE_NAME
            mimeType = "application/json"
            if (existingFileId == null) {
                parents = listOf("appDataFolder")
            }
        }

        val mediaContent = ByteArrayContent.fromString("application/json", jsonString)

        if (existingFileId != null) {
            // Update existing
            driveService.files().update(existingFileId, fileMetadata, mediaContent)
                .execute()
        } else {
            // Create new
            driveService.files().create(fileMetadata, mediaContent)
                .execute()
        }
    }

    /**
     * Downloads the backup from Google Drive's appDataFolder.
     * @return The JSON string, or null if no backup exists.
     */
    suspend fun downloadBackup(): String? = withContext(Dispatchers.IO) {
        val fileId = getBackupFileId() ?: return@withContext null
        
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        
        String(outputStream.toByteArray(), Charsets.UTF_8)
    }

    /**
     * Retrieves the file ID of the backup file if it exists.
     */
    private fun getBackupFileId(): String? {
        val fileList: FileList = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='$BACKUP_FILE_NAME'")
            .setFields("files(id, name, modifiedTime)")
            .execute()
            
        return fileList.files?.firstOrNull()?.id
    }
    
    /**
     * Gets the metadata of the backup file (modified time and size), or null if not found.
     */
    suspend fun getBackupMetadata(): BackupMetadata? = withContext(Dispatchers.IO) {
        val fileList: FileList = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name='$BACKUP_FILE_NAME'")
            .setFields("files(id, name, modifiedTime, size)")
            .execute()
            
        val file = fileList.files?.firstOrNull() ?: return@withContext null
        BackupMetadata(
            modifiedTime = file.modifiedTime?.value,
            sizeBytes = file.getSize()
        )
    }

    companion object {
        const val BACKUP_FILE_NAME = "trackone_backup.json"
    }
}

data class BackupMetadata(
    val modifiedTime: Long?,
    val sizeBytes: Long?
)
