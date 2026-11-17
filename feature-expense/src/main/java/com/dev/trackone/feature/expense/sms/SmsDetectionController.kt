package com.dev.trackone.feature.expense.sms

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the on/off state of SMS detection.
 *
 * The feature is on exactly when [SmsReceiver] is enabled in the package
 * manager and RECEIVE_SMS is granted — no separate preference is stored, so
 * the two can never disagree, and a reinstall starts with it off.
 */
@Singleton
class SmsDetectionController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val receiver = ComponentName(context, SmsReceiver::class.java)

    private val _isEnabled = MutableStateFlow(isReceiverEnabled() && hasPermission())
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED

    /** Turns detection on if the permission is held. Returns whether it is now on. */
    fun enable(): Boolean {
        if (!hasPermission()) {
            disable()
            return false
        }
        setReceiverEnabled(true)
        return true
    }

    fun disable() {
        setReceiverEnabled(false)
    }

    /** Switches detection off if the permission has been revoked since it was turned on. */
    fun syncWithPermission() {
        if (isReceiverEnabled() && !hasPermission()) {
            setReceiverEnabled(false)
        } else {
            _isEnabled.value = isReceiverEnabled() && hasPermission()
        }
    }

    // The manifest declares the receiver disabled, so DEFAULT also means off.
    private fun isReceiverEnabled(): Boolean =
        context.packageManager.getComponentEnabledSetting(receiver) ==
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    private fun setReceiverEnabled(enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            receiver,
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
        _isEnabled.value = enabled
    }
}
