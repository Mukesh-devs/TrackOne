package com.dev.trackone.feature.expiry.domain.model

/**
 * Domain model for a single expiry item.
 */
data class ExpiryItem(
    val id: Long = 0,
    val name: String,
    val provider: String,
    val label: String = "",
    val purchasedDate: Long,
    val expiryDate: Long,
    val notes: String? = null,
    val amount: Double? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * A group of expiry items sharing the same provider.
 * e.g., "Netflix" group containing Netflix Personal, Netflix Family, etc.
 */
data class ExpiryGroup(
    val name: String,
    val items: List<ExpiryItem>,
) {
    val itemCount: Int get() = items.size
    val hasMultiple: Boolean get() = items.size > 1
    val nearestExpiry: Long? get() = items.filter { it.expiryDate >= System.currentTimeMillis() }
        .minByOrNull { it.expiryDate }?.expiryDate
    val totalAmount: Double get() = items.mapNotNull { it.amount }.sum()
    val hasExpiredItems: Boolean get() = items.any { it.expiryDate < System.currentTimeMillis() }
}
