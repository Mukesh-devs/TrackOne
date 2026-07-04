package com.dev.trackone.core.common.model

/**
 * Defines sort ordering used across all modules.
 */
enum class SortOrder {
    ASCENDING,
    DESCENDING
}

/**
 * Defines common sort fields that multiple modules can share.
 */
enum class SortField {
    NAME,
    DATE,
    AMOUNT,
    CREATED_AT,
    UPDATED_AT
}

/**
 * Combines a sort field with its direction.
 */
data class SortConfig(
    val field: SortField = SortField.DATE,
    val order: SortOrder = SortOrder.DESCENDING
)
