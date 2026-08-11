package com.dev.trackone.core.network.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImportCountsDto(
    val expiry: Int = 0,
    val expense: Int = 0,
    val credential: Int = 0,
)

@JsonClass(generateAdapter = true)
data class RestoreResultDto(
    val mode: String = "",
    val imported: ImportCountsDto = ImportCountsDto(),
    val restoredAt: Long = 0L,
)
