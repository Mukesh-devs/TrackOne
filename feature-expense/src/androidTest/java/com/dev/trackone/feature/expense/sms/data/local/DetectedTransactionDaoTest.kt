package com.dev.trackone.feature.expense.sms.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetectedTransactionDaoTest {

    private lateinit var database: DetectedTransactionsDatabase
    private lateinit var dao: DetectedTransactionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DetectedTransactionsDatabase::class.java,
        ).build()
        dao = database.detectedTransactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun row(
        dedupKey: String,
        occurredAt: Long = 1_000L,
        createdAt: Long = occurredAt,
        status: DetectionStatus = DetectionStatus.PENDING_REVIEW,
    ) = DetectedTransactionEntity(
        name = "Test Shop",
        amountPaise = 25_000,
        type = TransactionType.DEBIT,
        occurredAt = occurredAt,
        status = status,
        dedupKey = dedupKey,
        createdAt = createdAt,
    )

    @Test
    fun duplicateDedupKeyIsIgnored() = runBlocking {
        val first = dao.insert(row("same"))
        val second = dao.insert(row("same", occurredAt = 2_000L))

        assertEquals(1L, first)
        assertEquals(-1L, second)
        assertEquals(1, dao.observePendingCount().first())
    }

    @Test
    fun pendingCountAndListOnlyIncludePendingRows() = runBlocking {
        dao.insert(row("a", occurredAt = 1_000L))
        dao.insert(row("b", occurredAt = 3_000L))
        dao.insert(row("c", occurredAt = 2_000L, status = DetectionStatus.ACCEPTED_UNSYNCED))

        assertEquals(2, dao.observePendingCount().first())
        assertEquals(listOf("b", "a"), dao.observePending().first().map { it.dedupKey })
    }

    @Test
    fun acceptMovesRowOutOfPending() = runBlocking {
        val id = dao.insert(row("a"))

        assertEquals(1, dao.accept(id))
        assertEquals(0, dao.observePendingCount().first())
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
        assertEquals(listOf(id), dao.getAcceptedUnsyncedIds())
        // A second accept is a no-op.
        assertEquals(0, dao.accept(id))
    }

    @Test
    fun declineDeletesPendingRow() = runBlocking {
        val id = dao.insert(row("a"))

        assertEquals(1, dao.decline(id))
        assertNull(dao.getById(id))
        assertEquals(0, dao.observePendingCount().first())
    }

    @Test
    fun declineDoesNotDeleteAcceptedRow() = runBlocking {
        val id = dao.insert(row("a"))
        dao.accept(id)

        assertEquals(0, dao.decline(id))
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
    }

    @Test
    fun deleteSyncedRemovesRow() = runBlocking {
        val id = dao.insert(row("a"))
        dao.accept(id)

        dao.deleteSynced(id)

        assertNull(dao.getById(id))
    }

    @Test
    fun purgeRemovesOnlyOldPendingRows() = runBlocking {
        val oldPending = dao.insert(row("old", createdAt = 100L))
        val oldAccepted = dao.insert(row("old-accepted", createdAt = 100L))
        dao.accept(oldAccepted)
        val recent = dao.insert(row("recent", createdAt = 5_000L))

        assertEquals(1, dao.purgeOlderThan(cutoff = 1_000L))

        assertNull(dao.getById(oldPending))
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(oldAccepted)?.status)
        assertEquals(DetectionStatus.PENDING_REVIEW, dao.getById(recent)?.status)
    }
}
