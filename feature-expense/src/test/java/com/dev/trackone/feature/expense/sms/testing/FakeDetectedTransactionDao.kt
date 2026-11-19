package com.dev.trackone.feature.expense.sms.testing

import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionDao
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory stand-in for the Room DAO, mirroring its SQL semantics. */
class FakeDetectedTransactionDao : DetectedTransactionDao {

    val rows = MutableStateFlow<List<DetectedTransactionEntity>>(emptyList())
    private var nextId = 1L
    var lastPurgeCutoff: Long? = null

    override suspend fun insert(entity: DetectedTransactionEntity): Long {
        if (rows.value.any { it.dedupKey == entity.dedupKey }) return -1L
        val id = nextId++
        rows.value = rows.value + entity.copy(id = id)
        return id
    }

    override fun observePendingCount(): Flow<Int> =
        rows.map { list -> list.count { it.status == DetectionStatus.PENDING_REVIEW } }

    override fun observePending(): Flow<List<DetectedTransactionEntity>> = rows.map { list ->
        list.filter { it.status == DetectionStatus.PENDING_REVIEW }
            .sortedWith(compareByDescending<DetectedTransactionEntity> { it.occurredAt }.thenByDescending { it.id })
    }

    override suspend fun getById(id: Long): DetectedTransactionEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun getAcceptedUnsyncedIds(): List<Long> =
        rows.value.filter { it.status == DetectionStatus.ACCEPTED_UNSYNCED }.map { it.id }

    override suspend fun accept(id: Long): Int {
        val row = rows.value.firstOrNull { it.id == id && it.status == DetectionStatus.PENDING_REVIEW } ?: return 0
        rows.value = rows.value.map { if (it.id == row.id) it.copy(status = DetectionStatus.ACCEPTED_UNSYNCED) else it }
        return 1
    }

    override suspend fun decline(id: Long): Int {
        val before = rows.value.size
        rows.value = rows.value.filterNot { it.id == id && it.status == DetectionStatus.PENDING_REVIEW }
        return before - rows.value.size
    }

    override suspend fun deleteSynced(id: Long) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun purgeOlderThan(cutoff: Long): Int {
        lastPurgeCutoff = cutoff
        val before = rows.value.size
        rows.value = rows.value.filterNot { it.status == DetectionStatus.PENDING_REVIEW && it.createdAt < cutoff }
        return before - rows.value.size
    }
}
