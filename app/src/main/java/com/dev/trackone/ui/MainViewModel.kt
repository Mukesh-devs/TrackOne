package com.dev.trackone.ui

import androidx.lifecycle.ViewModel
import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Main ViewModel managing the module pager state and theme preference.
 * Collects all registered [ModuleEntry] instances via Hilt multibinding.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    moduleEntries: Set<@JvmSuppressWildcards ModuleEntry>,
) : ViewModel() {

    /**
     * All registered modules, sorted by their declared order.
     */
    val modules: List<ModuleEntry> = moduleEntries.sortedBy { it.order }

    /**
     * Current theme mode. Defaults to system setting.
     * Future: persist this preference in DataStore / Room.
     */
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /**
     * Current page index in the pager.
     */
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }
}
