package com.dev.trackone.feature.expense.di

import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.expense.ExpenseModuleEntry
import com.dev.trackone.feature.expense.data.repository.ExpenseRepositoryImpl
import com.dev.trackone.feature.expense.domain.repository.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExpenseDiModule {

    @Binds
    @IntoSet
    abstract fun bindExpenseModuleEntry(entry: ExpenseModuleEntry): ModuleEntry

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
}
