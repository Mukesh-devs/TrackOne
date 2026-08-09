package com.dev.trackone.feature.expense.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.feature.expense.presentation.ExpenseViewModel
import com.dev.trackone.feature.expense.presentation.screen.ExpenseAddScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseDetailScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseHomeScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseStatsScreen
import com.dev.trackone.feature.expense.presentation.screen.ExpenseTransactionsScreen

object ExpenseRoutes {
    const val MAIN = "expense_main"
    const val HOME = "expense_home"
    const val TRANSACTIONS = "expense_transactions"
    const val ADD = "expense_add"
    const val STATS = "expense_stats"
    const val SETTINGS = "expense_settings"
    const val DETAIL = "expense_detail"
    const val EDIT = "expense_edit"
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
    val dateLabel = viewModel.getDateLabel()

    var currentTab by remember { mutableStateOf(ExpenseRoutes.HOME) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavHost(
            navController = navController,
            startDestination = ExpenseRoutes.MAIN,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(ExpenseRoutes.MAIN) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        ExpenseRoutes.HOME -> ExpenseHomeScreen(
                            uiState = uiState,
                            dateLabel = dateLabel,
                            onPeriodChange = viewModel::setTimePeriod,
                            onNavigateDate = viewModel::navigateDate,
                            onSelectMonth = viewModel::selectMonth,
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
                            getCsvData = viewModel::generateCsvForCurrentPeriod,
                        )

                        ExpenseRoutes.SETTINGS -> Column(modifier = Modifier.fillMaxSize()) {
                            ScreenTitle("Settings")
                            TrackOneEmptyState(
                                icon = Icons.Filled.Settings,
                                title = "Settings",
                                subtitle = "Expense module settings coming soon",
                            )
                        }

                        else -> Unit
                    }

                    ExpenseBottomNav(
                        selected = currentTab,
                        onSelect = { route ->
                            if (route == ExpenseRoutes.ADD) viewModel.initAddForm()
                            currentTab = route
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
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
    }
}

// ── Bottom nav (center Add emphasized) ────────────────────────────────────────

@Composable
private fun ExpenseBottomNav(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            expenseTabs.forEach { tab ->
                if (tab.route == ExpenseRoutes.ADD) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onSelect(tab.route) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add transaction",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                } else {
                    val isSelected = tab.route == selected
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable { onSelect(tab.route) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Small headers ─────────────────────────────────────────────────────────────

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
