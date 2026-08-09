package com.dev.trackone.feature.expiry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.feature.expiry.domain.model.ExpiryGroup
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem
import com.dev.trackone.feature.expiry.domain.repository.ExpiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpiryViewModel @Inject constructor(
    private val repository: ExpiryRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefs = context.getSharedPreferences("expiry_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ExpiryUiState())
    val uiState: StateFlow<ExpiryUiState> = _uiState.asStateFlow()

    private val _dismissedGroups = MutableStateFlow<Set<String>>(
        prefs.getStringSet("dismissed_groups", emptySet()) ?: emptySet()
    )

    private val _formState = MutableStateFlow(ExpiryFormState())
    val formState: StateFlow<ExpiryFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAll(),
                repository.getExpired(),
                repository.getAllProviders(),
                _dismissedGroups,
            ) { all, expired, providers, dismissed ->
                val groups = all.groupBy { it.provider }
                    .map { (provider, items) -> ExpiryGroup(name = provider, items = items) }
                    .filter { group ->
                        val hasActive = group.nearestExpiry != null
                        if (hasActive) true else group.name !in dismissed
                    }
                    .sortedBy { group -> group.items.minOf { it.expiryDate } }

                _uiState.update { state ->
                    val updatedSelectedGroup = state.selectedGroup?.let { currentSelected ->
                        groups.find { it.name == currentSelected.name }
                    }
                    state.copy(
                        isLoading = false,
                        allItems = all,
                        groups = groups,
                        expiredItems = expired,
                        providers = providers,
                        error = null,
                        selectedGroup = updatedSelectedGroup
                    )
                }
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
                .collect {}
        }
    }

    // ========== Search ==========

    fun onSearchQueryChange(query: String) {
        // Home filters the flat item list locally from [uiState.allItems], so the
        // ViewModel just needs to hold the current query. (No extra DB collector.)
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleSearch() {
        _uiState.update {
            val newActive = !it.isSearchActive
            if (!newActive) {
                loadData()
            }
            it.copy(isSearchActive = newActive, searchQuery = if (!newActive) "" else it.searchQuery)
        }
    }

    // ========== Selection ==========

    fun refresh() {
        // Data is Flow-backed and always live; this just drives the pull indicator.
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadData()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun selectGroup(group: ExpiryGroup) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun clearGroupSelection() {
        _uiState.update { it.copy(selectedGroup = null) }
    }

    fun selectItem(item: ExpiryItem) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun clearItemSelection() {
        _uiState.update { it.copy(selectedItem = null) }
    }

    // ========== CRUD ==========

    fun dismissGroupFromHome(groupName: String) {
        _dismissedGroups.update { current ->
            val newSet = current + groupName
            prefs.edit().putStringSet("dismissed_groups", newSet).apply()
            newSet
        }
    }

    fun deleteItem(item: ExpiryItem) {
        viewModelScope.launch {
            repository.delete(item)
            // Update selected group if it was showing this item
            _uiState.update { state ->
                val updatedGroup = state.selectedGroup?.let { group ->
                    val updatedItems = group.items.filter { it.id != item.id }
                    if (updatedItems.isEmpty()) null
                    else group.copy(items = updatedItems)
                }
                state.copy(selectedGroup = updatedGroup, selectedItem = null)
            }
        }
    }

    // ========== Form ==========

    fun initAddForm() {
        _formState.value = ExpiryFormState()
    }

    fun initEditForm(item: ExpiryItem) {
        _formState.value = ExpiryFormState(
            id = item.id,
            name = item.name,
            provider = item.provider,
            label = item.label,
            purchasedDate = item.purchasedDate,
            expiryDate = item.expiryDate,
            notes = item.notes ?: "",
            amount = item.amount?.toString() ?: "",
            username = item.username ?: "",
            email = item.email ?: "",
            password = item.password ?: "",
            isEditing = true,
        )
    }

    fun updateFormName(value: String) {
        _formState.update { it.copy(name = value, nameError = null) }
    }

    fun updateFormProvider(value: String) {
        _formState.update { it.copy(provider = value, providerError = null) }
    }

    fun updateFormLabel(value: String) {
        _formState.update { it.copy(label = value) }
    }

    fun updateFormPurchasedDate(value: Long?) {
        _formState.update { it.copy(purchasedDate = value, purchasedDateError = null) }
    }

    fun updateFormExpiryDate(value: Long?) {
        _formState.update { it.copy(expiryDate = value, expiryDateError = null) }
    }

    fun updateFormNotes(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun updateFormAmount(value: String) {
        _formState.update { it.copy(amount = value) }
    }

    fun updateFormUsername(value: String) {
        _formState.update { it.copy(username = value) }
    }

    fun updateFormEmail(value: String) {
        _formState.update { it.copy(email = value) }
    }

    fun updateFormPassword(value: String) {
        _formState.update { it.copy(password = value) }
    }

    fun saveForm() {
        val form = _formState.value

        // Validate
        var hasError = false
        if (form.name.isBlank()) {
            _formState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (form.provider.isBlank()) {
            _formState.update { it.copy(providerError = "Provider is required") }
            hasError = true
        }
        if (form.purchasedDate == null) {
            _formState.update { it.copy(purchasedDateError = "Purchase date is required") }
            hasError = true
        }
        if (form.expiryDate == null) {
            _formState.update { it.copy(expiryDateError = "Expiry date is required") }
            hasError = true
        }
        if (hasError) return

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val item = ExpiryItem(
                    id = if (form.isEditing) form.id else 0,
                    name = form.name.trim(),
                    provider = form.provider.trim(),
                    label = form.label.trim(),
                    purchasedDate = form.purchasedDate!!,
                    expiryDate = form.expiryDate!!,
                    notes = form.notes.ifBlank { null },
                    amount = form.amount.toDoubleOrNull(),
                    username = form.username.ifBlank { null },
                    email = form.email.ifBlank { null },
                    password = form.password.ifBlank { null },
                )
                if (form.isEditing) {
                    repository.update(item)
                } else {
                    repository.insert(item)
                }
                _formState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}
