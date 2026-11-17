package com.dev.trackone.feature.expense.sms.di

import com.dev.trackone.feature.expense.sms.domain.repository.DetectedTransactionRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dependencies for the classes the system instantiates itself. Looked up only
 * once an SMS has passed the cheap sender and keyword checks, so a non-bank
 * message never touches the dependency graph.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface SmsDetectionEntryPoint {
    fun detectedTransactionRepository(): DetectedTransactionRepository
}
