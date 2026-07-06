package com.dev.trackone.feature.expense.presentation.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneDatePicker
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod
import java.io.OutputStreamWriter

@Composable
fun ExpenseStatsScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onDateRangeSelected: (start: Long?, end: Long?) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    getCsvData: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = getCsvData()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(csvContent)
                    }
                }
                Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Dimens.SpacingLg,
                end = Dimens.SpacingLg,
                top = Dimens.SpacingMd,
                bottom = 90.dp // Accounts for global bottom navigation bar
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Compact Credit | Debit Bar with Download Icon inside (Using Stats Data)
        TrackOneCard(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingSm),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Credit (Income)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(Dimens.IconSizeSm)
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                            Text(
                                text = "Credit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyUtils.formatAmount(uiState.statsTotalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
    
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
    
                    // Debit (Expense)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(Dimens.IconSizeSm)
                            )
                            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                            Text(
                                text = "Debit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyUtils.formatAmount(uiState.statsTotalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(
                    onClick = { 
                        if (uiState.statsItems.isEmpty()) {
                            Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show()
                        } else {
                            val dl = DateUtils.formatDateShort(System.currentTimeMillis()).replace("/", "")
                            createDocumentLauncher.launch("TrackOne_Export_$dl.csv")
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // Single Bar for Date Range Picker
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showStartDatePicker = true }
                        .padding(Dimens.SpacingXs)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                    Column {
                        Text("From", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = uiState.statsStartDate?.let { DateUtils.formatDateShort(it) } ?: "Select",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // To Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showEndDatePicker = true }
                        .padding(Dimens.SpacingXs)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                    Column {
                        Text("To", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = uiState.statsEndDate?.let { DateUtils.formatDateShort(it) } ?: "Select",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (uiState.statsStartDate != null || uiState.statsEndDate != null) {
                    IconButton(
                        onClick = { onDateRangeSelected(null, null) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        // Insights Section
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        SharedTimePeriodToggle(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = onPeriodChange
        )
        
        SharedDateNavigationRow(
            dateLabel = dateLabel,
            onPrevious = { onNavigateDate(false) },
            onNext = { onNavigateDate(true) }
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        // Calculate expenses by mode based on uiState.items
        val expenses = uiState.items.filter { !it.isIncome }
        val creditCardExp = expenses.filter { it.paymentMode == PaymentMode.CREDIT_CARD }.sumOf { it.amount }
        val debitCardExp = expenses.filter { it.paymentMode == PaymentMode.DEBIT_CARD }.sumOf { it.amount }
        val upiExp = expenses.filter { it.paymentMode == PaymentMode.UPI }.sumOf { it.amount }
        val cashExp = expenses.filter { it.paymentMode == PaymentMode.CASH }.sumOf { it.amount }
        val netBankExp = expenses.filter { it.paymentMode == PaymentMode.NET_BANKING }.sumOf { it.amount }
        val otherExp = expenses.filter { it.paymentMode == PaymentMode.OTHERS }.sumOf { it.amount }

        // Layout the 8 items in a compact grid (4 rows x 2 cols)
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)
        ) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                CompactStatBox("Total Credits", uiState.totalIncome, Icons.Default.ArrowUpward, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                CompactStatBox("Total Expense", uiState.totalExpense, Icons.Default.ArrowDownward, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                CompactStatBox("Credit Card", creditCardExp, Icons.Default.CreditCard, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                CompactStatBox("Debit Card", debitCardExp, Icons.Default.Payment, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                CompactStatBox("UPI", upiExp, Icons.Default.PhoneAndroid, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                CompactStatBox("Cash", cashExp, Icons.Default.Money, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm)) {
                CompactStatBox("Net Banking", netBankExp, Icons.Default.AccountBalance, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                CompactStatBox("Other", otherExp, Icons.Default.Receipt, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }
    }

    if (showStartDatePicker) {
        TrackOneDatePicker(
            initialDate = uiState.statsStartDate ?: System.currentTimeMillis(),
            title = "Select From Date",
            onDateSelected = { 
                showStartDatePicker = false
                if (it != null) onDateRangeSelected(it, uiState.statsEndDate)
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        TrackOneDatePicker(
            initialDate = uiState.statsEndDate ?: System.currentTimeMillis(),
            title = "Select To Date",
            onDateSelected = { 
                showEndDatePicker = false
                if (it != null) onDateRangeSelected(uiState.statsStartDate, it)
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun CompactStatBox(
    title: String,
    amount: Double,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingSm),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = CurrencyUtils.formatAmount(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
