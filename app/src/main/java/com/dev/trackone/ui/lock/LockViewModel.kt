package com.dev.trackone.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.data.UserPreferencesRepository
import com.dev.trackone.database.AppDatabase
import com.dev.trackone.sync.GoogleAuthHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val database: AppDatabase,
    private val userPrefs: UserPreferencesRepository,
    private val googleAuthHelper: GoogleAuthHelper
) : ViewModel() {

    fun resetApp(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Wipe all local database tables
            database.clearAllTables()
            
            // Clear App Lock and PIN
            userPrefs.setAppLockEnabled(false)
            userPrefs.setHashedPin(null)
            
            withContext(Dispatchers.Main) {
                // Sign out of Google to clear backup link
                googleAuthHelper.signOut {
                    onComplete()
                }
            }
        }
    }
}
