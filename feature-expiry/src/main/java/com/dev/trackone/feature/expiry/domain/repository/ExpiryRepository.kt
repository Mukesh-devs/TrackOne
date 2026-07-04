package com.dev.trackone.feature.expiry.domain.repository

import com.dev.trackone.feature.expiry.domain.model.ExpiryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for expiry items.
 * The implementation uses Room, but this abstraction allows
 * future migration to Room + Cloud Sync without changing consumers.
 */
interface ExpiryRepository {

    fun getAll(): Flow<List<ExpiryItem>>

    fun getById(id: Long): Flow<ExpiryItem?>

    fun getByName(name: String): Flow<List<ExpiryItem>>

    fun getExpired(): Flow<List<ExpiryItem>>

    fun getActive(): Flow<List<ExpiryItem>>

    fun getUpcoming(days: Int): Flow<List<ExpiryItem>>

    fun search(query: String): Flow<List<ExpiryItem>>

    fun getAllProviders(): Flow<List<String>>

    suspend fun insert(item: ExpiryItem): Long

    suspend fun update(item: ExpiryItem)

    suspend fun delete(item: ExpiryItem)

    suspend fun deleteById(id: Long)
}
