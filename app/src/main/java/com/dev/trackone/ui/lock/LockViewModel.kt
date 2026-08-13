package com.dev.trackone.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.cloud.CloudAccountManager
import com.dev.trackone.data.UserPreferencesRepository
import com.dev.trackone.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val database: AppDatabase,
    private val userPrefs: UserPreferencesRepository,
    private val cloudAccountManager: CloudAccountManager,
) : ViewModel() {

    /**
     * "Forgot PIN" escape hatch: wipes this device back to a clean install.
     *
     * The cloud session goes with it. Leaving the token behind would make the
     * reset a way *around* App Lock — the account data would simply download
     * again on the next launch, with no PIN in the way. Signing out means the
     * cloud password is needed to get the data back, which is the whole point
     * of the lock.
     */
    fun resetApp(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Drops the token and the local cache with it.
            cloudAccountManager.signOut()

            // Anything the cache clear did not cover.
            database.clearAllTables()

            // Clear App Lock and PIN
            userPrefs.setAppLockEnabled(false)
            userPrefs.setHashedPin(null)

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
