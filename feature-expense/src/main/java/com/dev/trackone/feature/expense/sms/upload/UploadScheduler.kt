package com.dev.trackone.feature.expense.sms.upload

/** Queues the upload of an accepted detected transaction. */
interface UploadScheduler {

    /** Idempotent per [transactionId]: queuing an id that is already queued does nothing. */
    fun enqueue(transactionId: Long)
}
