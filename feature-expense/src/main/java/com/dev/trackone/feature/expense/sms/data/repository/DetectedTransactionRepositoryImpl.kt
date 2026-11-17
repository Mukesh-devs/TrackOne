package com.dev.trackone.feature.expense.sms.data.repository

import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionDao
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.data.mapper.toDomain
import com.dev.trackone.feature.expense.sms.domain.model.DetectedTransaction
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import com.dev.trackone.feature.expense.sms.parser.BankSmsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectedTransactionRepositoryImpl @Inject constructor(
    private val dao: DetectedTransactionDao,
) : DetectedTransactionRepository {

    override suspend fun recordSms(
        sender: String,
        body: String,
        smsTimestampMillis: Long,
        receivedAtMillis: Long,
    ): Boolean {
        // Parse in memory; only the extracted fields and a hash outlive this call.
        val entity = withContext(Dispatchers.Default) {
            val parsed = BankSmsParser.parse(sender, body, receivedAtMillis) ?: return@withContext null
            DetectedTransactionEntity(
                name = parsed.name,
                amountPaise = parsed.amountPaise,
                type = parsed.type,
                occurredAt = parsed.occurredAt,
                status = DetectionStatus.PENDING_REVIEW,
                dedupKey = DedupKey.of(sender, body, smsTimestampMillis),
                createdAt = receivedAtMillis,
            )
        } ?: return false
        return dao.insert(entity) != DUPLICATE_ROW_ID
    }

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override fun observePending(): Flow<List<DetectedTransaction>> =
        dao.observePending().map { rows -> rows.map { it.toDomain() } }

    override suspend fun accept(id: Long) {
        dao.accept(id)
    }

    override suspend fun decline(id: Long) {
        dao.decline(id)
    }

    override suspend fun purgeStale(nowMillis: Long) {
        dao.purgeOlderThan(nowMillis - PENDING_RETENTION_MS)
    }

    private companion object {
        /** What an IGNORE insert returns when the unique dedup key already exists. */
        const val DUPLICATE_ROW_ID = -1L
        val PENDING_RETENTION_MS = TimeUnit.DAYS.toMillis(30)
    }
}
