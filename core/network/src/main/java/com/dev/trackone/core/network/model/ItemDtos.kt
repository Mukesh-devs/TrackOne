package com.dev.trackone.core.network.model

import com.squareup.moshi.JsonClass

/*
 * Wire models for the three tracked entities.
 *
 * Field names and types match the Kotlin domain models exactly, so no
 * @Json(name = …) renaming is needed. Server-assigned fields (id, createdAt,
 * updatedAt) live only on the *response* DTOs — request bodies deliberately
 * leave them out so a POST can never try to dictate an id and a PUT can never
 * rewind a timestamp.
 *
 * Request bodies are serialised with nulls included (see NetworkModule), which
 * is what lets "clear the notes field" actually clear it server-side rather
 * than being silently dropped from a partial update.
 */

// ---------------------------------------------------------------- expiry

@JsonClass(generateAdapter = true)
data class ExpiryItemDto(
    val id: Long = 0L,
    val name: String = "",
    val provider: String = "",
    val label: String = "",
    val purchasedDate: Long = 0L,
    val expiryDate: Long = 0L,
    val notes: String? = null,
    val amount: Double? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class ExpiryItemRequest(
    val name: String,
    val provider: String,
    val label: String,
    val purchasedDate: Long,
    val expiryDate: Long,
    val notes: String?,
    val amount: Double?,
    val username: String?,
    val email: String?,
    val password: String?,
)

// --------------------------------------------------------------- expense

@JsonClass(generateAdapter = true)
data class ExpenseItemDto(
    val id: Long = 0L,
    val title: String = "",
    val amount: Double = 0.0,
    val isIncome: Boolean = false,
    val category: String = "",
    val paymentMode: String = "CASH",
    val date: Long = 0L,
    val notes: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class ExpenseItemRequest(
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val paymentMode: String,
    val date: Long,
    val notes: String?,
)

// ------------------------------------------------------------ credential

@JsonClass(generateAdapter = true)
data class CredentialItemDto(
    val id: Long = 0L,
    val title: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val url: String = "",
    val category: String = "",
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class CredentialItemRequest(
    val title: String,
    val username: String,
    val email: String,
    val password: String,
    val url: String,
    val category: String,
    val notes: String?,
    val isFavorite: Boolean,
)

@JsonClass(generateAdapter = true)
data class FavoriteRequest(val isFavorite: Boolean)
