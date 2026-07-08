package com.dev.trackone.feature.expiry

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.expiry.navigation.ExpiryNavigation
import javax.inject.Inject

/**
 * Entry point for the Expiry Tracker module.
 * Plugs into the main app's HorizontalPager via Hilt multibinding.
 */
class ExpiryModuleEntry @Inject constructor() : ModuleEntry {

    override val title: String = "Expiry"
    override val icon: ImageVector = Icons.Default.Timer
    override val order: Int = 0

    @Composable
    override fun Content(onSwipeLeft: () -> Unit, onSwipeRight: () -> Unit) {
        ExpiryNavigation(onSwipeLeft = onSwipeLeft, onSwipeRight = onSwipeRight)
    }
}
