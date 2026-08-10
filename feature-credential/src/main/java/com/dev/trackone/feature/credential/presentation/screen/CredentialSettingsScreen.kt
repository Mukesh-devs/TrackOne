package com.dev.trackone.feature.credential.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneScreenHeader
import com.dev.trackone.core.ui.component.TrackOneSectionCard
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.core.ui.theme.WarningAmber

/**
 * Credential module settings.
 *
 * The CSV write lives here rather than in the ViewModel: `Uri`/`Context` are UI
 * concerns, so the ViewModel only supplies the payload via [csvProvider] and the
 * outcome is surfaced through [onMessage] (a Snackbar, not a Toast).
 */
@Composable
fun CredentialSettingsScreen(
    csvProvider: () -> String,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val csv = csvProvider()
        if (csv.isBlank()) {
            onMessage("No credentials to export")
            return@rememberLauncherForActivityResult
        }
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
        }.onSuccess {
            onMessage("Exported successfully")
        }.onFailure {
            onMessage("Export failed: ${it.message ?: "unknown error"}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TrackOneScreenHeader(title = "Settings", icon = Icons.Default.Settings)

        Column(
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        ) {
            TrackOneSectionCard(title = "Data & export", icon = Icons.Default.Download) {
                Text(
                    text = "Export all saved credentials as a CSV file.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingLg))
                Button(
                    onClick = { exportLauncher.launch("credentials_export.csv") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    Text("Export CSV")
                }
            }

            // The export is plaintext — say so plainly rather than letting the
            // user discover it after the fact.
            Surface(
                shape = MaterialTheme.shapes.large,
                color = WarningAmber.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.CardPadding),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingMd))
                    Column {
                        Text(
                            text = "Exports are unencrypted",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "The CSV contains your passwords in plain text. " +
                                "Store it somewhere safe and delete it when you're done.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}
