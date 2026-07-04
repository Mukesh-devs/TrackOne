package com.dev.trackone.di

import android.content.Context
import androidx.room.Room
import com.dev.trackone.core.common.util.Constants
import com.dev.trackone.database.AppDatabase
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and all feature DAOs.
 * When adding a new feature module, add a @Provides method for its DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideExpiryDao(database: AppDatabase): ExpiryDao {
        return database.expiryDao()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideCredentialDao(database: AppDatabase): CredentialDao {
        return database.credentialDao()
    }
}
