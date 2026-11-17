package com.dev.trackone.feature.expense.sms.di

import com.dev.trackone.feature.expense.sms.data.repository.DetectedTransactionRepositoryImpl
import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsDetectionBindingsModule {

    @Binds
    @Singleton
    abstract fun bindDetectedTransactionRepository(
        impl: DetectedTransactionRepositoryImpl,
    ): DetectedTransactionRepository
}
