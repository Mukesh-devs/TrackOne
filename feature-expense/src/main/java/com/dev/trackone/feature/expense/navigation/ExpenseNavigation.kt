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
import androidx.navigation.NavHostController
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
    const val MAIN_PAGER = "expense_main_pager"
    const val HOME = "expense_home"
    const val TRANSACTIONS = "expense_transactions"
    const val ADD = "expense_add"
    const val STATS = "expense_stats"
    const val SETTINGS = "expense_settings"
    const val EDIT = "expense_edit"
    const val DETAIL = "expense_detail"
}

private val expenseBottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, ExpenseRoutes.HOME),
    BottomNavItem("Add", Icons.Default.Add, ExpenseRoutes.ADD),
    BottomNavItem("Stats", Icons.Default.PieChart, ExpenseRoutes.STATS),
    BottomNavItem("Settings", Icons.Default.Settings, ExpenseRoutes.SETTINGS),
)

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ExpenseNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(ExpenseRoutes.HOME) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = navBackStackEntry?.destination?.route

    val currentRoute = if (currentNavRoute == ExpenseRoutes.MAIN_PAGER || currentNavRoute == null) {
        currentTab
    } else {
        currentNavRoute
    }
    
    val isOnMainScreen = currentNavRoute == ExpenseRoutes.MAIN_PAGER || currentNavRoute == null

    // Bars scroll-aware visibility
    var isBarsVisible by remember { mutableStateOf(true) }

    com.dev.trackone.core.ui.theme.ExpenseModuleTheme {
    Scaffold(
        topBar = {
            if (isOnMainScreen) {
                if (currentRoute != ExpenseRoutes.HOME) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isBarsVisible,
                        enter = androidx.compose.animation.expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = tween(250),
                        ) + fadeIn(tween(200)),
                        exit = androidx.compose.animation.shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = tween(250),
                        ) + fadeOut(tween(150)),
                    ) {
                        TrackOneTopBar(
                            title = when (currentRoute) {
                                ExpenseRoutes.ADD -> "Add Transaction"
                                ExpenseRoutes.STATS -> "Statistics"
                                ExpenseRoutes.SETTINGS -> "Settings"
                                else -> "Expense"
                            },
                        )
                    }
                }
            } else if (currentNavRoute == ExpenseRoutes.TRANSACTIONS) {
                // Transactions screen has its own custom top bar in the new design,
                // so we don't render TrackOneTopBar here for it.
            } else if (currentNavRoute == ExpenseRoutes.DETAIL) {
                TrackOneTopBar(
                    title = "Transaction Details",
                    showBackButton = true,
                    onBackClick = {
                        viewModel.clearSelectedItem()
                        navController.popBackStack()
                    },
                )
            } else if (currentNavRoute == ExpenseRoutes.EDIT) {
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
                startDestination = ExpenseRoutes.MAIN_PAGER,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { 
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(tween(300))
                },
                exitTransition = { 
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeOut(tween(300))
                },
                popEnterTransition = {
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { -it / 3 },
                        animationSpec = tween(300)
                    ) + fadeIn(tween(300))
                },
                popExitTransition = {
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut(tween(300))
                },
            ) {
                composable(ExpenseRoutes.MAIN_PAGER) {
                    when (currentTab) {
                            ExpenseRoutes.HOME -> {
                                ExpenseHomeScreen(
                                    uiState = uiState,
                                    dateLabel = viewModel.getDateLabel(),
                                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onPeriodChange = { viewModel.setTimePeriod(it) },
                                    onNavigateDate = { viewModel.navigateDate(it) },
                                    onItemClick = { item ->
                                        viewModel.selectItem(item)
                                        navController.navigate(ExpenseRoutes.DETAIL)
                                    },
                                    onViewAllClick = {
                                        navController.navigate(ExpenseRoutes.TRANSACTIONS)
                                    },
                                    onMenuClick = { },
                                    isRefreshing = uiState.isRefreshing,
                                    onRefresh = { viewModel.refresh() },
                                    onScrollingDown = { scrollingDown ->
                                        isBarsVisible = !scrollingDown
                                    },
                                )
                            }
                            ExpenseRoutes.ADD -> {
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
                                        currentTab = ExpenseRoutes.HOME
                                    },
                                )
                            }
                            ExpenseRoutes.STATS -> {
                                com.dev.trackone.feature.expense.presentation.screen.ExpenseStatsScreen(
                                    uiState = uiState,
                                    dateLabel = viewModel.getDateLabel(),
                                    onDateRangeSelected = viewModel::setStatsDateRange,
                                    onPeriodChange = viewModel::setTimePeriod,
                                    onNavigateDate = viewModel::navigateDate,
                                    getCsvData = viewModel::generateCsvData
                                )
                            }
                            ExpenseRoutes.SETTINGS -> {
                                com.dev.trackone.core.ui.component.TrackOneEmptyState(
                                    icon = Icons.Default.Settings,
                                    title = "Settings",
                                    subtitle = "Expense module settings coming soon",
                                )
                            }
                            else -> Unit
                        }
                }

                composable(ExpenseRoutes.TRANSACTIONS) {
                    com.dev.trackone.feature.expense.presentation.screen.ExpenseTransactionsScreen(
                        uiState = uiState,
                        dateLabel = viewModel.getDateLabel(),
                        onPeriodChange = { viewModel.setTimePeriod(it) },
                        onNavigateDate = { viewModel.navigateDate(it) },
                        onItemClick = { item ->
                            viewModel.selectItem(item)
                            navController.navigate(ExpenseRoutes.DETAIL)
                        },
                        onBack = { navController.popBackStack() },
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() }
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

                composable(ExpenseRoutes.DETAIL) {
                    uiState.selectedItem?.let { item ->
                        com.dev.trackone.feature.expense.presentation.screen.ExpenseDetailScreen(
                            item = item,
                            onEdit = {
                                viewModel.initEditForm(item)
                                navController.navigate(ExpenseRoutes.EDIT)
                            },
                            onDelete = {
                                viewModel.deleteItem(item)
                                viewModel.clearSelectedItem()
                                navController.popBackStack()
                            },
                            onBack = {
                                viewModel.clearSelectedItem()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }

            // Animated bottom bar — slides down to hide on scroll
            if (isOnMainScreen) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isBarsVisible,
                    enter = androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(250),
                    ) + fadeIn(tween(200)),
                    exit = androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250),
                    ) + fadeOut(tween(150)),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    TrackOneBottomBar(
                        items = expenseBottomNavItems,
                        selectedRoute = currentRoute,
                        onItemSelected = { item ->
                            if (item.route == ExpenseRoutes.ADD) {
                                viewModel.initAddForm()
                            }
                            currentTab = item.route
                        },
                    )
                }
            }
        }
    }
    }
}
