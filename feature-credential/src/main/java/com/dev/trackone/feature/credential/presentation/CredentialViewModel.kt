package com.dev.trackone.feature.credential.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.domain.repository.CredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredentialViewModel @Inject constructor(
    private val repository: CredentialRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CredentialUiState())
    val uiState: StateFlow<CredentialUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CredentialFormState())
    val formState: StateFlow<CredentialFormState> = _formState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAll(),
                repository.getFavorites(),
                repository.getAllCategories(),
            ) { all, favorites, categories ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        items = all,
                        favoriteItems = favorites,
                        categories = categories,
                        error = null,
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
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadData()
        } else {
            viewModelScope.launch {
                repository.search(query)
                    .catch { }
                    .collect { results ->
                        _uiState.update { it.copy(items = results) }
                    }
            }
        }
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

    // ========== Filtering ==========
    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            if (category == null) {
                loadData()
            } else {
                repository.getByCategory(category).collect { results ->
                    _uiState.update { it.copy(items = results) }
                }
            }
        }
    }

    // ========== CRUD ==========

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            kotlinx.coroutines.delay(800) // Fake delay for UX
            loadData()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleFavorite(item: CredentialItem) {
        viewModelScope.launch {
            repository.update(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun deleteItem(item: CredentialItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
    
    fun exportCsv(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val items = _uiState.value.items
                if (items.isEmpty()) {
                    android.widget.Toast.makeText(context, "No credentials to export", android.widget.Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val csvContent = buildString {
                    append("Title,Username,Email,Password,URL,Category,Notes\n")
                    items.forEach { item ->
                        append("${item.title.escapeCsv()},${item.username.escapeCsv()},${item.email.escapeCsv()},${item.password.escapeCsv()},${item.url.escapeCsv()},${item.category.escapeCsv()},${(item.notes ?: "").escapeCsv()}\n")
                    }
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                android.widget.Toast.makeText(context, "Exported successfully!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Failed to export CSV", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun String.escapeCsv(): String {
        var escaped = this.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    // ========== Form ==========

    fun initAddForm() {
        _formState.value = CredentialFormState()
    }

    fun initEditForm(item: CredentialItem) {
        _formState.value = CredentialFormState(
            id = item.id,
            title = item.title,
            username = item.username,
            email = item.email,
            password = item.password,
            url = item.url,
            category = item.category,
            notes = item.notes ?: "",
            isFavorite = item.isFavorite,
            isEditing = true,
        )
    }

    fun updateFormTitle(value: String) {
        _formState.update { it.copy(title = value, titleError = null) }
    }

    fun updateFormUsername(value: String) {
        _formState.update { it.copy(username = value) }
    }

    fun updateFormEmail(value: String) {
        _formState.update { it.copy(email = value) }
    }

    fun updateFormPassword(value: String) {
        _formState.update { it.copy(password = value, passwordError = null) }
    }

    fun updateFormUrl(value: String) {
        _formState.update { it.copy(url = value) }
    }

    fun updateFormCategory(value: String) {
        _formState.update { it.copy(category = value) }
    }

    fun updateFormNotes(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun updateFormFavorite(isFavorite: Boolean) {
        _formState.update { it.copy(isFavorite = isFavorite) }
    }

    fun saveForm() {
        val form = _formState.value

        // Validate
        var hasError = false
        if (form.title.isBlank()) {
            _formState.update { it.copy(titleError = "Title is required") }
            hasError = true
        }
        if (form.password.isBlank()) {
            _formState.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }
        
        if (hasError) return

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val item = CredentialItem(
                    id = if (form.isEditing) form.id else 0,
                    title = form.title.trim(),
                    username = form.username.trim(),
                    email = form.email.trim(),
                    password = form.password, // Consider encrypting here later
                    url = form.url.trim(),
                    category = form.category.trim(),
                    notes = form.notes.ifBlank { null },
                    isFavorite = form.isFavorite,
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
