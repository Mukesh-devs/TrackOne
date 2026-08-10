package com.dev.trackone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.dev.trackone.core.database.converter.Converters
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.feature.expiry.data.local.ExpiryEntity

/**
 * Central Room database for TrackOne.
 * All feature module entities and DAOs are registered here.
 *
 * When adding a new feature module:
 * 1. Add its Entity to the entities array
 * 2. Add an abstract DAO getter
 * 3. Increment the version number
 * 4. Add a Migration to [MIGRATIONS] — the build will fail without one, which
 *    is deliberate: it prevents a schema change from wiping user data.
 *
 * Schemas are exported to app/schemas/ (see the `room.schemaLocation` KSP arg in
 * build.gradle.kts) so future migrations can be written and verified against a
 * known baseline.
 */
@Database(
    entities = [
        ExpiryEntity::class,
        ExpenseEntity::class,
        CredentialEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expiryDao(): ExpiryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun credentialDao(): CredentialDao

    companion object {
        /**
         * Migrations from version 4 onward. Empty today because 4 is current —
         * add one entry per schema bump, e.g.:
         *
         *   val MIGRATION_4_5 = object : Migration(4, 5) {
         *       override fun migrate(db: SupportSQLiteDatabase) {
         *           db.execSQL("ALTER TABLE expiry_items ADD COLUMN ...")
         *       }
         *   }
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
