package com.dev.trackone.feature.credential.domain.repository

import com.dev.trackone.feature.credential.domain.model.CredentialItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for credential items.
 */
interface CredentialRepository {

    fun getAll(): Flow<List<CredentialItem>>

    fun getById(id: Long): Flow<CredentialItem?>

    fun getFavorites(): Flow<List<CredentialItem>>

    fun getByCategory(category: String): Flow<List<CredentialItem>>

    fun search(query: String): Flow<List<CredentialItem>>
    
    fun getAllCategories(): Flow<List<String>>

    suspend fun insert(item: CredentialItem): Long

    suspend fun update(item: CredentialItem)

    suspend fun delete(item: CredentialItem)

    suspend fun deleteById(id: Long)

    fun countAll(): Flow<Int>

    /**
     * Forces an immediate pull from the API, bypassing the background-refresh
     * throttle. Drives pull-to-refresh; throws if the network is unavailable so
     * the gesture can report failure instead of spinning silently.
     */
    suspend fun refreshFromCloud()
}
