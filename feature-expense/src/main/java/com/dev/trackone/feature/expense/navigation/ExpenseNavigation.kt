package com.dev.trackone.feature.expense.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.trackone.core.ui.component.BottomNavItem
import com.dev.trackone.core.ui.component.TrackOneBottomBar
import com.dev.trackone.core.ui.component.TrackOneTopBar
import com.dev.trackone.feature.expense.presentation.ExpenseViewModel
import com.dev.trackone.feature.expense.presentation.screen.ExpenseAddScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseHomeScreen

object ExpenseRoutes {
    const val HOME = "expense_home"
    const val ADD = "expense_add"
    const val STATS = "expense_stats"
    const val SETTINGS = "expense_settings"
    const val EDIT = "expense_edit"
}

private val expenseBottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, ExpenseRoutes.HOME),
    BottomNavItem("Add", Icons.Default.Add, ExpenseRoutes.ADD),
    BottomNavItem("Stats", Icons.Default.PieChart, ExpenseRoutes.STATS),
    BottomNavItem("Settings", Icons.Default.Settings, ExpenseRoutes.SETTINGS),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseNavigation() {
    val navController = rememberNavController()
    val viewModel: ExpenseViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ExpenseRoutes.HOME
    
    val isOnMainScreen = currentRoute in listOf(
        ExpenseRoutes.HOME, ExpenseRoutes.ADD, ExpenseRoutes.STATS, ExpenseRoutes.SETTINGS
    )

    com.dev.trackone.core.ui.theme.ExpenseModuleTheme {
    Scaffold(
        topBar = {
            if (isOnMainScreen) {
                TrackOneTopBar(
                    title = when (currentRoute) {
                        ExpenseRoutes.HOME -> "Expense Tracker"
                        ExpenseRoutes.ADD -> "Add Transaction"
                        ExpenseRoutes.STATS -> "Statistics"
                        ExpenseRoutes.SETTINGS -> "Settings"
                        else -> "Expense"
                    },
                    showSearchIcon = currentRoute == ExpenseRoutes.HOME,
                    onSearchClick = { viewModel.toggleSearch() },
                )
            } else {
                TrackOneTopBar(
                    title = "Edit Transaction",
                    showBackButton = true,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = ExpenseRoutes.HOME,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
            ) {
                composable(ExpenseRoutes.HOME) {
                    ExpenseHomeScreen(
                        uiState = uiState,
                        dateLabel = viewModel.getDateLabel(),
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        onPeriodChange = { viewModel.setTimePeriod(it) },
                        onNavigateDate = { viewModel.navigateDate(it) },
                        onEditItem = { item ->
                            viewModel.initEditForm(item)
                            navController.navigate(ExpenseRoutes.EDIT)
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                        },
                    )
                }

                composable(ExpenseRoutes.ADD) {
                    ExpenseAddScreen(
                        formState = formState,
                        onTitleChange = viewModel::updateFormTitle,
                        onAmountChange = viewModel::updateFormAmount,
                        onTypeChange = viewModel::updateFormType,
                        onCategoryChange = viewModel::updateFormCategory,
                        onPaymentModeChange = viewModel::updateFormPaymentMode,
                        onDateChange = viewModel::updateFormDate,
                        onNotesChange = viewModel::updateFormNotes,
                        onSave = viewModel::saveForm,
                        onSaveSuccess = {
                            viewModel.resetSaveSuccess()
                            navController.navigate(ExpenseRoutes.HOME) {
                                popUpTo(ExpenseRoutes.HOME) { inclusive = true }
                            }
                        },
                    )
                }
                composable(ExpenseRoutes.STATS) {
                    com.dev.trackone.feature.expense.presentation.screen.ExpenseStatsScreen(
                        uiState = uiState,
                        dateLabel = viewModel.getDateLabel(),
                        onDateRangeSelected = viewModel::setStatsDateRange,
                        onPeriodChange = viewModel::setTimePeriod,
                        onNavigateDate = viewModel::navigateDate,
                        getCsvData = viewModel::generateCsvData
                    )
                }

                composable(ExpenseRoutes.SETTINGS) {
                    // Placeholder settings screen
                    com.dev.trackone.core.ui.component.TrackOneEmptyState(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Expense module settings coming soon",
                    )
                }

                composable(ExpenseRoutes.EDIT) {
                    ExpenseAddScreen(
                        formState = formState,
                        onTitleChange = viewModel::updateFormTitle,
                        onAmountChange = viewModel::updateFormAmount,
                        onTypeChange = viewModel::updateFormType,
                        onCategoryChange = viewModel::updateFormCategory,
                        onPaymentModeChange = viewModel::updateFormPaymentMode,
                        onDateChange = viewModel::updateFormDate,
                        onNotesChange = viewModel::updateFormNotes,
                        onSave = viewModel::saveForm,
                        onSaveSuccess = {
                            viewModel.resetSaveSuccess()
                            navController.popBackStack()
                        },
                    )
                }
            }

            // Floating bottom bar overlay — transparent behind the pill
            if (isOnMainScreen) {
                TrackOneBottomBar(
                    items = expenseBottomNavItems,
                    selectedRoute = currentRoute,
                    onItemSelected = { item ->
                        if (item.route != currentRoute) {
                            if (item.route == ExpenseRoutes.ADD) {
                                viewModel.initAddForm()
                            }
                            navController.navigate(item.route) {
                                popUpTo(ExpenseRoutes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
    }
}
