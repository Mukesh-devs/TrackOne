package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.ui.graphics.Color
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.presentation.SpendingFilter

/**
 * Slice colours for the statistics breakdown.
 *
 * Shared rather than file-private so the drill-down screen can carry the same
 * accent as the slice that opened it — that colour is the only thing tying the
 * two screens together once the chart is off-screen.
 */
internal fun categoryColor(c: ExpenseCategory): Color = when (c) {
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

internal fun paymentModeColor(m: PaymentMode): Color = when (m) {
    PaymentMode.CREDIT_CARD -> Color(0xFF5E35B1)
    PaymentMode.DEBIT_CARD -> Color(0xFF1E88E5)
    PaymentMode.UPI -> Color(0xFF00897B)
    PaymentMode.CASH -> Color(0xFF43A047)
    PaymentMode.NET_BANKING -> Color(0xFFF4511E)
    PaymentMode.OTHERS -> Color(0xFF9E9E9E)
}

internal fun SpendingFilter.accentColor(): Color = when (this) {
    is SpendingFilter.ByCategory -> categoryColor(category)
    is SpendingFilter.ByPaymentMode -> paymentModeColor(mode)
}
