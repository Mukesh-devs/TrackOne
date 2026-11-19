package com.dev.trackone.feature.expense.sms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for detected transactions.
 *
 * Status values are written as literals; they match the enum names stored by
 * [DetectedTransactionConverters].
 */
@Dao
interface DetectedTransactionDao {

    /** IGNORE, so a duplicate [DetectedTransactionEntity.dedupKey] returns -1 instead of a second row. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DetectedTransactionEntity): Long

    @Query("SELECT COUNT(*) FROM detected_transactions WHERE status = 'PENDING_REVIEW'")
    fun observePendingCount(): Flow<Int>

    @Query(
        """
        SELECT * FROM detected_transactions
        WHERE status = 'PENDING_REVIEW'
        ORDER BY occurredAt DESC, id DESC
        """,
    )
    fun observePending(): Flow<List<DetectedTransactionEntity>>

    @Query("SELECT * FROM detected_transactions WHERE id = :id")
    suspend fun getById(id: Long): DetectedTransactionEntity?

    @Query("SELECT id FROM detected_transactions WHERE status = 'ACCEPTED_UNSYNCED'")
    suspend fun getAcceptedUnsyncedIds(): List<Long>

    /** Returns the number of rows changed: 0 if the row was already handled. */
    @Query(
        """
        UPDATE detected_transactions SET status = 'ACCEPTED_UNSYNCED'
        WHERE id = :id AND status = 'PENDING_REVIEW'
        """,
    )
    suspend fun accept(id: Long): Int

    /** Only a pending row can be declined; an accepted one is already on its way. */
    @Query("DELETE FROM detected_transactions WHERE id = :id AND status = 'PENDING_REVIEW'")
    suspend fun decline(id: Long): Int

    /** Called once the backend has the row — it is the source of truth from then on. */
    @Query("DELETE FROM detected_transactions WHERE id = :id")
    suspend fun deleteSynced(id: Long)

    /** Drops suggestions nobody reviewed; accepted rows are never purged. */
    @Query("DELETE FROM detected_transactions WHERE status = 'PENDING_REVIEW' AND createdAt < :cutoff")
    suspend fun purgeOlderThan(cutoff: Long): Int
}
