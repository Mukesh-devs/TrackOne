package com.dev.trackone.feature.expense.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.dev.trackone.feature.expense.sms.di.SmsDetectionEntryPoint
import com.dev.trackone.feature.expense.sms.parser.BankSenderRegistry
import com.dev.trackone.feature.expense.sms.parser.BankSmsParser
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Receives new SMS while "Auto-detect bank transactions" is on.
 *
 * Checks run cheapest first and return at the first miss, so a non-bank
 * message costs one regex match and a set lookup. No network, notifications
 * or UI here, and nothing from the message is ever logged — only generic
 * events with no content.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val receivedAt = System.currentTimeMillis()

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)?.filterNotNull().orEmpty()
        // Multi-part messages arrive as several parts from one sender.
        val candidates = parts
            .groupBy { it.originatingAddress.orEmpty() }
            .mapNotNull { (sender, senderParts) ->
                if (!BankSenderRegistry.isBankSender(sender)) return@mapNotNull null
                val body = senderParts.joinToString(separator = "") { it.messageBody.orEmpty() }
                if (!BankSmsParser.looksLikeTransaction(body)) return@mapNotNull null
                Candidate(sender, body, senderParts.first().timestampMillis)
            }
        if (candidates.isEmpty()) return

        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, SmsDetectionEntryPoint::class.java)
            .detectedTransactionRepository()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(PROCESSING_TIMEOUT_MS) {
                    for (sms in candidates) {
                        val saved = repository.recordSms(sms.sender, sms.body, sms.timestampMillis, receivedAt)
                        Log.d(TAG, if (saved) "txn saved" else "sms ignored")
                    }
                }
            } catch (e: Exception) {
                // Class name only: a message could echo content.
                Log.w(TAG, "sms processing failed: ${e.javaClass.simpleName}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private class Candidate(val sender: String, val body: String, val timestampMillis: Long)

    private companion object {
        const val TAG = "SmsReceiver"

        /** Well inside the ~10 s the system allows a goAsync() receiver. */
        const val PROCESSING_TIMEOUT_MS = 8_000L
    }
}
