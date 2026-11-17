package com.dev.trackone.feature.expense.sms.presentation.detected

import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.data.repository.DetectedTransactionRepositoryImpl
import com.dev.trackone.feature.expense.sms.domain.model.DetectedTransaction
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import com.dev.trackone.feature.expense.sms.testing.FakeDetectedTransactionDao
import com.dev.trackone.feature.expense.sms.testing.FakeUploadScheduler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetectedTransactionsViewModelTest {

    private val dao = FakeDetectedTransactionDao()
    private val scheduler = FakeUploadScheduler()
    private val repository = DetectedTransactionRepositoryImpl(dao, scheduler)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun seed(dedupKey: String, occurredAt: Long): Long = dao.insert(
        DetectedTransactionEntity(
            name = "Test Shop",
            amountPaise = 25_000,
            type = TransactionType.DEBIT,
            occurredAt = occurredAt,
            status = DetectionStatus.PENDING_REVIEW,
            dedupKey = dedupKey,
            createdAt = occurredAt,
        ),
    )

    private fun TestScope.observe(viewModel: DetectedTransactionsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.items.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.pendingCount.collect() }
    }

    @Test
    fun listsPendingNewestFirst() = runTest {
        val older = seed("a", occurredAt = 1_000L)
        val newer = seed("b", occurredAt = 2_000L)
        val viewModel = DetectedTransactionsViewModel(repository)
        observe(viewModel)

        assertEquals(listOf(newer, older), viewModel.items.value.map { it.id })
        assertEquals(2, viewModel.pendingCount.value)
    }

    @Test
    fun acceptRemovesRowAndQueuesUpload() = runTest {
        val id = seed("a", occurredAt = 1_000L)
        val viewModel = DetectedTransactionsViewModel(repository)
        observe(viewModel)

        viewModel.accept(id)

        assertTrue(viewModel.items.value.isEmpty())
        assertEquals(0, viewModel.pendingCount.value)
        assertEquals(DetectionStatus.ACCEPTED_UNSYNCED, dao.getById(id)?.status)
        assertEquals(listOf(id), scheduler.enqueued)
    }

    @Test
    fun declineRemovesRowAndDeletesIt() = runTest {
        val id = seed("a", occurredAt = 1_000L)
        val viewModel = DetectedTransactionsViewModel(repository)
        observe(viewModel)

        viewModel.decline(id)

        assertTrue(viewModel.items.value.isEmpty())
        assertNull(dao.getById(id))
        assertTrue(scheduler.enqueued.isEmpty())
    }

    @Test
    fun rowLeavesListBeforeWriteFinishesAndReturnsIfItFails() = runTest {
        val id = seed("a", occurredAt = 1_000L)
        val gate = CompletableDeferred<Unit>()
        val slowRepository = object : DetectedTransactionRepository by repository {
            override suspend fun accept(id: Long) {
                gate.await()
                error("disk full")
            }
        }
        val viewModel = DetectedTransactionsViewModel(slowRepository)
        observe(viewModel)

        viewModel.accept(id)
        assertTrue(viewModel.items.value.isEmpty())

        gate.complete(Unit)
        assertEquals(listOf(id), viewModel.items.value.map(DetectedTransaction::id))
    }
}
