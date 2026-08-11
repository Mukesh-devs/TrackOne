package com.dev.trackone.feature.expiry.data.repository

import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.apiDelete
import com.dev.trackone.core.network.api.cachedWithRemoteRefresh
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.feature.expiry.data.mapper.toDomain
import com.dev.trackone.feature.expiry.data.mapper.toEntity
import com.dev.trackone.feature.expiry.data.mapper.toRequest
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem
import com.dev.trackone.feature.expiry.domain.repository.ExpiryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-first implementation of [ExpiryRepository]: the Worker API is the
 * source of truth and Room is an offline cache in front of it.
 *
 * **Reads** hand back the cached rows immediately — the UI paints instantly and
 * still works with no connection — while a background fetch replaces the cache
 * with the server's copy. Room's own flows then push the fresh rows through the
 * same collector, so callers need no refresh logic. A failed fetch is swallowed:
 * the user simply keeps seeing cached data.
 *
 * **Writes** go to the API first and only touch Room with what comes back, so
 * ids and timestamps always match the server. If the call fails the exception
 * propagates and *nothing* is written locally, which is what lets the ViewModel
 * tell the user the save didn't happen instead of silently diverging.
 */
@Singleton
class ExpiryRepositoryImpl @Inject constructor(
    private val dao: ExpiryDao,
    private val api: TrackOneApi,
    private val refreshCoordinator: RemoteRefreshCoordinator,
) : ExpiryRepository {

    /** Every read refreshes the same whole-table snapshot, so they share one key. */
    private fun <T> Flow<T>.cached(): Flow<T> =
        cachedWithRemoteRefresh(refreshCoordinator, CACHE_KEY) { pullFromCloud() }

    private suspend fun pullFromCloud() {
        val remote = fetchAllPages { limit, offset -> api.getExpiryItems(limit, offset) }
        dao.replaceAll(remote.map { it.toEntity() })
    }

    override fun getAll(): Flow<List<ExpiryItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getById(id: Long): Flow<ExpiryItem?> =
        dao.getById(id).map { it?.toDomain() }.cached()

    override fun getByName(name: String): Flow<List<ExpiryItem>> =
        dao.getByName(name).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getExpired(): Flow<List<ExpiryItem>> =
        dao.getExpired(System.currentTimeMillis())
            .map { entities -> entities.map { it.toDomain() } }
            .cached()

    override fun getActive(): Flow<List<ExpiryItem>> =
        dao.getActive(System.currentTimeMillis())
            .map { entities -> entities.map { it.toDomain() } }
            .cached()

    override fun getUpcoming(days: Int): Flow<List<ExpiryItem>> {
        val now = System.currentTimeMillis()
        val future = now + TimeUnit.DAYS.toMillis(days.toLong())
        return dao.getUpcoming(now, future)
            .map { entities -> entities.map { it.toDomain() } }
            .cached()
    }

    override fun search(query: String): Flow<List<ExpiryItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getAllProviders(): Flow<List<String>> = dao.getAllProviders().cached()

    override suspend fun insert(item: ExpiryItem): Long {
        val created = apiCall { api.createExpiryItem(item.toRequest()) }
        dao.insert(created.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
        return created.id
    }

    override suspend fun update(item: ExpiryItem) {
        val updated = apiCall { api.updateExpiryItem(item.id, item.toRequest()) }
        // insert (REPLACE), not update: the row may not be cached locally yet.
        dao.insert(updated.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    override suspend fun delete(item: ExpiryItem) = deleteById(item.id)

    override suspend fun deleteById(id: Long) {
        apiDelete { api.deleteExpiryItem(id) }
        dao.deleteById(id)
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    override fun countExpiringBetween(dayStart: Long, dayEnd: Long): Flow<Int> =
        dao.countExpiringBetween(dayStart, dayEnd).cached()


    override suspend fun refreshFromCloud() {
        refreshCoordinator.invalidate(CACHE_KEY)
        pullFromCloud()
    }

    private companion object {
        const val CACHE_KEY = "expiry_items"
    }
}
