package com.dev.trackone.di

import android.content.Context
import com.dev.trackone.feature.credential.data.local.CredentialDao
import com.dev.trackone.feature.expense.data.local.ExpenseDao
import com.dev.trackone.feature.expiry.data.local.ExpiryDao
import com.dev.trackone.sync.GoogleAuthHelper
import com.dev.trackone.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideGoogleAuthHelper(
        @ApplicationContext context: Context
    ): GoogleAuthHelper {
        return GoogleAuthHelper(context)
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        expiryDao: ExpiryDao,
        expenseDao: ExpenseDao,
        credentialDao: CredentialDao
    ): SyncManager {
        return SyncManager(context, expiryDao, expenseDao, credentialDao)
    }
}
