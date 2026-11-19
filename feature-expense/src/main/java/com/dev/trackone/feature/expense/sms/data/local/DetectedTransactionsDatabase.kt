package com.dev.trackone.feature.expense.sms.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Local-only database for transactions detected in bank SMS.
 *
 * Deliberately separate from the app's main `AppDatabase`:
 * - backup rules work per file, so only a separate file can be kept out of
 *   cloud backup and device transfer (see `res/xml/data_extraction_rules.xml`);
 * - it holds nothing but pending suggestions, so it is encrypted on its own
 *   (SQLCipher, see `EncryptedOpenHelperFactory`) and simply recreated if its
 *   key is ever lost, without touching the expense cache in the main database.
 *
 * Schemas are exported to feature-expense/schemas/. Any schema change needs a
 * real Migration — there is no destructive fallback.
 */
@Database(
    entities = [DetectedTransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DetectedTransactionConverters::class)
abstract class DetectedTransactionsDatabase : RoomDatabase() {

    abstract fun detectedTransactionDao(): DetectedTransactionDao

    companion object {
        /** Also named in the app's backup exclusion rules — keep them in sync. */
        const val NAME = "detected_transactions.db"
    }
}
