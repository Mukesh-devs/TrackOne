package com.dev.trackone.feature.expense.data.repository

import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.apiDelete
import com.dev.trackone.core.network.api.cachedWithRemoteRefresh
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expense.data.mapper.toDomain
import com.dev.trackone.feature.expense.data.mapper.toEntity
import com.dev.trackone.feature.expense.data.mapper.toRequest
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-first implementation of [ExpenseRepository]: the Worker API is the
 * source of truth and Room is an offline cache in front of it.
 *
 * Reads emit the cache immediately and refresh it in the background; writes go
 * to the API first and only then to Room, throwing when the network is down so
 * the ViewModel can say so rather than diverging from the server.
 *
 * Note that the refresh always pulls the *whole* table rather than the visible
 * date range. Period-scoped reads (Day/Month/Year) are served by Room queries
 * over that full cache — fetching only the visible range and then calling
 * `replaceAll` would evict every other month from the cache.
 */
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao,
    private val api: TrackOneApi,
    private val refreshCoordinator: RemoteRefreshCoordinator,
) : ExpenseRepository {

    private fun <T> Flow<T>.cached(): Flow<T> =
        cachedWithRemoteRefresh(refreshCoordinator, CACHE_KEY) { pullFromCloud() }

    private suspend fun pullFromCloud() {
        val remote = fetchAllPages { limit, offset -> api.getExpenseItems(limit, offset) }
        dao.replaceAll(remote.map { it.toEntity() })
    }

    override fun getAll(): Flow<List<ExpenseItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getById(id: Long): Flow<ExpenseItem?> =
        dao.getById(id).map { it?.toDomain() }.cached()

    override fun getByDateRange(startTime: Long, endTime: Long): Flow<List<ExpenseItem>> =
        dao.getByDateRange(startTime, endTime)
            .map { entities -> entities.map { it.toDomain() } }
            .cached()

    override fun getByType(isIncome: Boolean): Flow<List<ExpenseItem>> =
        dao.getByType(isIncome).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getByCategory(category: String): Flow<List<ExpenseItem>> =
        dao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun search(query: String): Flow<List<ExpenseItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double> =
        dao.getTotalIncome(startTime, endTime).map { it ?: 0.0 }.cached()

    override fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double> =
        dao.getTotalExpense(startTime, endTime).map { it ?: 0.0 }.cached()

    override suspend fun insert(item: ExpenseItem): Long {
        val created = apiCall { api.createExpenseItem(item.toRequest()) }
        dao.insert(created.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
        return created.id
    }

    override suspend fun update(item: ExpenseItem) {
        val updated = apiCall { api.updateExpenseItem(item.id, item.toRequest()) }
        // insert (REPLACE), not update: the row may not be cached locally yet.
        dao.insert(updated.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    override suspend fun delete(item: ExpenseItem) = deleteById(item.id)

    override suspend fun deleteById(id: Long) {
        apiDelete { api.deleteExpenseItem(id) }
        dao.deleteById(id)
        refreshCoordinator.invalidate(CACHE_KEY)
    }


    override suspend fun refreshFromCloud() {
        refreshCoordinator.invalidate(CACHE_KEY)
        pullFromCloud()
    }

    private companion object {
        const val CACHE_KEY = "expense_items"
    }
}
