package com.dev.trackone.ui

import androidx.lifecycle.ViewModel
import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.dev.trackone.data.UserPreferences
import com.dev.trackone.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.dev.trackone.sync.SyncManager
import com.dev.trackone.ui.sync.SyncUiState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Main ViewModel managing the module pager state and theme preference.
 * Collects all registered [ModuleEntry] instances via Hilt multibinding.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    moduleEntries: Set<@JvmSuppressWildcards ModuleEntry>,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    /**
     * All registered modules, sorted by their declared order.
     */
    val modules: List<ModuleEntry> = moduleEntries.sortedBy { it.order }

    /**
     * User preferences from DataStore
     */
    val userPreferences: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

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
    
    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.completeOnboarding()
        }
    }

    fun completeTour() {
        viewModelScope.launch {
            userPreferencesRepository.completeTour()
        }
    }

    // --- Cloud Sync ---

    private val _syncUiState = MutableStateFlow<SyncUiState>(SyncUiState.NotSignedIn)
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()

    /**
     * Whether local data has changes not yet backed up.
     * true = backup pending (local edits are newer than last backup)
     * false = synced (backup is up to date)
     * null = unknown / not signed in / no data
     */
    private val _isBackupPending = MutableStateFlow<Boolean?>(null)
    val isBackupPending: StateFlow<Boolean?> = _isBackupPending.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private var currentAccount: GoogleSignInAccount? = null

    init {
        // Observe both data edits AND backup timestamp changes in real-time
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                syncManager.observeLatestDataEditTime(),
                userPreferencesRepository.userPreferencesFlow
            ) { latestEditTime, prefs ->
                val lastBackupTimestamp = prefs.lastBackupTimestamp
                when {
                    lastBackupTimestamp == 0L && latestEditTime == 0L -> null
                    lastBackupTimestamp == 0L -> true
                    latestEditTime > lastBackupTimestamp -> true
                    else -> false
                }
            }.collect { pending ->
                _isBackupPending.value = pending
            }
        }
    }

    fun onSignedIn(account: GoogleSignInAccount) {
        currentAccount = account
        _isSignedIn.value = true
        _syncUiState.value = SyncUiState.Loading
        viewModelScope.launch {
            val backupMetadata = syncManager.getBackupMetadata(account)
            _syncUiState.value = SyncUiState.SignedIn(
                accountEmail = account.email ?: "Unknown Email",
                lastBackupTime = backupMetadata?.modifiedTime,
                backupSizeBytes = backupMetadata?.sizeBytes
            )
        }
    }

    fun onSignOut() {
        currentAccount = null
        _isSignedIn.value = false
        _syncUiState.value = SyncUiState.NotSignedIn
    }

    fun onBackup() {
        val account = currentAccount ?: return
        _syncUiState.value = SyncUiState.Loading
        viewModelScope.launch {
            val result = syncManager.backup(account)
            val newMetadata = syncManager.getBackupMetadata(account)
            val newSignedIn = SyncUiState.SignedIn(
                accountEmail = account.email ?: "Unknown Email",
                lastBackupTime = newMetadata?.modifiedTime,
                backupSizeBytes = newMetadata?.sizeBytes
            )
            if (result.isSuccess) {
                val now = System.currentTimeMillis()
                userPreferencesRepository.setLastBackupTimestamp(now)
                _syncUiState.value = SyncUiState.Success("Backup successful!", newSignedIn)
            } else {
                _syncUiState.value = SyncUiState.Error(result.exceptionOrNull()?.message ?: "Backup failed", newSignedIn)
            }
        }
    }

    fun onRestore() {
        val account = currentAccount ?: return
        _syncUiState.value = SyncUiState.Loading
        viewModelScope.launch {
            val result = syncManager.restore(account)
            val newMetadata = syncManager.getBackupMetadata(account)
            val newSignedIn = SyncUiState.SignedIn(
                accountEmail = account.email ?: "Unknown Email",
                lastBackupTime = newMetadata?.modifiedTime,
                backupSizeBytes = newMetadata?.sizeBytes
            )
            if (result.isSuccess) {
                val now = System.currentTimeMillis()
                userPreferencesRepository.setLastBackupTimestamp(now)
                _syncUiState.value = SyncUiState.Success("Restore successful!", newSignedIn)
            } else {
                _syncUiState.value = SyncUiState.Error(result.exceptionOrNull()?.message ?: "Restore failed", newSignedIn)
            }
        }
    }
    
    /**
     * Resets the sync UI back to the signed-in state (with backup/restore buttons).
     * Called when the bottom sheet is dismissed after a Success/Error.
     */
    fun resetSyncState() {
        if (currentAccount != null) {
            onSignedIn(currentAccount!!)
        } else {
            _syncUiState.value = SyncUiState.NotSignedIn
        }
    }
}
