package com.dev.trackone.feature.credential.domain.model

/**
 * Domain model for a single credential.
 */
data class CredentialItem(
    val id: Long = 0,
    val title: String,
    val username: String,
    val email: String,
    val password: String,
    val url: String = "",
    val category: String = "",
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
