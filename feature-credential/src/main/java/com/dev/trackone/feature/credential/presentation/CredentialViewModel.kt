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

    // Filter inputs. Kept as flows so the pipeline below re-evaluates on change
    // instead of launching a fresh collector per keystroke.
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    init {
        observeData()
    }

    /**
     * One long-lived collector for the whole screen.
     *
     * Previously every search keystroke and category tap launched an additional
     * `repository.x().collect { }` that was never cancelled, so collectors piled
     * up and raced to write [uiState]. Combining the filter inputs into a single
     * pipeline fixes the leak and makes filtering instant (no extra DB queries).
     */
    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.getAll(),
                repository.getAllCategories(),
                _searchQuery,
                _selectedCategory,
            ) { all, categories, query, category ->
                val trimmed = query.trim()
                val visible = all.filter { item ->
                    (category == null || item.category == category) &&
                        (trimmed.isBlank() || item.matches(trimmed))
                }
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        items = visible,
                        favoriteItems = visible.filter { it.isFavorite },
                        categories = categories,
                        searchQuery = query,
                        selectedCategory = category,
                        error = null,
                    )
                }
            }
                .catch { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "Unknown error")
                    }
                }
                .collect {}
        }
    }

    // ========== Search & filtering ==========

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _uiState.update { state ->
            val active = !state.isSearchActive
            if (!active) _searchQuery.value = ""
            state.copy(isSearchActive = active)
        }
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    // ========== CRUD ==========

    /** Data is Flow-backed and always live; this only drives the pull indicator. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = false) }
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

    // ========== CSV export ==========

    /**
     * Builds the CSV payload. Deliberately returns a String rather than writing
     * a file: Uri/Context/Toast belong to the UI layer, which keeps this
     * ViewModel free of Android framework types and unit-testable.
     */
    fun buildCsv(): String {
        val items = _uiState.value.items
        if (items.isEmpty()) return ""
        return buildString {
            append("Title,Username,Email,Password,URL,Category,Notes\n")
            items.forEach { item ->
                append(item.title.escapeCsv()).append(',')
                append(item.username.escapeCsv()).append(',')
                append(item.email.escapeCsv()).append(',')
                append(item.password.escapeCsv()).append(',')
                append(item.url.escapeCsv()).append(',')
                append(item.category.escapeCsv()).append(',')
                append((item.notes ?: "").escapeCsv()).append('\n')
            }
        }
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

    fun updateFormTitle(value: String) = _formState.update { it.copy(title = value, titleError = null) }
    fun updateFormUsername(value: String) = _formState.update { it.copy(username = value) }
    fun updateFormEmail(value: String) = _formState.update { it.copy(email = value) }
    fun updateFormPassword(value: String) = _formState.update { it.copy(password = value, passwordError = null) }
    fun updateFormUrl(value: String) = _formState.update { it.copy(url = value) }
    fun updateFormCategory(value: String) = _formState.update { it.copy(category = value) }
    fun updateFormNotes(value: String) = _formState.update { it.copy(notes = value) }
    fun updateFormFavorite(isFavorite: Boolean) = _formState.update { it.copy(isFavorite = isFavorite) }

    fun saveForm() {
        val form = _formState.value

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
                    password = form.password,
                    url = form.url.trim(),
                    category = form.category.trim(),
                    notes = form.notes.ifBlank { null },
                    isFavorite = form.isFavorite,
                )
                if (form.isEditing) repository.update(item) else repository.insert(item)
                _formState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, titleError = e.message ?: "Could not save") }
            }
        }
    }

    fun resetSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}

private fun CredentialItem.matches(query: String): Boolean {
    val q = query.lowercase()
    return title.lowercase().contains(q) ||
        username.lowercase().contains(q) ||
        email.lowercase().contains(q) ||
        url.lowercase().contains(q) ||
        category.lowercase().contains(q)
}

private fun String.escapeCsv(): String {
    val escaped = replace("\"", "\"\"")
    return if (escaped.any { it == ',' || it == '\n' || it == '"' }) "\"$escaped\"" else escaped
}
