package com.dev.trackone.feature.expense.sms.presentation.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.R

/**
 * The "Auto-detect bank transactions" switch, off by default.
 *
 * Turning it on always shows the disclosure first and only then the system
 * permission prompt, as Play's prominent-disclosure policy requires.
 */
@Composable
fun SmsDetectionSettingsCard(
    modifier: Modifier = Modifier,
    viewModel: SmsDetectionSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val isEnabled by viewModel.isEnabled.collectAsStateWithLifecycle()
    var showDisclosure by rememberSaveable { mutableStateOf(false) }
    // Android will no longer show the prompt: "don't ask again", or blocked as
    // a restricted setting for an app installed outside Play.
    var isBlocked by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            isBlocked = false
            viewModel.enable()
        } else {
            val activity = context.findActivity()
            isBlocked = activity == null ||
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECEIVE_SMS)
        }
    }

    // Back from system settings: the permission may have changed there.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        if (viewModel.hasPermission()) isBlocked = false
        onPauseOrDispose { }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimens.ElevationTonal,
            shadowElevation = Dimens.ElevationListItem,
        ) {
            Row(
                modifier = Modifier.padding(Dimens.CardPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpacingLg))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sms_detection_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.sms_detection_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(Dimens.SpacingSm))

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        if (checked) showDisclosure = true else viewModel.disable()
                    },
                )
            }
        }

        if (isBlocked && !isEnabled) {
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            Text(
                text = stringResource(R.string.sms_permission_blocked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.SpacingXs),
            )
            if (mayBeRestrictedSetting(context)) {
                Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                Text(
                    text = stringResource(R.string.sms_permission_restricted_help),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Dimens.SpacingXs),
                )
            }
            TextButton(onClick = { context.openAppSettings() }) {
                Text(stringResource(R.string.sms_open_app_settings))
            }
        }
    }

    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            icon = { Icon(Icons.Default.Sms, contentDescription = null) },
            title = { Text(stringResource(R.string.sms_disclosure_title)) },
            text = { Text(stringResource(R.string.sms_disclosure_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDisclosure = false
                    if (viewModel.hasPermission()) {
                        viewModel.enable()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
                    }
                }) { Text(stringResource(R.string.sms_disclosure_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text(stringResource(R.string.sms_disclosure_not_now))
                }
            },
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

/**
 * Android 13+ blocks SMS permissions for apps installed outside an app store
 * until the user allows "restricted settings" in App info.
 */
private fun mayBeRestrictedSetting(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    val installer = try {
        context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
    } catch (e: Exception) {
        null
    }
    return installer != PLAY_STORE_PACKAGE
}

private const val PLAY_STORE_PACKAGE = "com.android.vending"
