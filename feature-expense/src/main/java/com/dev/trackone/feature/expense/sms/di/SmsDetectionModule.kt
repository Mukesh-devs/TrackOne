package com.dev.trackone.feature.expense.sms.di

import android.content.Context
import androidx.room.Room
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionDao
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmsDetectionModule {

    @Provides
    @Singleton
    fun provideDetectedTransactionsDatabase(
        @ApplicationContext context: Context,
    ): DetectedTransactionsDatabase = Room.databaseBuilder(
        context,
        DetectedTransactionsDatabase::class.java,
        DetectedTransactionsDatabase.NAME,
    ).build()

    @Provides
    fun provideDetectedTransactionDao(database: DetectedTransactionsDatabase): DetectedTransactionDao =
        database.detectedTransactionDao()
}
