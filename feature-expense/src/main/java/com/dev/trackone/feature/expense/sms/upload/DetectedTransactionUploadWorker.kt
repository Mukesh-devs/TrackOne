package com.dev.trackone.feature.expense.sms.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dev.trackone.feature.expense.sms.di.SmsDetectionEntryPoint
import dagger.hilt.android.EntryPointAccessors

/** Thin shell around [DetectedTransactionUploader]; WorkManager supplies the retry policy. */
class DetectedTransactionUploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_TRANSACTION_ID, NO_ID)
        if (id == NO_ID) return Result.failure()

        val uploader = EntryPointAccessors
            .fromApplication(applicationContext, SmsDetectionEntryPoint::class.java)
            .detectedTransactionUploader()

        return when (uploader.upload(id)) {
            UploadResult.DONE -> Result.success()
            UploadResult.RETRY -> Result.retry()
            UploadResult.GIVE_UP -> Result.failure()
        }
    }

    companion object {
        const val KEY_TRANSACTION_ID = "transaction_id"
        private const val NO_ID = -1L

        fun uniqueWorkName(transactionId: Long): String = "detected-transaction-upload-$transactionId"
    }
}
