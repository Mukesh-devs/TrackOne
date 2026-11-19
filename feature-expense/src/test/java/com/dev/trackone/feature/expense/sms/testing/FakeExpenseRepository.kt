package com.dev.trackone.feature.expense.sms.testing

import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

/** Records creates; [failWith] makes the next one throw instead. */
class FakeExpenseRepository : ExpenseRepository {

    data class Insert(val item: ExpenseItem, val idempotencyKey: String?)

    val inserts = mutableListOf<Insert>()
    var failWith: Exception? = null

    override suspend fun insert(item: ExpenseItem, idempotencyKey: String?): Long {
        failWith?.let { throw it }
        inserts += Insert(item, idempotencyKey)
        return inserts.size.toLong()
    }

    override fun getAll(): Flow<List<ExpenseItem>> = unused()
    override fun getById(id: Long): Flow<ExpenseItem?> = unused()
    override fun getByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseItem>> = unused()
    override fun getByType(isIncome: Boolean): Flow<List<ExpenseItem>> = unused()
    override fun getByCategory(category: String): Flow<List<ExpenseItem>> = unused()
    override fun search(query: String): Flow<List<ExpenseItem>> = unused()
    override fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double> = unused()
    override fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double> = unused()
    override suspend fun update(item: ExpenseItem) = unused()
    override suspend fun delete(item: ExpenseItem) = unused()
    override suspend fun deleteById(id: Long) = unused()
    override suspend fun refreshFromCloud() = unused()

    private fun unused(): Nothing = throw UnsupportedOperationException("not used by these tests")
}
