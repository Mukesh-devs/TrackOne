package com.dev.trackone.feature.expense.presentation

import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.model.PaymentMode

/**
 * Time period for expense filtering.
 */
enum class TimePeriod(val label: String) {
    DAY("Day"),
    MONTH("Month"),
    YEAR("Year"),
}

/**
 * One slice of the statistics breakdown, drilled into.
 *
 * The chart splits the same spend two ways, and both drill down identically, so
 * the screen behind a slice takes this rather than a category or a payment mode
 * specifically.
 */
sealed interface SpendingFilter {

    /** Heading for the drill-down screen — the slice's own name. */
    val label: String

    fun matches(item: ExpenseItem): Boolean

    data class ByCategory(val category: ExpenseCategory) : SpendingFilter {
        override val label: String get() = category.displayName
        override fun matches(item: ExpenseItem): Boolean = item.category == category
    }

    data class ByPaymentMode(val mode: PaymentMode) : SpendingFilter {
        override val label: String get() = mode.displayName
        override fun matches(item: ExpenseItem): Boolean = item.paymentMode == mode
    }
}

/**
 * UI state for the Expense module screens.
 */
data class ExpenseUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val items: List<ExpenseItem> = emptyList(),
    val incomeItems: List<ExpenseItem> = emptyList(),
    val expenseItems: List<ExpenseItem> = emptyList(),
    val totalIncome: Double = 0.0,
    val selectedItem: ExpenseItem? = null,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val error: String? = null,
    val selectedCategory: ExpenseCategory? = null,
    /** The statistics slice being drilled into, if any. */
    val spendingFilter: SpendingFilter? = null,
    val selectedPeriod: TimePeriod = TimePeriod.MONTH,
    val currentDate: Long = System.currentTimeMillis(),

    // Statistics specific state for CSV export
    val statsStartDate: Long? = null,
    val statsEndDate: Long? = null,
    val statsItems: List<ExpenseItem> = emptyList(),
    val statsTotalIncome: Double = 0.0,
    val statsTotalExpense: Double = 0.0,
    /** One-shot message for a failed write (delete), shown as a snackbar. */
    val actionMessage: String? = null,
)

/**
 * State for the Add/Edit form.
 */
data class ExpenseFormState(
    val id: Long = 0,
    val title: String = "",
    val amount: String = "",
    val isIncome: Boolean = false,
    val category: ExpenseCategory? = null,
    val paymentMode: PaymentMode? = null,
    val date: Long? = System.currentTimeMillis(),
    val notes: String = "",
    val isEditing: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val paymentModeError: String? = null,
    val dateError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    /** Why the last save failed — usually "no internet", since saves are cloud-first. */
    val saveError: String? = null,
)
