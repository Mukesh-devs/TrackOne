package com.dev.trackone.feature.credential.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved credential.
 * Passwords can optionally be stored encrypted depending on future implementation.
 */
@Entity(tableName = "credential_items")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val username: String,
    val email: String,
    val password: String, // Store plaintext or encrypted based on requirements
    val url: String = "",
    val category: String = "",
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
