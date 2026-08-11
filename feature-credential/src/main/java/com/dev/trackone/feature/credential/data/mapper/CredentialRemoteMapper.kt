package com.dev.trackone.feature.credential.data.mapper

import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.core.network.model.CredentialItemRequest
import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.credential.domain.model.CredentialItem

/**
 * Mappers between the API wire model and the local layers.
 *
 * List responses mask the password as `********` unless `reveal=true` is asked
 * for, so the repository always requests the real value — a cache full of
 * asterisks would be worse than no cache at all.
 */

fun CredentialItemDto.toEntity(): CredentialEntity = CredentialEntity(
    id = id,
    title = title,
    username = username,
    email = email,
    password = password,
    url = url,
    category = category,
    notes = notes,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CredentialItem.toRequest(): CredentialItemRequest = CredentialItemRequest(
    title = title,
    username = username,
    email = email,
    password = password,
    url = url,
    category = category,
    notes = notes,
    isFavorite = isFavorite,
)
