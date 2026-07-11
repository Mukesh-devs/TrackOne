package com.dev.trackone.ui.lock

import androidx.compose.runtime.*

@Composable
fun AuthGate(
    isAppLockEnabled: Boolean,
    hashedPin: String?,
    isAuthenticated: Boolean,
    onUnlocked: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isAppLockEnabled && !isAuthenticated && hashedPin != null) {
        LockScreen(
            hashedPin = hashedPin,
            onUnlocked = onUnlocked
        )
    } else {
        content()
    }
}
