package com.dev.trackone.core.network.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dev.trackone.core.network.model.SessionDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** The signed-in account, as far as this device knows. */
data class AuthSession(
    val token: String,
    val expiresAt: Long,
    val userId: Long,
    val email: String,
) {
    /**
     * Treated as expired a minute early so a call that is already in flight when
     * the clock rolls over doesn't come back as a surprise 401.
     */
    val isExpired: Boolean get() = expiresAt in 1..(System.currentTimeMillis() + LEEWAY_MS)

    private companion object {
        const val LEEWAY_MS = 60_000L
    }
}

/**
 * Persists the bearer token from `/auth/signin`.
 *
 * Kept in EncryptedSharedPreferences: the token is a 30-day credential for the
 * user's whole dataset, so it should not sit in cleartext next to the app.
 * If the keystore is unavailable (a handful of OEM builds fail to provision it)
 * we fall back to plain preferences rather than leaving the user unable to sign
 * in at all — the API is still TLS-protected either way.
 *
 * SharedPreferences rather than DataStore, again, because the OkHttp auth
 * interceptor has to read this synchronously.
 */
@Singleton
class AuthTokenStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = createPrefs(context)

    private val _session = MutableStateFlow(readSession())
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    /** Non-suspending read for the interceptor. Null when signed out or expired. */
    val currentToken: String?
        get() = _session.value?.takeIf { !it.isExpired }?.token

    fun save(dto: SessionDto) {
        val session = AuthSession(
            token = dto.token,
            expiresAt = dto.expiresAt,
            userId = dto.user.id,
            email = dto.user.email,
        )
        prefs.edit {
            putString(KEY_TOKEN, session.token)
            putLong(KEY_EXPIRES_AT, session.expiresAt)
            putLong(KEY_USER_ID, session.userId)
            putString(KEY_EMAIL, session.email)
        }
        _session.value = session
    }

    fun clear() {
        prefs.edit { clear() }
        _session.value = null
    }

    private fun readSession(): AuthSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        return AuthSession(
            token = token,
            expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L),
            userId = prefs.getLong(KEY_USER_ID, 0L),
            email = prefs.getString(KEY_EMAIL, "").orEmpty(),
        )
    }

    private companion object {
        const val TAG = "AuthTokenStore"
        const val PREFS_NAME = "trackone_auth"
        const val FALLBACK_PREFS_NAME = "trackone_auth_plain"
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRES_AT = "expires_at"
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"

        fun createPrefs(context: Context): SharedPreferences = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (e: Exception) {
            Log.w(TAG, "Keystore unavailable, storing the session unencrypted", e)
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}
