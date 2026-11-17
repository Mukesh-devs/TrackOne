package com.dev.trackone.feature.expense.sms.data.repository

import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import com.dev.trackone.feature.expense.sms.testing.FakeDetectedTransactionDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DetectedTransactionRepositoryImplTest {

    private val dao = FakeDetectedTransactionDao()
    private val repository = DetectedTransactionRepositoryImpl(dao)

    private val sender = "VM-HDFCBK-T"
    private val body = "Rs.250.00 debited from a/c XX1234 to TEST SHOP"

    @Test
    fun bankSmsIsStoredWithOnlyExtractedFields() = runBlocking {
        assertTrue(repository.recordSms(sender, body, smsTimestampMillis = 1_000L, receivedAtMillis = 5_000L))

        val row = dao.rows.value.single()
        assertEquals("Test Shop", row.name)
        assertEquals(25_000L, row.amountPaise)
        assertEquals(TransactionType.DEBIT, row.type)
        assertEquals(5_000L, row.occurredAt)
        assertEquals(5_000L, row.createdAt)
        assertEquals(DetectionStatus.PENDING_REVIEW, row.status)
        assertEquals(DedupKey.of(sender, body, 1_000L), row.dedupKey)
        assertFalse(row.dedupKey.contains("debited", ignoreCase = true))
    }

    @Test
    fun redeliveredSmsIsNotStoredTwice() = runBlocking {
        assertTrue(repository.recordSms(sender, body, 1_000L, 5_000L))
        assertFalse(repository.recordSms(sender, body, 1_000L, 6_000L))
        assertEquals(1, dao.rows.value.size)
    }

    @Test
    fun sameTextAtAnotherTimeIsANewTransaction() = runBlocking {
        assertTrue(repository.recordSms(sender, body, 1_000L, 5_000L))
        assertTrue(repository.recordSms(sender, body, 2_000L, 6_000L))
        assertEquals(2, dao.rows.value.size)
    }

    @Test
    fun nonBankSmsIsNotStored() = runBlocking {
        assertFalse(repository.recordSms("+910000000000", body, 1_000L, 5_000L))
        assertFalse(repository.recordSms(sender, "Your parcel is out for delivery", 1_000L, 5_000L))
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test
    fun purgeKeepsThirtyDays() = runBlocking {
        val now = TimeUnit.DAYS.toMillis(100)
        repository.purgeStale(now)
        assertEquals(now - TimeUnit.DAYS.toMillis(30), dao.lastPurgeCutoff)
    }

    @Test
    fun dedupKeyIsSha256Hex() {
        val key = DedupKey.of(sender, body, 1_000L)
        assertEquals(64, key.length)
        assertTrue(key.all { it in '0'..'9' || it in 'a'..'f' })
        assertNotEquals(key, DedupKey.of(sender, body, 1_001L))
    }
}
