package com.dev.trackone.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface that each feature module implements to provide its entry point
 * to the main application's HorizontalPager.
 *
 * Each module contributes itself via Hilt's @IntoSet multibinding.
 * The app collects all ModuleEntry instances and displays them in the pager.
 */
interface ModuleEntry {

    /**
     * Display title for the module (shown in the pager indicator).
     */
    val title: String

    /**
     * Icon representing the module.
     */
    val icon: ImageVector

    /**
     * Sort order for positioning in the pager.
     * Lower values appear first.
     */
    val order: Int

    /**
     * The module's root Composable content.
     * Each module provides its own internal navigation (NavHost + BottomNavigation).
     */
    @Composable
    fun Content()
}
