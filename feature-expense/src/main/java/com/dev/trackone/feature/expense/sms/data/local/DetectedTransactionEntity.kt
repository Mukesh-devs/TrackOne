package com.dev.trackone.feature.expense.sms.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType

/**
 * Room entity for a transaction detected in a bank SMS.
 *
 * [dedupKey] is the SHA-256 of sender, body and SMS timestamp — a one-way hash,
 * so a re-delivered broadcast is rejected by the unique index without the
 * message text ever being stored.
 */
@Entity(
    tableName = "detected_transactions",
    indices = [Index(value = ["dedupKey"], unique = true)],
)
data class DetectedTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Money as paise in a Long — never Float/Double. */
    val amountPaise: Long,
    val type: TransactionType,
    /** Epoch millis (UTC) of when the SMS was received. */
    val occurredAt: Long,
    val status: DetectionStatus,
    val dedupKey: String,
    val createdAt: Long,
)
