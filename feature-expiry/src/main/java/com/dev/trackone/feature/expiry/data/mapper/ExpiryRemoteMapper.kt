package com.dev.trackone.feature.expiry.data.mapper

import com.dev.trackone.core.network.model.ExpiryItemDto
import com.dev.trackone.core.network.model.ExpiryItemRequest
import com.dev.trackone.feature.expiry.data.local.ExpiryEntity
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

/**
 * Mappers between the API wire model and the local layers.
 *
 * The server owns `id`, `createdAt` and `updatedAt`, so responses are written
 * to Room verbatim and requests deliberately omit those fields.
 */

fun ExpiryItemDto.toEntity(): ExpiryEntity = ExpiryEntity(
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

fun ExpiryItem.toRequest(): ExpiryItemRequest = ExpiryItemRequest(
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
)
