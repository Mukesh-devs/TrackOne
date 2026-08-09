package com.dev.trackone.feature.expense.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseTransactionsScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    onItemClick: (ExpenseItem) -> Unit,
    onBack: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val notImplementedToast = {
        Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    // Group items by day (start of day millis)
    val groupedItems = remember(uiState.items) {
        uiState.items.groupBy { DateUtils.getStartOfDay(it.date) }
            .toSortedMap(compareByDescending { it })
    }

    val cal = Calendar.getInstance()
    val todayStart = DateUtils.getStartOfDay(cal.timeInMillis)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = DateUtils.getStartOfDay(cal.timeInMillis)

    Scaffold(
        bottomBar = {
            // Sticky total at the bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingLg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CurrencyUtils.formatAmount(uiState.totalExpense),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100) // Orange color
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.SpacingXxl),
                modifier = Modifier.fillMaxSize()
            ) {
                // Period Toggle & Date Nav
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        SharedTimePeriodToggle(
                            selectedPeriod = uiState.selectedPeriod,
                            onPeriodSelected = onPeriodChange,
                            modifier = Modifier
                                .padding(horizontal = Dimens.SpacingLg)
                                .padding(top = Dimens.SpacingMd, bottom = 4.dp),
                        )
                        
                        SharedDateNavigationRow(
                            dateLabel = dateLabel,
                            onPrevious = { onNavigateDate(false) },
                            onNext = { onNavigateDate(true) },
                            modifier = Modifier.padding(horizontal = Dimens.SpacingLg).padding(bottom = 8.dp)
                        )
                    }
                }

                if (groupedItems.isEmpty()) {
                    item {
                        TrackOneEmptyState(
                            icon = Icons.Default.FilterList,
                            title = "No transactions",
                            subtitle = "You have no expenses for this period.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.SpacingXxl),
                        )
                    }
                } else {
                    groupedItems.forEach { (dayMillis, itemsForDay) ->
                        // Date Header Sticky
                        stickyHeader(key = "header_$dayMillis") {
                            val dateStr = when (dayMillis) {
                                todayStart -> "Today, ${DateUtils.formatDate(dayMillis)}"
                                yesterdayStart -> "Yesterday, ${DateUtils.formatDate(dayMillis)}"
                                else -> DateUtils.formatDate(dayMillis)
                            }
                            
                            val dayTotal = itemsForDay.filter { !it.isIncome }.sumOf { it.amount }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Total: ${CurrencyUtils.formatAmount(dayTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Items for this day
                        items(
                            items = itemsForDay,
                            key = { it.id }
                        ) { item ->
                            // Custom item layout that mimics the design: icon in rounded box, title, time/cat, amount, chevron
                            ExpenseListItem(
                                item = item,
                                onClick = { onItemClick(item) },
                                modifier = Modifier.padding(horizontal = Dimens.SpacingLg, vertical = 4.dp)
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.padding(bottom = Dimens.SpacingMd))
                        }
                    }
                }
            }
        }
    }
}
