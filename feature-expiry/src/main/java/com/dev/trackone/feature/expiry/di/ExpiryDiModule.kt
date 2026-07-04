package com.dev.trackone.feature.expiry.di

import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.expiry.ExpiryModuleEntry
import com.dev.trackone.feature.expiry.data.repository.ExpiryRepositoryImpl
import com.dev.trackone.feature.expiry.domain.repository.ExpiryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExpiryDiModule {

    @Binds
    @IntoSet
    abstract fun bindExpiryModuleEntry(entry: ExpiryModuleEntry): ModuleEntry

    @Binds
    @Singleton
    abstract fun bindExpiryRepository(impl: ExpiryRepositoryImpl): ExpiryRepository
}
