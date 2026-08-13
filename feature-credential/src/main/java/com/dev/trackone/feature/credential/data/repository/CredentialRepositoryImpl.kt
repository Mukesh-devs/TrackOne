package com.dev.trackone.feature.credential.data.repository

import com.dev.trackone.core.network.api.RemoteRefreshCoordinator
import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.api.apiDelete
import com.dev.trackone.core.network.api.cachedWithRemoteRefresh
import com.dev.trackone.core.network.api.fetchAllPages
import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.credential.data.mapper.MASKED_PASSWORD
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
        dao.replaceAll(hydratePasswords(remote))
    }

    /**
     * Fills in the passwords the list endpoint masks.
     *
     * `GET /credential-items` always answers with [MASKED_PASSWORD]; only the
     * per-item endpoint returns the real value, so it is fetched one row at a
     * time. A row whose server `updatedAt` still matches the cached copy cannot
     * have changed, so its password is reused — which is what keeps the routine
     * background refresh at zero extra requests and makes only new or edited
     * rows cost a call. A cached mask (written by an older build, back when the
     * list endpoint honoured `reveal=true`) never counts as reusable, so those
     * rows heal on the next refresh.
     *
     * A failure here propagates rather than falling back to the mask: leaving
     * the previous cache in place is always better than replacing a working
     * password with asterisks.
     */
    private suspend fun hydratePasswords(remote: List<CredentialItemDto>): List<CredentialEntity> {
        if (remote.isEmpty()) return emptyList()
        val cached = dao.getAllOnce().associateBy { it.id }
        return remote.map { dto ->
            val reusable = cached[dto.id]
                ?.takeIf { it.updatedAt == dto.updatedAt && it.password != MASKED_PASSWORD }
            dto.toEntity(
                password = reusable?.password ?: apiCall { api.getCredentialItem(dto.id) }.password,
            )
        }
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
        dao.insert(created.toEntity(password = created.unmaskedPassword(item.password)))
        refreshCoordinator.invalidate(CACHE_KEY)
        return created.id
    }

    override suspend fun update(item: CredentialItem) {
        val updated = apiCall { api.updateCredentialItem(item.id, item.toRequest()) }
        // insert (REPLACE), not update: the row may not be cached locally yet.
        dao.insert(updated.toEntity(password = updated.unmaskedPassword(item.password)))
        refreshCoordinator.invalidate(CACHE_KEY)
    }

    /**
     * Guards the write path against a masked echo. Create and update currently
     * reply with the real password, but we just sent it, so there is no reason
     * to depend on that — and caching `********` after a save would lock the
     * user out of their own entry.
     */
    private fun CredentialItemDto.unmaskedPassword(sent: String): String =
        if (password == MASKED_PASSWORD) sent else password

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
