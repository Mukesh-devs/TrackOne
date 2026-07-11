package com.dev.trackone.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.data.UserPreferencesRepository
import com.dev.trackone.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val isAppLockEnabled: StateFlow<Boolean> = preferencesRepository.userPreferencesFlow
        .map { it.isAppLockEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val hasPinSet: StateFlow<Boolean> = preferencesRepository.userPreferencesFlow
        .map { it.hashedPin != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun enableAppLockWithPin(pin: String) {
        viewModelScope.launch {
            val hashedPin = SecurityManager.hashPin(pin)
            preferencesRepository.setHashedPin(hashedPin)
            preferencesRepository.setAppLockEnabled(true)
        }
    }

    fun disableAppLock() {
        viewModelScope.launch {
            preferencesRepository.setAppLockEnabled(false)
            preferencesRepository.setHashedPin(null)
        }
    }
}
