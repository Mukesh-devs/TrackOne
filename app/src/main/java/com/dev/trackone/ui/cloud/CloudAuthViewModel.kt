package com.dev.trackone.ui.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.trackone.cloud.CloudAccountManager

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the app is with respect to the cloud account. */
sealed interface CloudAuthState {
    /** Restoring/validating a stored token — shown as a spinner, not a form. */
    data object Checking : CloudAuthState
    data object SignedOut : CloudAuthState
    data class SignedIn(val email: String) : CloudAuthState
}

data class SignInFormState(
    val email: String = "",
    val password: String = "",
    val isRegistering: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CloudAuthViewModel @Inject constructor(
    private val accountManager: CloudAccountManager,
) : ViewModel() {

    /**
     * True only while a *stored* token is being validated. Starting from "no
     * stored token" means a fresh install goes straight to the form instead of
     * flashing a spinner.
     */
    private val _isCheckingStoredSession = MutableStateFlow(accountManager.session.value != null)

    private val _form = MutableStateFlow(SignInFormState())
    val form: StateFlow<SignInFormState> = _form.asStateFlow()

    val state: StateFlow<CloudAuthState> = combine(
        accountManager.session,
        _isCheckingStoredSession,
    ) { session, checking ->
        when {
            session != null && !session.isExpired -> CloudAuthState.SignedIn(session.email)
            checking -> CloudAuthState.Checking
            else -> CloudAuthState.SignedOut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = if (_isCheckingStoredSession.value) {
            CloudAuthState.Checking
        } else {
            CloudAuthState.SignedOut
        },
    )

    init {
        viewModelScope.launch {
            accountManager.verifyRestoredSession()
            _isCheckingStoredSession.value = false
        }
    }



    /**
     * Signs out and drops the local cache with it — cached rows carry no account
     * marker, so leaving them would show this account's data to the next one.
     */
    fun signOut() {
        viewModelScope.launch {
            accountManager.signOut()
            _form.value = SignInFormState()
        }
    }

    fun setEmail(value: String) = _form.update { it.copy(email = value, error = null) }

    fun setPassword(value: String) = _form.update { it.copy(password = value, error = null) }

    fun toggleMode() = _form.update {
        it.copy(isRegistering = !it.isRegistering, error = null)
    }



    fun submit() {
        val form = _form.value
        if (form.isSubmitting) return

        val email = form.email.trim()
        if (email.isBlank() || !email.contains('@')) {
            _form.update { it.copy(error = "Enter a valid email address") }
            return
        }
        if (form.password.length < 8) {
            _form.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }

        _form.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = runCatching {
                if (form.isRegistering) {
                    accountManager.signUp(email, form.password)
                } else {
                    accountManager.signIn(email, form.password)
                }
            }
            _form.update { current ->
                if (result.isSuccess) {
                    // Never keep the password around after a successful sign-in.
                    current.copy(isSubmitting = false, password = "", error = null)
                } else {
                    current.copy(
                        isSubmitting = false,
                        error = result.exceptionOrNull()?.message ?: "Sign-in failed",
                    )
                }
            }
        }
    }
}
