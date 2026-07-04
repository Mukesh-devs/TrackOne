package com.dev.trackone.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
 */
@Database(
    entities = [
        ExpiryEntity::class,
        ExpenseEntity::class,
        CredentialEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expiryDao(): ExpiryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun credentialDao(): CredentialDao
}
