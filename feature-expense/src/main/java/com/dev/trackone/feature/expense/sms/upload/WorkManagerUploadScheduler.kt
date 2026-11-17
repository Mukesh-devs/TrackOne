package com.dev.trackone.feature.expense.sms.upload

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-time work per accepted transaction, unique by id, that waits for a
 * network and backs off exponentially. Only the row id goes into the work
 * data — never anything from the SMS.
 */
@Singleton
class WorkManagerUploadScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : UploadScheduler {

    override fun enqueue(transactionId: Long) {
        val request = OneTimeWorkRequestBuilder<DetectedTransactionUploadWorker>()
            .setInputData(workDataOf(DetectedTransactionUploadWorker.KEY_TRANSACTION_ID to transactionId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, INITIAL_BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()

        // KEEP: a second Accept, or the start-up re-queue, never doubles the work.
        WorkManager.getInstance(context).enqueueUniqueWork(
            DetectedTransactionUploadWorker.uniqueWorkName(transactionId),
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val INITIAL_BACKOFF_SECONDS = 30L
    }
}
