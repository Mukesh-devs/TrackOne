package com.dev.trackone.feature.expense.sms

import android.content.Context
import android.util.Log
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionsDatabase
import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-off housekeeping when the user opens the app — not on a schedule, and
 * not when the process is only woken for an SMS.
 */
@Singleton
class SmsDetectionStartup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val controller: SmsDetectionController,
    private val repository: DetectedTransactionRepository,
) {

    private val started = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Safe to call from every Activity.onCreate; runs once per process. */
    fun onAppStart() {
        if (!started.compareAndSet(false, true)) return

        // Revoking a runtime permission kills the process, so this catches it.
        controller.syncWithPermission()

        // Never used the feature: no database to open.
        if (!context.getDatabasePath(DetectedTransactionsDatabase.NAME).exists()) return
        scope.launch {
            try {
                repository.purgeStale(System.currentTimeMillis())
            } catch (e: Exception) {
                Log.w(TAG, "startup maintenance failed: ${e.javaClass.simpleName}")
            }
        }
    }

    private companion object {
        const val TAG = "SmsDetectionStartup"
    }
}
