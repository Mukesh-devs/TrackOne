package com.dev.trackone.feature.expense.domain.model

/**
 * Domain model for a single expense/income transaction.
 */
data class ExpenseItem(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: ExpenseCategory,
    val date: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Enum for expense and income categories.
 */
enum class ExpenseCategory(val displayName: String, val isIncome: Boolean) {
    // Income Categories
    SALARY("Salary", true),
    FREELANCE("Freelance", true),
    INVESTMENTS("Investments", true),
    GIFTS_RECEIVED("Gifts", true),
    OTHER_INCOME("Other Income", true),
    
    // Expense Categories
    HOUSING("Housing", false),
    FOOD("Food & Dining", false),
    TRANSPORTATION("Transportation", false),
    UTILITIES("Utilities", false),
    INSURANCE("Insurance", false),
    HEALTHCARE("Healthcare", false),
    SAVINGS("Savings & Debt", false),
    PERSONAL("Personal Spending", false),
    ENTERTAINMENT("Entertainment", false),
    EDUCATION("Education", false),
    SHOPPING("Shopping", false),
    TRAVEL("Travel", false),
    OTHER_EXPENSE("Other Expense", false);
    
    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.find { it.name == name } ?: if (name.contains("Income", ignoreCase = true)) OTHER_INCOME else OTHER_EXPENSE
        }
        
        fun getIncomeCategories() = entries.filter { it.isIncome }
        fun getExpenseCategories() = entries.filter { !it.isIncome }
    }
}
