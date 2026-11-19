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

    /**
     * Creates [item] on the server. Pass an [idempotencyKey] for a create that
     * may be retried, so a retry after a lost response cannot add it twice.
     */
    suspend fun insert(item: ExpenseItem, idempotencyKey: String? = null): Long

    suspend fun update(item: ExpenseItem)

    suspend fun delete(item: ExpenseItem)

    suspend fun deleteById(id: Long)

    /**
     * Forces an immediate pull from the API, bypassing the background-refresh
     * throttle. Drives pull-to-refresh; throws if the network is unavailable so
     * the gesture can report failure instead of spinning silently.
     */
    suspend fun refreshFromCloud()
}
