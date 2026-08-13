package com.dev.trackone.feature.credential.data.mapper

import com.dev.trackone.core.network.model.CredentialItemDto
import com.dev.trackone.core.network.model.CredentialItemRequest
import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.credential.domain.model.CredentialItem

/**
 * Mappers between the API wire model and the local layers.
 *
 * List responses mask the password as [MASKED_PASSWORD], so a DTO that came
 * from a list has to be mapped with the real value supplied separately — see
 * `CredentialRepositoryImpl.hydratePasswords`. A cache full of asterisks would
 * be worse than no cache at all.
 */

/** What the API sends in place of a password on any list response. */
const val MASKED_PASSWORD = "********"

fun CredentialItemDto.toEntity(password: String = this.password): CredentialEntity = CredentialEntity(
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
