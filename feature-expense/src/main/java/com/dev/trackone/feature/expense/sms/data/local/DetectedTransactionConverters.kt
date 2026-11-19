package com.dev.trackone.feature.expense.sms.data.local

import androidx.room.TypeConverter
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType

/**
 * Stores the enums as their names. The status literals in
 * [DetectedTransactionDao]'s queries rely on exactly these values.
 */
class DetectedTransactionConverters {

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromDetectionStatus(status: DetectionStatus): String = status.name

    @TypeConverter
    fun toDetectionStatus(value: String): DetectionStatus = DetectionStatus.valueOf(value)
}
