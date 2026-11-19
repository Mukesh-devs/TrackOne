package com.dev.trackone.feature.expense.sms.testing

import com.dev.trackone.feature.expense.sms.upload.UploadScheduler

class FakeUploadScheduler : UploadScheduler {

    val enqueued = mutableListOf<Long>()

    override fun enqueue(transactionId: Long) {
        enqueued += transactionId
    }
}
