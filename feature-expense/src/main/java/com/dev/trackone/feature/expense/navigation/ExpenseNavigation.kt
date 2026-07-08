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
import kotlinx.coroutines.launch

object ExpenseRoutes {
    const val MAIN_PAGER = "expense_main_pager"
    const val HOME = "expense_home"
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
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { expenseBottomNavItems.size })

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = navBackStackEntry?.destination?.route
    
    val currentRoute = if (currentNavRoute == ExpenseRoutes.MAIN_PAGER || currentNavRoute == null) {
        expenseBottomNavItems[pagerState.currentPage].route
    } else {
        currentNavRoute
    }
    
    val isOnMainScreen = currentNavRoute == ExpenseRoutes.MAIN_PAGER || currentNavRoute == null

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
            } else if (currentNavRoute == ExpenseRoutes.DETAIL) {
                TrackOneTopBar(
                    title = "Transaction Details",
                    showBackButton = true,
                    onBackClick = {
                        viewModel.clearSelectedItem()
                        navController.popBackStack()
                    },
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
                startDestination = ExpenseRoutes.MAIN_PAGER,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
            ) {
                composable(ExpenseRoutes.MAIN_PAGER) {
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (expenseBottomNavItems[page].route) {
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
                                    isRefreshing = uiState.isRefreshing,
                                    onRefresh = { viewModel.refresh() }
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
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
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
                        }
                    }
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

            // Floating bottom bar overlay — transparent behind the pill
            if (isOnMainScreen) {
                TrackOneBottomBar(
                    items = expenseBottomNavItems,
                    selectedRoute = currentRoute,
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    onItemSelected = { item ->
                        val index = expenseBottomNavItems.indexOfFirst { it.route == item.route }
                        if (index >= 0) {
                            if (item.route == ExpenseRoutes.ADD) {
                                viewModel.initAddForm()
                            }
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
    }
}
