package com.dev.trackone.feature.expense.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.SpendingFilter
import com.dev.trackone.feature.expense.presentation.TimePeriod

private data class Slice(
    val label: String,
    val amount: Double,
    val fraction: Float,
    val color: Color,
    /** What this slice narrows to when tapped. */
    val filter: SpendingFilter,
)

/**
 * The two ways the same spend can be sliced. They share one card and one chart
 * so both are a single tap apart — stacking a second chart underneath the first
 * would bury payment modes below the fold.
 */
private enum class Breakdown(val label: String) {
    CATEGORY("Category"),
    PAYMENT_MODE("Payment Mode"),
}

@Composable
fun ExpenseStatsScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    onPickDate: (Long) -> Unit,
    getCsvData: () -> String,
    onSliceClick: (SpendingFilter) -> Unit,
    onMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showInsights by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            val data = getCsvData()
            if (data.isBlank()) {
                onMessage("No transactions to export")
            } else {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray()) }
                }.onSuccess {
                    onMessage("Exported successfully")
                }.onFailure {
                    onMessage("Export failed")
                }
            }
        }
    }

    val byCategory = remember(uiState.expenseItems) { categorySlices(uiState.expenseItems) }
    val byPaymentMode = remember(uiState.expenseItems) { paymentModeSlices(uiState.expenseItems) }
    val totalExpense = uiState.totalExpense

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = { showInsights = true },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Insights",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SharedTimePeriodToggle(selectedPeriod = uiState.selectedPeriod, onPeriodSelected = onPeriodChange)
            }
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                    SharedDateNavigationRow(
                        dateLabel = dateLabel,
                        period = uiState.selectedPeriod,
                        currentDate = uiState.currentDate,
                        onPrevious = { onNavigateDate(false) },
                        onNext = { onNavigateDate(true) },
                        onPickDate = onPickDate,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            item {
                Surface(
                    onClick = { csvLauncher.launch("transactions_${dateLabel.replace(" ", "_")}.csv") },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download CSV", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            item {
                if (byCategory.isEmpty()) {
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No spending in this period", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    SpendingBreakdownCard(
                        byCategory = byCategory,
                        byPaymentMode = byPaymentMode,
                        total = totalExpense,
                        onSliceClick = onSliceClick,
                    )
                }
            }
        }
    }

    if (showInsights) {
        InsightsDialog(uiState = uiState, onDismiss = { showInsights = false })
    }
}

// ── Spending breakdown ────────────────────────────────────────────────────────

/**
 * One card, one chart, two ways to slice it. The toggle sits directly under the
 * title so Category and Payment Mode are equally reachable, and the donut's
 * centre keeps showing the same total either way — which is what makes the two
 * views comparable at a glance rather than two separate charts to scroll past.
 *
 * Each legend row opens the transactions behind that slice.
 */
@Composable
private fun SpendingBreakdownCard(
    byCategory: List<Slice>,
    byPaymentMode: List<Slice>,
    total: Double,
    onSliceClick: (SpendingFilter) -> Unit,
) {
    var breakdown by rememberSaveable { mutableStateOf(Breakdown.CATEGORY) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(14.dp))

            BreakdownToggle(selected = breakdown, onSelect = { breakdown = it })

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = breakdown,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "spending_breakdown",
            ) { mode ->
                val slices = when (mode) {
                    Breakdown.CATEGORY -> byCategory
                    Breakdown.PAYMENT_MODE -> byPaymentMode
                }
                Column {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        DonutChart(slices = slices)
                        // Capped to the ring's inner diameter so a long total
                        // ellipsizes instead of spilling over the arcs.
                        Column(
                            modifier = Modifier.widthIn(max = 128.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyUtils.formatAmount(total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    slices.forEach { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSliceClick(s.filter) }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(s.color))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(s.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("${(s.fraction * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
                            Text(CurrencyUtils.formatAmount(s.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View ${s.label} transactions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownToggle(selected: Breakdown, onSelect: (Breakdown) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            Breakdown.entries.forEach { mode ->
                val isSelected = mode == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(mode) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    )
                }
            }
        }
    }
}

/**
 * Ring rather than a filled pie: it leaves the middle free for the total, and
 * the hairline gap between arcs keeps two similarly-coloured neighbours apart.
 */
@Composable
private fun DonutChart(slices: List<Slice>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(200.dp)) {
        val stroke = 30.dp.toPx()
        val gap = if (slices.size > 1) 1.5f else 0f
        var start = -90f
        slices.forEach { s ->
            val sweep = s.fraction * 360f
            drawArc(
                color = s.color,
                startAngle = start,
                sweepAngle = (sweep - gap).coerceAtLeast(0.6f),
                useCenter = false,
                topLeft = Offset(stroke / 2f, stroke / 2f),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke),
            )
            start += sweep
        }
    }
}

// ── Insights popup (moved from the old total cards) ───────────────────────────

@Composable
private fun InsightsDialog(uiState: ExpenseUiState, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))
                InsightRow("Total Income", uiState.totalIncome, Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(12.dp))
                InsightRow("Total Expense", uiState.totalExpense, MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                InsightRow("Balance", uiState.balance, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InsightRow(label: String, amount: Double, accent: Color) {
    Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(CurrencyUtils.formatAmount(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun categorySlices(items: List<ExpenseItem>): List<Slice> =
    slicesOf(items, { it.category }, ::categoryColor, SpendingFilter::ByCategory)

private fun paymentModeSlices(items: List<ExpenseItem>): List<Slice> =
    slicesOf(items, { it.paymentMode }, ::paymentModeColor, SpendingFilter::ByPaymentMode)

/** Groups spend by [keyOf] and turns each group into a share of the total. */
private fun <K> slicesOf(
    items: List<ExpenseItem>,
    keyOf: (ExpenseItem) -> K,
    colorOf: (K) -> Color,
    filterOf: (K) -> SpendingFilter,
): List<Slice> {
    val totals = items
        .groupBy(keyOf)
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .filter { it.value > 0.0 }
    val total = totals.values.sum()
    if (total <= 0.0) return emptyList()
    return totals.entries
        .sortedByDescending { it.value }
        .map { (key, amount) ->
            val filter = filterOf(key)
            Slice(
                label = filter.label,
                amount = amount,
                fraction = (amount / total).toFloat(),
                color = colorOf(key),
                filter = filter,
            )
        }
}

// Slice colours live in ExpenseChartColors.kt — the drill-down screen shares them.
