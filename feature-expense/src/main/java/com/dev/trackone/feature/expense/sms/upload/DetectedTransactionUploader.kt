package com.dev.trackone.feature.expense.sms.upload

import android.util.Log
import com.dev.trackone.core.network.api.ApiErrorException
import com.dev.trackone.core.network.api.NoConnectivityException
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.domain.model.PaymentMode
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionDao
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionEntity
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import kotlinx.coroutines.CancellationException
import java.math.BigDecimal
import javax.inject.Inject

/** What the worker should do next. */
enum class UploadResult {
    /** Uploaded (or nothing left to upload): the work is finished. */
    DONE,

    /** A transient failure — try again later. */
    RETRY,

    /** The server rejected it; retrying will not help. The row stays for the next app start. */
    GIVE_UP,
}

/**
 * Sends one accepted transaction through the app's existing create-expense
 * call, then deletes the local row: from then on the backend is the source of
 * truth and the device keeps nothing.
 */
class DetectedTransactionUploader @Inject constructor(
    private val dao: DetectedTransactionDao,
    private val expenseRepository: ExpenseRepository,
) {

    suspend fun upload(id: Long): UploadResult {
        val row = dao.getById(id) ?: return UploadResult.DONE
        // Never upload anything the user has not accepted.
        if (row.status != DetectionStatus.ACCEPTED_UNSYNCED) return UploadResult.DONE

        return try {
            expenseRepository.insert(row.toExpenseItem(), idempotencyKey = row.idempotencyKey())
            dao.deleteSynced(id)
            UploadResult.DONE
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoConnectivityException) {
            UploadResult.RETRY
        } catch (e: ApiErrorException) {
            if (e.httpCode.isRetryable()) {
                UploadResult.RETRY
            } else {
                // Status code only — the body could echo the payload.
                Log.w(TAG, "upload rejected: HTTP ${e.httpCode}")
                UploadResult.GIVE_UP
            }
        } catch (e: Exception) {
            // Signed out (401), no server configured, or a local write failure.
            Log.w(TAG, "upload failed: ${e.javaClass.simpleName}")
            UploadResult.GIVE_UP
        }
    }

    private fun Int.isRetryable(): Boolean = this == 408 || this == 429 || this in 500..599

    private companion object {
        const val TAG = "DetectedTxnUpload"
    }
}

/**
 * Only name, amount, type and date-time come from the SMS. The API also needs
 * a category and payment mode, so those are the neutral "other" values the
 * user can change later.
 */
internal fun DetectedTransactionEntity.toExpenseItem(): ExpenseItem {
    val isIncome = type == TransactionType.CREDIT
    return ExpenseItem(
        title = name,
        amount = BigDecimal.valueOf(amountPaise, 2).toDouble(),
        isIncome = isIncome,
        category = if (isIncome) ExpenseCategory.OTHER_INCOME else ExpenseCategory.OTHER_EXPENSE,
        paymentMode = PaymentMode.OTHERS,
        date = occurredAt,
        notes = null,
    )
}

/**
 * Stable across retries of the same row, and unique across reinstalls (ids
 * restart from 1 in a fresh database, the creation time does not repeat).
 */
internal fun DetectedTransactionEntity.idempotencyKey(): String = "sms-$id-$createdAt"
