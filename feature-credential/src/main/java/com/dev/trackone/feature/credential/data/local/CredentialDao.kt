package com.dev.trackone.feature.credential.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for credential items.
 */
@Dao
interface CredentialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CredentialEntity): Long

    @Update
    suspend fun update(entity: CredentialEntity)

    @Delete
    suspend fun delete(entity: CredentialEntity)

    @Query("DELETE FROM credential_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM credential_items")
    suspend fun getAllOnce(): List<CredentialEntity>

    @Query("DELETE FROM credential_items")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CredentialEntity>)

    @Query("SELECT * FROM credential_items ORDER BY title ASC")
    fun getAll(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credential_items WHERE id = :id")
    fun getById(id: Long): Flow<CredentialEntity?>

    @Query("SELECT * FROM credential_items WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credential_items WHERE category = :category ORDER BY title ASC")
    fun getByCategory(category: String): Flow<List<CredentialEntity>>

    @Query("""
        SELECT * FROM credential_items 
        WHERE title LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%'
        OR email LIKE '%' || :query || '%'
        OR url LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun search(query: String): Flow<List<CredentialEntity>>

    @Query("SELECT DISTINCT category FROM credential_items WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
}
