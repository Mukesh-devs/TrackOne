package com.dev.trackone.core.network.auth

import com.dev.trackone.core.network.api.TrackOneApi
import com.dev.trackone.core.network.api.apiCall
import com.dev.trackone.core.network.model.CredentialsBody
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sign-in / sign-up against the Worker API, plus the token that results.
 *
 * Deliberately knows nothing about Room: wiping and repopulating the local
 * cache when the account changes is the app module's job, since only it can see
 * every DAO.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: TrackOneApi,
    private val tokenStore: AuthTokenStore,
) {
    val session: StateFlow<AuthSession?> = tokenStore.session

    val isSignedIn: Boolean get() = tokenStore.currentToken != null

    /** Emits false whenever there is no usable (present, unexpired) token. */
    fun observeSignedIn() = session.map { it != null && !it.isExpired }

    suspend fun signIn(email: String, password: String): AuthSession {
        val dto = apiCall { api.signIn(CredentialsBody(email.trim().lowercase(), password)) }
        tokenStore.save(dto)
        return requireNotNull(tokenStore.session.value)
    }

    suspend fun signUp(email: String, password: String): AuthSession {
        val dto = apiCall { api.signUp(CredentialsBody(email.trim().lowercase(), password)) }
        tokenStore.save(dto)
        return requireNotNull(tokenStore.session.value)
    }

    /**
     * Cheap "is my stored token still good?" probe for app start. Any failure
     * other than a 401 (which the interceptor already handles by clearing the
     * token) is treated as "unknown" so a flaky network can't sign the user out.
     */
    suspend fun verifySession(): Boolean = runCatching { apiCall { api.me() } }.isSuccess

    /** Forgets the token only. Callers that also hold a cache must clear it too. */
    fun clearSession() = tokenStore.clear()
}
