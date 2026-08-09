package com.dev.trackone.feature.expiry.presentation

import com.dev.trackone.feature.expiry.domain.model.ExpiryGroup
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

/**
 * UI state for the Expiry module screens.
 */
data class ExpiryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val groups: List<ExpiryGroup> = emptyList(),
    val expiredItems: List<ExpiryItem> = emptyList(),
    val allItems: List<ExpiryItem> = emptyList(),
    val selectedGroup: ExpiryGroup? = null,
    val selectedItem: ExpiryItem? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val providers: List<String> = emptyList(),
    val selectedProvider: String? = null,
    val showActiveOnly: Boolean = false,
    val showExpiredOnly: Boolean = false,
    /** IDs of items the user swiped to trash (soft-deleted; shown in the Expired tab). */
    val trashedIds: Set<Long> = emptySet(),
    val error: String? = null,
)

/**
 * State for the Add/Edit form.
 */
data class ExpiryFormState(
    val id: Long = 0,
    val name: String = "",
    val provider: String = "",
    val label: String = "",
    val purchasedDate: Long? = null,
    val expiryDate: Long? = null,
    val notes: String = "",
    val amount: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isEditing: Boolean = false,
    val nameError: String? = null,
    val providerError: String? = null,
    val purchasedDateError: String? = null,
    val expiryDateError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
)
