package com.dev.trackone.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.ui.sync.SyncUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    syncUiState: SyncUiState,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler {
        onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Last Backup Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color.DarkGray)
                    Text("Last Backup", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.titleMedium)
                }

                val signedInState = when (syncUiState) {
                    is SyncUiState.SignedIn -> syncUiState
                    is SyncUiState.Success -> syncUiState.signedInState
                    is SyncUiState.Error -> syncUiState.signedInState
                    else -> null
                }
                
                if (signedInState != null) {
                    Text(
                        text = "Google Drive: ${signedInState.lastBackupTime?.let { formatTime(it) } ?: "Never"}",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Size: ${formatSize(signedInState.backupSizeBytes)}",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (syncUiState is SyncUiState.Loading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                        Text("Syncing...", color = Color.DarkGray)
                    }
                } else {
                    Text("Sign in to view backup details.", color = Color.DarkGray)
                }
            }

            // Buttons
            Button(
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                enabled = syncUiState is SyncUiState.SignedIn || syncUiState is SyncUiState.Success || syncUiState is SyncUiState.Error
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back Up Now", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            
            Button(
                onClick = onRestoreClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                enabled = syncUiState is SyncUiState.SignedIn || syncUiState is SyncUiState.Success || syncUiState is SyncUiState.Error
            ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Google Account Settings
            Text("Google Drive settings", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Google Account", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    val email = when (syncUiState) {
                        is SyncUiState.SignedIn -> syncUiState.accountEmail
                        is SyncUiState.Success -> syncUiState.signedInState?.accountEmail ?: "Not selected"
                        is SyncUiState.Error -> syncUiState.signedInState?.accountEmail ?: "Not selected"
                        else -> "Not selected"
                    }
                    Text(email, color = Color.DarkGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            if (syncUiState is SyncUiState.Success) {
                Text(syncUiState.message, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
            } else if (syncUiState is SyncUiState.Error) {
                Text(syncUiState.message, color = Color.Red, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun formatSize(bytes: Long?): String {
    if (bytes == null || bytes == 0L) return "Unknown"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
        kb >= 1.0 -> String.format(Locale.getDefault(), "%.1f KB", kb)
        else -> "$bytes B"
    }
}
