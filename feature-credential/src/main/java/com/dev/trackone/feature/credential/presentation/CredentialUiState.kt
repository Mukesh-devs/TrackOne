package com.dev.trackone.feature.credential.presentation

import com.dev.trackone.feature.credential.domain.model.CredentialItem

/**
 * UI state for the Credential module screens.
 */
data class CredentialUiState(
    val isLoading: Boolean = true,
    val items: List<CredentialItem> = emptyList(),
    val favoriteItems: List<CredentialItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val error: String? = null,
)

/**
 * State for the Add/Edit form.
 */
data class CredentialFormState(
    val id: Long = 0,
    val title: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val url: String = "",
    val category: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isEditing: Boolean = false,
    val titleError: String? = null,
    val passwordError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
)
