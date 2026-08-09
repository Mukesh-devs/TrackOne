package com.dev.trackone.feature.expiry.data.repository

import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.feature.expiry.data.mapper.toDomain
import com.dev.trackone.feature.expiry.data.mapper.toEntity
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem
import com.dev.trackone.feature.expiry.domain.repository.ExpiryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [ExpiryRepository].
 */
@Singleton
class ExpiryRepositoryImpl @Inject constructor(
    private val dao: ExpiryDao,
) : ExpiryRepository {

    override fun getAll(): Flow<List<ExpiryItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getById(id: Long): Flow<ExpiryItem?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getByName(name: String): Flow<List<ExpiryItem>> =
        dao.getByName(name).map { entities -> entities.map { it.toDomain() } }

    override fun getExpired(): Flow<List<ExpiryItem>> =
        dao.getExpired(System.currentTimeMillis()).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getActive(): Flow<List<ExpiryItem>> =
        dao.getActive(System.currentTimeMillis()).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUpcoming(days: Int): Flow<List<ExpiryItem>> {
        val now = System.currentTimeMillis()
        val future = now + TimeUnit.DAYS.toMillis(days.toLong())
        return dao.getUpcoming(now, future).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun search(query: String): Flow<List<ExpiryItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }

    override fun getAllProviders(): Flow<List<String>> = dao.getAllProviders()

    override suspend fun insert(item: ExpiryItem): Long =
        dao.insert(item.toEntity())

    override suspend fun update(item: ExpiryItem) =
        dao.update(item.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun delete(item: ExpiryItem) =
        dao.delete(item.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    override fun countExpiringBetween(dayStart: Long, dayEnd: Long): Flow<Int> =
        dao.countExpiringBetween(dayStart, dayEnd)
}
