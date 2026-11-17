package com.dev.trackone.feature.expense.sms.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Transactions detected in bank SMS. Everything stays in the local database
 * until the user accepts an item.
 */
interface DetectedTransactionRepository {

    /**
     * Parses one incoming SMS and stores it for review if it is a bank credit
     * or debit. Returns true when a new row was saved; false for anything that
     * is not a transaction and for a duplicate delivery of the same SMS.
     */
    suspend fun recordSms(
        sender: String,
        body: String,
        smsTimestampMillis: Long,
        receivedAtMillis: Long,
    ): Boolean

    /** Live number of items waiting for review — drives the badge. */
    fun observePendingCount(): Flow<Int>

    /** Deletes suggestions that have waited for review longer than the retention period. */
    suspend fun purgeStale(nowMillis: Long)
}
