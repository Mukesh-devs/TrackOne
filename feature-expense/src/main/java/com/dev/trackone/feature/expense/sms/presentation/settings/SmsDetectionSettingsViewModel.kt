package com.dev.trackone.feature.expense.sms.presentation.settings

import androidx.lifecycle.ViewModel
import com.dev.trackone.feature.expense.sms.SmsDetectionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SmsDetectionSettingsViewModel @Inject constructor(
    private val controller: SmsDetectionController,
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = controller.isEnabled

    fun hasPermission(): Boolean = controller.hasPermission()

    fun enable(): Boolean = controller.enable()

    fun disable() = controller.disable()

    /** Picks up a permission granted or revoked in system settings. */
    fun refresh() = controller.syncWithPermission()
}
