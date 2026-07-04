package com.dev.trackone.feature.expense.data.repository

import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expense.data.mapper.toDomain
import com.dev.trackone.feature.expense.data.mapper.toEntity
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [ExpenseRepository].
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
) : ExpenseRepository {

    override fun getAll(): Flow<List<ExpenseItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getById(id: Long): Flow<ExpenseItem?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseItem>> =
        dao.getByDateRange(startTime, endTime).map { entities -> entities.map { it.toDomain() } }

    override fun getByType(isIncome: Boolean): Flow<List<ExpenseItem>> =
        dao.getByType(isIncome).map { entities -> entities.map { it.toDomain() } }

    override fun getByCategory(category: String): Flow<List<ExpenseItem>> =
        dao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override fun search(query: String): Flow<List<ExpenseItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }
        
    override fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double> =
        dao.getTotalIncome(startTime, endTime).map { it ?: 0.0 }
        
    override fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double> =
        dao.getTotalExpense(startTime, endTime).map { it ?: 0.0 }

    override suspend fun insert(item: ExpenseItem): Long =
        dao.insert(item.toEntity())

    override suspend fun update(item: ExpenseItem) =
        dao.update(item.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun delete(item: ExpenseItem) =
        dao.delete(item.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)
}
