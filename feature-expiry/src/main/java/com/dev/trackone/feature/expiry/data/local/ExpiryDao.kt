package com.dev.trackone.feature.expiry.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for expiry items.
 */
@Dao
interface ExpiryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpiryEntity): Long

    @Update
    suspend fun update(entity: ExpiryEntity)

    @Delete
    suspend fun delete(entity: ExpiryEntity)

    @Query("DELETE FROM expiry_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expiry_items")
    suspend fun getAllOnce(): List<ExpiryEntity>

    @Query("DELETE FROM expiry_items")
    suspend fun deleteAll()

    @Query("SELECT MAX(updatedAt) FROM expiry_items")
    suspend fun getLatestUpdatedAt(): Long?

    @Query("SELECT MAX(updatedAt) FROM expiry_items")
    fun observeLatestUpdatedAt(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ExpiryEntity>)

    @Query("SELECT * FROM expiry_items ORDER BY expiryDate ASC")
    fun getAll(): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items WHERE id = :id")
    fun getById(id: Long): Flow<ExpiryEntity?>

    @Query("SELECT * FROM expiry_items WHERE name = :name ORDER BY expiryDate ASC")
    fun getByName(name: String): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items WHERE expiryDate < :currentTime ORDER BY expiryDate DESC")
    fun getExpired(currentTime: Long): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items WHERE expiryDate >= :currentTime ORDER BY expiryDate ASC")
    fun getActive(currentTime: Long): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items WHERE expiryDate BETWEEN :fromTime AND :toTime ORDER BY expiryDate ASC")
    fun getUpcoming(fromTime: Long, toTime: Long): Flow<List<ExpiryEntity>>

    @Query("""
        SELECT * FROM expiry_items 
        WHERE name LIKE '%' || :query || '%' 
        OR provider LIKE '%' || :query || '%'
        OR email LIKE '%' || :query || '%'
        OR username LIKE '%' || :query || '%'
        ORDER BY expiryDate ASC
    """)
    fun search(query: String): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items ORDER BY expiryDate ASC")
    fun getAllSortedByExpiry(): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items ORDER BY name ASC")
    fun getAllSortedByName(): Flow<List<ExpiryEntity>>

    @Query("SELECT * FROM expiry_items ORDER BY amount DESC")
    fun getAllSortedByAmount(): Flow<List<ExpiryEntity>>

    @Query("SELECT DISTINCT provider FROM expiry_items ORDER BY provider ASC")
    fun getAllProviders(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM expiry_items WHERE expiryDate BETWEEN :dayStart AND :dayEnd")
    fun countExpiringBetween(dayStart: Long, dayEnd: Long): Flow<Int>
}
