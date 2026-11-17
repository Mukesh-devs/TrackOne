package com.dev.trackone.feature.expense.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.trackone.core.ui.component.FloatingNavItem
import com.dev.trackone.core.ui.component.TrackOneFloatingNav
import com.dev.trackone.feature.expense.presentation.ExpenseViewModel
import com.dev.trackone.feature.expense.presentation.screen.ExpenseAddScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseBreakdownDetailScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseDetailScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseHomeScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseStatsScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseTransactionsScreen
import com.dev.trackone.feature.expense.sms.presentation.settings.SmsDetectionSettingsCard

object ExpenseRoutes {
    const val MAIN = "expense_main"
    const val HOME = "expense_home"
    const val TRANSACTIONS = "expense_transactions"
    const val ADD = "expense_add"
    const val STATS = "expense_stats"
    const val SETTINGS = "expense_settings"
    const val DETAIL = "expense_detail"
    const val EDIT = "expense_edit"

    /** Transactions behind one slice of the Stats breakdown. */
    const val BREAKDOWN = "expense_breakdown"
}

private data class ExpenseTab(val label: String, val icon: ImageVector, val route: String)

private val expenseTabs = listOf(
    ExpenseTab("Home", Icons.Filled.Home, ExpenseRoutes.HOME),
    ExpenseTab("Transactions", Icons.Filled.Receipt, ExpenseRoutes.TRANSACTIONS),
    ExpenseTab("Add", Icons.Filled.Add, ExpenseRoutes.ADD),
    ExpenseTab("Stats", Icons.Filled.BarChart, ExpenseRoutes.STATS),
    ExpenseTab("Settings", Icons.Filled.Settings, ExpenseRoutes.SETTINGS),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ExpenseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    // Reading the observed uiState fields here keeps the label reactive to
    // period/date changes (getDateLabel() reading _uiState.value would be stale).
    val dateLabel = viewModel.dateLabelFor(uiState.selectedPeriod, uiState.currentDate)

    var currentTab by remember { mutableStateOf(ExpenseRoutes.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Writes are cloud-first, so a failed save/delete means nothing changed
    // anywhere. Surface it rather than letting the action look like it worked.
    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }
    LaunchedEffect(formState.saveError) {
        formState.saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveError()
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ExpenseRoutes.MAIN,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(ExpenseRoutes.MAIN) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(tween(220)) + androidx.compose.animation.slideInHorizontally(tween(220)) { it / 14 }) togetherWith
                                fadeOut(tween(160))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "expense_tabs",
                    ) { tab ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                    when (tab) {
                        ExpenseRoutes.HOME -> ExpenseHomeScreen(
                            uiState = uiState,
                            dateLabel = dateLabel,
                            onPeriodChange = viewModel::setTimePeriod,
                            onNavigateDate = viewModel::navigateDate,
                            onPickDate = viewModel::jumpToDate,
                            onItemClick = { item ->
                                viewModel.selectItem(item)
                                navController.navigate(ExpenseRoutes.DETAIL)
                            },
                            onViewAllClick = { currentTab = ExpenseRoutes.TRANSACTIONS },
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refresh() },
                        )

                        ExpenseRoutes.TRANSACTIONS -> ExpenseTransactionsScreen(
                            uiState = uiState,
                            dateLabel = dateLabel,
                            onPeriodChange = { viewModel.setTimePeriod(it) },
                            onNavigateDate = { viewModel.navigateDate(it) },
                            onPickDate = viewModel::jumpToDate,
                            onItemClick = { item ->
                                viewModel.selectItem(item)
                                navController.navigate(ExpenseRoutes.DETAIL)
                            },
                            onBack = { currentTab = ExpenseRoutes.HOME },
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = { viewModel.refresh() },
                        )

                        ExpenseRoutes.ADD -> Column(modifier = Modifier.fillMaxSize()) {
                            ScreenTitle("Add Transaction")
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

                        ExpenseRoutes.STATS -> ExpenseStatsScreen(
                            uiState = uiState,
                            dateLabel = dateLabel,
                            onPeriodChange = viewModel::setTimePeriod,
                            onNavigateDate = viewModel::navigateDate,
                            onPickDate = viewModel::jumpToDate,
                            getCsvData = viewModel::generateCsvForCurrentPeriod,
                            onSliceClick = { filter ->
                                viewModel.selectSpendingFilter(filter)
                                navController.navigate(ExpenseRoutes.BREAKDOWN)
                            },
                            onMessage = ::showMessage,
                        )

                        ExpenseRoutes.SETTINGS -> Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            ScreenTitle("Settings")
                            SmsDetectionSettingsCard(
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }

                        else -> Unit
                    }
                        }
                    }

                    TrackOneFloatingNav(
                        items = expenseTabs.map {
                            FloatingNavItem(it.label, it.icon, it.route, emphasized = it.route == ExpenseRoutes.ADD)
                        },
                        selectedRoute = currentTab,
                        onSelect = { route ->
                            if (route == ExpenseRoutes.ADD) viewModel.initAddForm()
                            currentTab = route
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            composable(ExpenseRoutes.BREAKDOWN) {
                uiState.spendingFilter?.let { filter ->
                    ExpenseBreakdownDetailScreen(
                        filter = filter,
                        // Already narrowed to the period the chart was showing.
                        items = uiState.expenseItems,
                        periodLabel = dateLabel,
                        periodTotal = uiState.totalExpense,
                        onItemClick = { item ->
                            viewModel.selectItem(item)
                            navController.navigate(ExpenseRoutes.DETAIL)
                        },
                        onBack = {
                            viewModel.clearSpendingFilter()
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable(ExpenseRoutes.DETAIL) {
                uiState.selectedItem?.let { item ->
                    ExpenseDetailScreen(
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
                        },
                    )
                }
            }

            composable(ExpenseRoutes.EDIT) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScreenHeader("Edit Transaction", onBack = { navController.popBackStack() })
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
        }

        // Above the floating nav so messages are never covered by it.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp),
        )
      }
    }
}

// ── Small headers ─────────────────────────────────────────────────────────────

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
    )
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
