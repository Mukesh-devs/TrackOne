package com.dev.trackone.feature.expense.sms.domain.model

/** Direction of a bank transaction, from the account holder's point of view. */
enum class TransactionType {
    /** Money came in: credited, received, deposited, refund. */
    CREDIT,

    /** Money went out: debited, spent, paid, withdrawn. */
    DEBIT,
}

/**
 * Where a detected transaction is in its life on the device. There is no
 * "declined" or "synced" state: both of those delete the row.
 */
enum class DetectionStatus {
    /** Parsed from an SMS, waiting for the user to accept or decline it. */
    PENDING_REVIEW,

    /** Accepted by the user; the upload to the expense API has not succeeded yet. */
    ACCEPTED_UNSYNCED,
}

/**
 * A bank transaction found in an incoming SMS. Only these fields are ever kept —
 * never the message body, sender, account number, balance or reference.
 */
data class DetectedTransaction(
    val id: Long,
    val name: String,
    val amountPaise: Long,
    val type: TransactionType,
    /** Epoch millis of the moment the SMS was received on this device. */
    val occurredAt: Long,
)
