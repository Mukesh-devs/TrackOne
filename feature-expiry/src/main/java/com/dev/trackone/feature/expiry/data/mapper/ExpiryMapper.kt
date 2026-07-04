package com.dev.trackone.feature.expiry.data.mapper

import com.dev.trackone.feature.expiry.data.local.ExpiryEntity
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

/**
 * Mappers between [ExpiryEntity] (data layer) and [ExpiryItem] (domain layer).
 */

fun ExpiryEntity.toDomain(): ExpiryItem = ExpiryItem(
    id = id,
    name = name,
    provider = provider,
    label = label,
    purchasedDate = purchasedDate,
    expiryDate = expiryDate,
    notes = notes,
    amount = amount,
    username = username,
    email = email,
    password = password,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ExpiryItem.toEntity(): ExpiryEntity = ExpiryEntity(
    id = id,
    name = name,
    provider = provider,
    label = label,
    purchasedDate = purchasedDate,
    expiryDate = expiryDate,
    notes = notes,
    amount = amount,
    username = username,
    email = email,
    password = password,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
