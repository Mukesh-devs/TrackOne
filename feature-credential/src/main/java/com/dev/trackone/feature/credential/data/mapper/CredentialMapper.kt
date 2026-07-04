package com.dev.trackone.feature.credential.data.mapper

import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.credential.domain.model.CredentialItem

/**
 * Mappers between [CredentialEntity] (data layer) and [CredentialItem] (domain layer).
 */

fun CredentialEntity.toDomain(): CredentialItem = CredentialItem(
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

fun CredentialItem.toEntity(): CredentialEntity = CredentialEntity(
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
