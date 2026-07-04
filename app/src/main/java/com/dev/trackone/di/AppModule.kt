package com.dev.trackone.di

import com.dev.trackone.core.navigation.ModuleEntry
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

/**
 * App-level Hilt module.
 * Declares the multibinding set for ModuleEntry so the app compiles
 * even if no feature modules are present (empty set fallback).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Multibinds
    abstract fun bindModuleEntries(): Set<ModuleEntry>
}
