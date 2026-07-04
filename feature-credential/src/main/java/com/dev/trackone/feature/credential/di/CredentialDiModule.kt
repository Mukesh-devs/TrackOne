package com.dev.trackone.feature.credential.di

import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.credential.CredentialModuleEntry
import com.dev.trackone.feature.credential.data.repository.CredentialRepositoryImpl
import com.dev.trackone.feature.credential.domain.repository.CredentialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CredentialDiModule {

    @Binds
    @IntoSet
    abstract fun bindCredentialModuleEntry(entry: CredentialModuleEntry): ModuleEntry

    @Binds
    @Singleton
    abstract fun bindCredentialRepository(impl: CredentialRepositoryImpl): CredentialRepository
}
