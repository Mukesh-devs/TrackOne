package com.dev.trackone.feature.expense.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an income or expense transaction.
 */
@Entity(tableName = "expense_items")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean, // true for Income, false for Expense
    val category: String,
    val paymentMode: String = "CASH",
    val date: Long,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
