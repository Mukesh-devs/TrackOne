package com.dev.trackone.core.network.model

import com.squareup.moshi.JsonClass

/**
 * The `{ success, data, error, meta }` wrapper every TrackOne API response uses.
 *
 * See `docs/API.md` → "Response envelope".
 */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: ApiErrorBody? = null,
    val meta: PageMeta? = null,
)

@JsonClass(generateAdapter = true)
data class ApiErrorBody(
    val code: String = "unknown_error",
    val message: String = "Something went wrong",
)

@JsonClass(generateAdapter = true)
data class PageMeta(
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
    val count: Int = 0,
    val hasMore: Boolean = false,
)
