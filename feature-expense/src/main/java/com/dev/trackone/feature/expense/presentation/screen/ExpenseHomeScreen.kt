package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod
import com.dev.trackone.feature.expense.sms.presentation.detected.DetectedTransactionsBadgeButton
import java.util.Calendar

private val HeroGradient = Brush.linearGradient(listOf(Color(0xFF039BE5), Color(0xFF01579B)))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHomeScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    onPickDate: (Long) -> Unit,
    onItemClick: (ExpenseItem) -> Unit,
    onViewAllClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    pendingDetectedCount: Int = 0,
    onDetectedTransactionsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val recent = remember(uiState.items) {
        uiState.items.sortedByDescending { it.date }.take(6)
    }
    val periodWord = when (uiState.selectedPeriod) {
        TimePeriod.DAY -> "Today"
        TimePeriod.MONTH -> "This Month"
        TimePeriod.YEAR -> "This Year"
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
        ) {
            item {
                Header(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodChange = onPeriodChange,
                    pendingDetectedCount = pendingDetectedCount,
                    onDetectedTransactionsClick = onDetectedTransactionsClick,
                )
            }

            item {
                HeroCard(
                    totalSpent = uiState.totalExpense,
                    balance = uiState.balance,
                    periodWord = periodWord,
                    dateLabel = dateLabel,
                    period = uiState.selectedPeriod,
                    currentDate = uiState.currentDate,
                    onNavigateDate = onNavigateDate,
                    onPickDate = onPickDate,
                    expenseItems = uiState.expenseItems,
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onViewAllClick() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 2.dp),
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "View all transactions",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp),
                        )
                    }
                }
            }

            if (recent.isEmpty()) {
                item {
                    Text(
                        text = "No transactions in $periodWord. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                    )
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        tonalElevation = 1.dp,
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            recent.forEachIndexed { index, item ->
                                ExpenseListItem(item = item, onClick = { onItemClick(item) })
                                if (index != recent.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun Header(
    selectedPeriod: TimePeriod,
    onPeriodChange: (TimePeriod) -> Unit,
    pendingDetectedCount: Int,
    onDetectedTransactionsClick: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Expense Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Change period",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                TimePeriod.entries.forEach { period ->
                    DropdownMenuItem(
                        text = { Text(period.label) },
                        onClick = {
                            onPeriodChange(period)
                            menuOpen = false
                        },
                    )
                }
            }
        }
        DetectedTransactionsBadgeButton(
            count = pendingDetectedCount,
            onClick = onDetectedTransactionsClick,
        )
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(
    totalSpent: Double,
    balance: Double,
    periodWord: String,
    dateLabel: String,
    period: TimePeriod,
    currentDate: Long,
    onNavigateDate: (Boolean) -> Unit,
    onPickDate: (Long) -> Unit,
    expenseItems: List<ExpenseItem>,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HeroGradient, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    PeriodChip(
                        dateLabel = dateLabel,
                        period = period,
                        currentDate = currentDate,
                        onPickDate = onPickDate,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyUtils.formatAmount(totalSpent),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = periodWord,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )

                Spacer(modifier = Modifier.height(8.dp))
                Sparkline(
                    values = remember(expenseItems) { dailyTotals(expenseItems) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Remaining Balance: ${CurrencyUtils.formatAmount(balance)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

/**
 * Chip showing the current period label; tapping opens a picker in that same
 * granularity. It used to list months whatever the filter said, so picking a
 * date in Day or Year mode was impossible — and choosing one silently dropped
 * you back into Month.
 */
@Composable
private fun PeriodChip(
    dateLabel: String,
    period: TimePeriod,
    currentDate: Long,
    onPickDate: (Long) -> Unit,
) {
    var open by remember { mutableStateOf(false) }

    Surface(
        onClick = { open = true },
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.18f),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = when (period) {
                    TimePeriod.DAY -> "Choose a day"
                    TimePeriod.MONTH -> "Choose a month"
                    TimePeriod.YEAR -> "Choose a year"
                },
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }

    if (open) {
        PeriodPickerDialog(
            period = period,
            currentDate = currentDate,
            onPick = onPickDate,
            onDismiss = { open = false },
        )
    }
}

@Composable
private fun Sparkline(values: List<Float>, modifier: Modifier = Modifier) {
    val line = Color.White
    Canvas(modifier = modifier) {
        if (values.size < 2) {
            // Flat baseline
            drawLine(
                color = line.copy(alpha = 0.6f),
                start = Offset(0f, size.height * 0.7f),
                end = Offset(size.width, size.height * 0.7f),
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
            return@Canvas
        }
        val maxV = values.max().coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1)
        val points = values.mapIndexed { i, v ->
            Offset(i * stepX, size.height - (v / maxV) * size.height * 0.9f - size.height * 0.05f)
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (p in points.drop(1)) lineTo(p.x, p.y)
        }
        drawPath(path, color = line.copy(alpha = 0.9f), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

/** Buckets expense amounts by day (in date order) for the sparkline. */
private fun dailyTotals(items: List<ExpenseItem>): List<Float> {
    if (items.isEmpty()) return emptyList()
    val byDay = sortedMapOf<Int, Float>()
    for (item in items) {
        val cal = Calendar.getInstance().apply { timeInMillis = item.date }
        val key = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
        byDay[key] = (byDay[key] ?: 0f) + item.amount.toFloat()
    }
    return byDay.values.toList()
}
