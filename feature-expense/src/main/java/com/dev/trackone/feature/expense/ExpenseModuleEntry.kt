package com.dev.trackone.feature.expense

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.expense.navigation.ExpenseNavigation
import javax.inject.Inject

/**
 * Entry point for the Expense Tracker module.
 */
class ExpenseModuleEntry @Inject constructor() : ModuleEntry {

    override val title: String = "Expense"
    override val icon: ImageVector = Icons.Default.AccountBalanceWallet
    override val order: Int = 1

    @androidx.compose.material3.ExperimentalMaterial3Api
    @Composable
    override fun Content(onSwipeLeft: () -> Unit, onSwipeRight: () -> Unit) {
        ExpenseNavigation(onSwipeLeft = onSwipeLeft, onSwipeRight = onSwipeRight)
    }
}
