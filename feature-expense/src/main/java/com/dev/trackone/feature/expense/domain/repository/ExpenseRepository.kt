package com.dev.trackone.feature.expense.domain.repository

import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for expense items.
 */
interface ExpenseRepository {

    fun getAll(): Flow<List<ExpenseItem>>

    fun getById(id: Long): Flow<ExpenseItem?>

    fun getByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseItem>>

    fun getByType(isIncome: Boolean): Flow<List<ExpenseItem>>

    fun getByCategory(category: String): Flow<List<ExpenseItem>>

    fun search(query: String): Flow<List<ExpenseItem>>
    
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double>
    
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double>

    suspend fun insert(item: ExpenseItem): Long

    suspend fun update(item: ExpenseItem)

    suspend fun delete(item: ExpenseItem)

    suspend fun deleteById(id: Long)
}
