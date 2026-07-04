package com.dev.trackone.feature.expense.data.mapper

import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem

/**
 * Mappers between [ExpenseEntity] (data layer) and [ExpenseItem] (domain layer).
 */

fun ExpenseEntity.toDomain(): ExpenseItem = ExpenseItem(
    id = id,
    title = title,
    amount = amount,
    isIncome = isIncome,
    category = ExpenseCategory.fromString(category),
    date = date,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ExpenseItem.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    isIncome = isIncome,
    category = category.name,
    date = date,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
