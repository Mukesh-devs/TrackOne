package com.dev.trackone.feature.expense.sms.di

import android.content.Context
import androidx.room.Room
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionDao
import com.dev.trackone.feature.expense.sms.data.local.DetectedTransactionsDatabase
import com.dev.trackone.feature.expense.sms.data.local.security.DatabaseKeyStore
import com.dev.trackone.feature.expense.sms.data.local.security.EncryptedOpenHelperFactory
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
        keyStore: DatabaseKeyStore,
    ): DetectedTransactionsDatabase = Room.databaseBuilder(
        context,
        DetectedTransactionsDatabase::class.java,
        DetectedTransactionsDatabase.NAME,
    )
        // Encrypted at rest with SQLCipher; see EncryptedOpenHelperFactory.
        .openHelperFactory(EncryptedOpenHelperFactory(context, keyStore))
        .build()

    @Provides
    fun provideDetectedTransactionDao(database: DetectedTransactionsDatabase): DetectedTransactionDao =
        database.detectedTransactionDao()
}
