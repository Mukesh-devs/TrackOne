package com.dev.trackone.ui.settings

import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.ui.cloud.CloudAuthState
import com.dev.trackone.ui.cloud.CloudAuthViewModel
import kotlinx.coroutines.launch

/**
 * Settings — the app's own card idiom rather than a stack of list rows.
 *
 * Three fixed bands: who you are at the top, everything adjustable in the
 * scrolling middle, and the one destructive action pinned to the bottom where
 * it can't be hit by accident on the way to a switch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    cloudViewModel: CloudAuthViewModel = hiltViewModel(),
) {
    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
    val cloudState by cloudViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler { onNavigateBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AccountHeader(
                email = (cloudState as? CloudAuthState.SignedIn)?.email,
                modifier = Modifier.padding(
                    start = Dimens.SpacingMd,
                    end = Dimens.ScreenPadding,
                    top = Dimens.SpacingSm,
                    bottom = Dimens.SpacingSm,
                ),
            )

            // Only the middle band scrolls, so the account stays visible at the
            // top and Sign out stays put at the bottom on any screen height.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding),
            ) {
                Spacer(modifier = Modifier.height(Dimens.SpacingXl))

                SectionLabel("Security")

                AppLockCard(
                    enabled = isAppLockEnabled,
                    onToggle = { isChecked ->
                        if (!isChecked) {
                            viewModel.disableAppLock()
                            return@AppLockCard
                        }
                        val canAuthenticate = BiometricManager.from(context).canAuthenticate(
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.BIOMETRIC_WEAK,
                        )
                        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                            showPinSetupDialog = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No fingerprint or screen lock is set up on this device.",
                                )
                            }
                        }
                    },
                )

                Spacer(modifier = Modifier.height(Dimens.SpacingXl))
            }

            SignOutButton(
                enabled = cloudState is CloudAuthState.SignedIn,
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = Dimens.SpacingXl),
            )
        }
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            onDismiss = { showPinSetupDialog = false },
            onPinSet = { pin ->
                viewModel.enableAppLockWithPin(pin)
                showPinSetupDialog = false
            },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out?") },
            text = {
                Text(
                    "Your data stays safe on the server. The offline copy on this device is " +
                        "cleared and downloaded again next time you sign in.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    cloudViewModel.signOut()
                    showSignOutDialog = false
                }) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// ── Account header ────────────────────────────────────────────────────────────

/**
 * Who you're signed in as: the default profile mark at full size on the left,
 * the account beside it. Deliberately not a card — this is the identity of the
 * screen, not one of its settings, so it sits straight on the background and
 * leaves the surfaces to things you can actually change.
 *
 * No display name exists on the account, so the address itself is the headline.
 */
@Composable
private fun AccountHeader(email: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(84.dp),
        )

        Spacer(modifier = Modifier.width(Dimens.SpacingLg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = email ?: "Not signed in",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXxs))
            Text(
                text = if (email != null) {
                    "Synced to your TrackOne cloud account"
                } else {
                    "Sign in to sync across devices"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ── Middle band ───────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = Dimens.SpacingXs, bottom = Dimens.SpacingMd),
    )
}

@Composable
private fun AppLockCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
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
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.IconSizeMd),
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "App Lock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Require fingerprint or PIN to open TrackOne",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingSm))

            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

// ── Sign out ──────────────────────────────────────────────────────────────────

@Composable
private fun SignOutButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpacingXxl, vertical = Dimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.IconSizeSm),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingMd))
            Text(
                text = "Sign out",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ── PIN setup ─────────────────────────────────────────────────────────────────

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isConfirming) "Confirm PIN" else "Set a PIN",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "Used as a fallback if Fingerprint fails",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                )

                OutlinedTextField(
                    value = if (isConfirming) confirmPin else pin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            if (isConfirming) confirmPin = it else pin = it
                            error = null
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text("4-digit PIN") },
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!isConfirming) {
                                if (pin.length == 4) {
                                    isConfirming = true
                                } else {
                                    error = "PIN must be 4 digits"
                                }
                            } else {
                                if (confirmPin == pin) {
                                    onPinSet(pin)
                                } else {
                                    error = "PINs do not match"
                                    confirmPin = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(if (isConfirming) "Save" else "Next")
                    }
                }
            }
        }
    }
}
