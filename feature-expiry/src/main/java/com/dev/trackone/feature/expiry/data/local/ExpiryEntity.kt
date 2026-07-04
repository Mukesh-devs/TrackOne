package com.dev.trackone.feature.expiry.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an expiry item.
 * Stores subscription/membership/coupon/license/warranty data.
 */
@Entity(tableName = "expiry_items")
data class ExpiryEntity(
    @PrimaryKey(autoGenerate = true)
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
