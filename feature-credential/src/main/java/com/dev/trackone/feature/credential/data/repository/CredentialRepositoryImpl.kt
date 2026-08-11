package com.dev.trackone.feature.credential.data.repository

import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.apiDelete
import com.dev.trackone.core.network.api.cachedWithRemoteRefresh
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.mapper.toDomain
import com.dev.trackone.feature.credential.data.mapper.toEntity
import com.dev.trackone.feature.credential.data.mapper.toRequest
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.domain.repository.CredentialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-first implementation of [CredentialRepository]: the Worker API is the
 * source of truth and Room is an offline cache in front of it.
 *
 * Reads emit the cache immediately and refresh it in the background; writes go
 * to the API first and only then to Room, throwing when the network is down so
 * the ViewModel can say so rather than diverging from the server.
 */
@Singleton
class CredentialRepositoryImpl @Inject constructor(
    private val dao: CredentialDao,
    private val api: TrackOneApi,
    private val refreshCoordinator: RemoteRefreshCoordinator,
) : CredentialRepository {

    private fun <T> Flow<T>.cached(): Flow<T> =
        cachedWithRemoteRefresh(refreshCoordinator, CACHE_KEY) { pullFromCloud() }

    private suspend fun pullFromCloud() {
        val remote = fetchAllPages { limit, offset -> api.getCredentialItems(limit, offset) }
        dao.replaceAll(remote.map { it.toEntity() })
    }

    override fun getAll(): Flow<List<CredentialItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getById(id: Long): Flow<CredentialItem?> =
        dao.getById(id).map { it?.toDomain() }.cached()

    override fun getFavorites(): Flow<List<CredentialItem>> =
        dao.getFavorites().map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getByCategory(category: String): Flow<List<CredentialItem>> =
        dao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun search(query: String): Flow<List<CredentialItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }.cached()

    override fun getAllCategories(): Flow<List<String>> = dao.getAllCategories().cached()

    override suspend fun insert(item: CredentialItem): Long {
        val created = apiCall { api.createCredentialItem(item.toRequest()) }
        dao.insert(created.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
        return created.id
    }

    override suspend fun update(item: CredentialItem) {
        val updated = apiCall { api.updateCredentialItem(item.id, item.toRequest()) }
        // insert (REPLACE), not update: the row may not be cached locally yet.
        dao.insert(updated.toEntity())
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    override suspend fun delete(item: CredentialItem) = deleteById(item.id)

    override suspend fun deleteById(id: Long) {
        apiDelete { api.deleteCredentialItem(id) }
        dao.deleteById(id)
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    override fun countAll(): Flow<Int> = dao.countAll().cached()


    override suspend fun refreshFromCloud() {
        refreshCoordinator.invalidate(CACHE_KEY)
        pullFromCloud()
    }

    private companion object {
        const val CACHE_KEY = "credential_items"
    }
}
