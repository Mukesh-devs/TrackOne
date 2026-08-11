package com.dev.trackone.core.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CredentialsBody(
    val email: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class AccountUserDto(
    val id: Long = 0L,
    val email: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

/**
 * Returned by both `/auth/signup` and `/auth/signin`.
 *
 * `expiresIn` is seconds (JWT convention); `expiresAt` is epoch millis, like
 * every other timestamp in this API.
 */
@JsonClass(generateAdapter = true)
data class SessionDto(
    val token: String = "",
    val tokenType: String = "Bearer",
    val expiresIn: Long = 0L,
    val expiresAt: Long = 0L,
    val user: AccountUserDto = AccountUserDto(),
)
