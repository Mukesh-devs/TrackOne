package com.dev.trackone.feature.credential.data.repository

import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.mapper.toDomain
import com.dev.trackone.feature.credential.data.mapper.toEntity
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.domain.repository.CredentialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [CredentialRepository].
 */
@Singleton
class CredentialRepositoryImpl @Inject constructor(
    private val dao: CredentialDao,
) : CredentialRepository {

    override fun getAll(): Flow<List<CredentialItem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun getById(id: Long): Flow<CredentialItem?> =
        dao.getById(id).map { it?.toDomain() }

    override fun getFavorites(): Flow<List<CredentialItem>> =
        dao.getFavorites().map { entities -> entities.map { it.toDomain() } }

    override fun getByCategory(category: String): Flow<List<CredentialItem>> =
        dao.getByCategory(category).map { entities -> entities.map { it.toDomain() } }

    override fun search(query: String): Flow<List<CredentialItem>> =
        dao.search(query).map { entities -> entities.map { it.toDomain() } }
        
    override fun getAllCategories(): Flow<List<String>> =
        dao.getAllCategories()

    override suspend fun insert(item: CredentialItem): Long =
        dao.insert(item.toEntity())

    override suspend fun update(item: CredentialItem) =
        dao.update(item.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun delete(item: CredentialItem) =
        dao.delete(item.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    override fun countAll(): Flow<Int> =
        dao.countAll()
}
