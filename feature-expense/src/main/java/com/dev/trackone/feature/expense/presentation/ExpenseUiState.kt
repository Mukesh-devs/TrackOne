package com.dev.trackone.feature.expense.presentation

import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem

/**
 * UI state for the Expense module screens.
 */
data class ExpenseUiState(
    val isLoading: Boolean = true,
    val items: List<ExpenseItem> = emptyList(),
    val incomeItems: List<ExpenseItem> = emptyList(),
    val expenseItems: List<ExpenseItem> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val error: String? = null,
    val selectedCategory: ExpenseCategory? = null,
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
    val date: Long? = System.currentTimeMillis(),
    val notes: String = "",
    val isEditing: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val dateError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
)
