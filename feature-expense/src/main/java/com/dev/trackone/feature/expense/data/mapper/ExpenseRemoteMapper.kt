package com.dev.trackone.feature.expense.data.mapper

import com.dev.trackone.core.network.model.ExpenseItemDto
import com.dev.trackone.core.network.model.ExpenseItemRequest
import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expense.domain.model.ExpenseItem

/**
 * Mappers between the API wire model and the local layers.
 *
 * `category` and `paymentMode` travel as enum *names* in both directions, which
 * is exactly how they are already stored in Room — no translation table needed.
 */

fun ExpenseItemDto.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    isIncome = isIncome,
    category = category,
    paymentMode = paymentMode,
    date = date,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ExpenseItem.toRequest(): ExpenseItemRequest = ExpenseItemRequest(
    title = title,
    amount = amount,
    isIncome = isIncome,
    category = category.name,
    paymentMode = paymentMode.name,
    date = date,
    notes = notes,
)
