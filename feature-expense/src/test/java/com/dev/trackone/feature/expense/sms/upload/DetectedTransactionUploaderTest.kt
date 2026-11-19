package com.dev.trackone.feature.expense.sms.upload

import com.dev.trackone.core.network.api.ApiErrorException
import com.dev.trackone.core.network.api.NoConnectivityException
import com.dev.trackone.core.network.api.UnauthorizedException
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import com.dev.trackone.feature.expense.sms.testing.FakeDetectedTransactionDao
import com.dev.trackone.feature.expense.sms.testing.FakeExpenseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectedTransactionUploaderTest {

    private val dao = FakeDetectedTransactionDao()
    private val expenses = FakeExpenseRepository()
    private val uploader = DetectedTransactionUploader(dao, expenses)

    private suspend fun seed(
        type: TransactionType = TransactionType.DEBIT,
        status: DetectionStatus = DetectionStatus.ACCEPTED_UNSYNCED,
    ): Long = dao.insert(
        DetectedTransactionEntity(
            name = "Test Shop",
            amountPaise = 149_950,
            type = type,
            occurredAt = 1_763_400_000_000L,
            status = status,
            dedupKey = "key",
            createdAt = 1_763_400_000_123L,
        ),
    )

    @Test
    fun successSendsOneCreateAndDeletesRow() = runBlocking {
        val id = seed()

        assertEquals(UploadResult.DONE, uploader.upload(id))

        val sent = expenses.inserts.single()
        assertEquals("Test Shop", sent.item.title)
        assertEquals(1_499.50, sent.item.amount, 0.0)
        assertEquals(false, sent.item.isIncome)
        assertEquals(ExpenseCategory.OTHER_EXPENSE, sent.item.category)
        assertEquals(PaymentMode.OTHERS, sent.item.paymentMode)
        assertEquals(1_763_400_000_000L, sent.item.date)
        assertNull(sent.item.notes)
        assertEquals("sms-$id-1763400000123", sent.idempotencyKey)
        assertNull(dao.getById(id))
    }

    @Test
    fun creditIsSentAsIncome() = runBlocking {
        val id = seed(type = TransactionType.CREDIT)

        uploader.upload(id)

        val sent = expenses.inserts.single().item
        assertEquals(true, sent.isIncome)
        assertEquals(ExpenseCategory.OTHER_INCOME, sent.category)
    }

    @Test
    fun clientErrorDoesNotRetryAndKeepsRow() = runBlocking {
        val id = seed()
        expenses.failWith = ApiErrorException(422, "validation_error", "Some fields are invalid.")

        assertEquals(UploadResult.GIVE_UP, uploader.upload(id))
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
    }

    @Test
    fun signedOutDoesNotRetryAndKeepsRow() = runBlocking {
        val id = seed()
        expenses.failWith = UnauthorizedException()

        assertEquals(UploadResult.GIVE_UP, uploader.upload(id))
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
    }

    @Test
    fun serverErrorsRetry() = runBlocking {
        val id = seed()
        for (code in listOf(500, 503, 408, 429)) {
            expenses.failWith = ApiErrorException(code, "http_$code", "error")
            assertEquals("HTTP $code", UploadResult.RETRY, uploader.upload(id))
        }
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
    }

    @Test
    fun noConnectivityRetries() = runBlocking {
        val id = seed()
        expenses.failWith = NoConnectivityException()

        assertEquals(UploadResult.RETRY, uploader.upload(id))
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
    }

    @Test
    fun pendingRowIsNeverUploaded() = runBlocking {
        val id = seed(status = DetectionStatus.PENDING_REVIEW)

        assertEquals(UploadResult.DONE, uploader.upload(id))
        assertTrue(expenses.inserts.isEmpty())
        assertEquals(DetectionStatus.PENDING_REVIEW, dao.getById(id)?.status)
    }

    @Test
    fun missingRowIsDone() = runBlocking {
        assertEquals(UploadResult.DONE, uploader.upload(42L))
        assertTrue(expenses.inserts.isEmpty())
    }
}
