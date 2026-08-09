package com.dev.trackone.feature.expense.presentation.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod

private data class Slice(val label: String, val amount: Double, val fraction: Float, val color: Color)

@Composable
fun ExpenseStatsScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    getCsvData: () -> String,
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
                Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
            } else {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray()) }
                }.onSuccess {
                    Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val slices = remember(uiState.expenseItems) { buildSlices(uiState) }
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
                        onPrevious = { onNavigateDate(false) },
                        onNext = { onNavigateDate(true) },
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
                if (slices.isEmpty()) {
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
                    PieChartCard(slices = slices, total = totalExpense)
                }
            }
        }
    }

    if (showInsights) {
        InsightsDialog(uiState = uiState, onDismiss = { showInsights = false })
    }
}

// ── Pie chart ─────────────────────────────────────────────────────────────────

@Composable
private fun PieChartCard(slices: List<Slice>, total: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    var start = -90f
                    slices.forEach { s ->
                        val sweep = s.fraction * 360f
                        drawArc(color = s.color, startAngle = start, sweepAngle = sweep, useCenter = true)
                        start += sweep
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            slices.forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(s.color))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(s.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text("${(s.fraction * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
                    Text(CurrencyUtils.formatAmount(s.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
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

private fun buildSlices(uiState: ExpenseUiState): List<Slice> {
    val byCat = uiState.expenseItems
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .filter { it.value > 0.0 }
    val total = byCat.values.sum()
    if (total <= 0.0) return emptyList()
    return byCat.entries
        .sortedByDescending { it.value }
        .map { Slice(it.key.displayName, it.value, (it.value / total).toFloat(), categoryColor(it.key)) }
}

private fun categoryColor(c: ExpenseCategory): Color = when (c) {
    ExpenseCategory.FOOD -> Color(0xFFFFB300)
    ExpenseCategory.TRANSPORTATION -> Color(0xFF1E88E5)
    ExpenseCategory.HOUSING -> Color(0xFF43A047)
    ExpenseCategory.UTILITIES -> Color(0xFF8E24AA)
    ExpenseCategory.HEALTHCARE -> Color(0xFFE53935)
    ExpenseCategory.SHOPPING -> Color(0xFF00ACC1)
    ExpenseCategory.ENTERTAINMENT -> Color(0xFFE91E63)
    ExpenseCategory.EDUCATION -> Color(0xFF3949AB)
    ExpenseCategory.TRAVEL -> Color(0xFF00897B)
    ExpenseCategory.PERSONAL -> Color(0xFF6D4C41)
    ExpenseCategory.INSURANCE -> Color(0xFF546E7A)
    ExpenseCategory.SAVINGS -> Color(0xFF00695C)
    else -> Color(0xFF9E9E9E)
}
