package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.presentation.TimePeriod
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Flight

/**
 * Day / Month / Year segmented toggle used in Home and Stats screens.
 */
@Composable
fun SharedTimePeriodToggle(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TimePeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = { onPeriodSelected(period) }),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                }
            }
        }
    }
}

/**
 * Left/Right arrow date navigator showing the current date label.
 */
@Composable
fun SharedDateNavigationRow(
    dateLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * List item for transactions matching the new design.
 */
@Composable
fun ExpenseListItem(
    item: ExpenseItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        val (catIcon, catColor) = when (item.category) {
            ExpenseCategory.FOOD -> Icons.Default.Fastfood to Color(0xFFFFB300)
            ExpenseCategory.TRANSPORTATION -> Icons.Default.DirectionsCar to Color(0xFF1E88E5)
            ExpenseCategory.HOUSING -> Icons.Default.Home to Color(0xFF43A047)
            ExpenseCategory.UTILITIES -> Icons.Default.Wifi to Color(0xFF8E24AA)
            ExpenseCategory.HEALTHCARE -> Icons.Default.LocalHospital to Color(0xFFE53935)
            ExpenseCategory.SHOPPING -> Icons.Default.ShoppingCart to Color(0xFF00ACC1)
            ExpenseCategory.ENTERTAINMENT -> Icons.Default.Movie to Color(0xFFE91E63)
            ExpenseCategory.EDUCATION -> Icons.Default.School to Color(0xFF3949AB)
            ExpenseCategory.TRAVEL -> Icons.Default.Flight to Color(0xFF00897B)
            ExpenseCategory.SALARY, ExpenseCategory.FREELANCE -> Icons.Default.Work to Color(0xFF43A047)
            ExpenseCategory.GIFTS_RECEIVED -> Icons.Default.CardGiftcard to Color(0xFFF4511E)
            else -> Icons.Default.Receipt to Color.Gray
        }

        val payIcon = when (item.paymentMode) {
            PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD -> Icons.Default.CreditCard
            PaymentMode.CASH -> Icons.Default.Money
            else -> Icons.Default.Receipt
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon in a rounded box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(catColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = item.category.displayName,
                    tint = catColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title & Time/Category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                val timeStr = DateUtils.formatDate(item.date, "h:mm a")
                val catStr = item.category.displayName
                Text(
                    text = "$timeStr • $catStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount & Payment Mode Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencyUtils.formatAmount(item.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = payIcon,
                    contentDescription = item.paymentMode.displayName,
                    tint = catColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
