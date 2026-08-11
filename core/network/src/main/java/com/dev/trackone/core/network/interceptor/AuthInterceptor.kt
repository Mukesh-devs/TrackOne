package com.dev.trackone.core.network.interceptor

import com.dev.trackone.core.network.auth.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the bearer token to everything except the two endpoints that issue
 * one — sending a stale token while signing in would get the sign-in itself
 * rejected.
 *
 * A `401` anywhere else means the stored token is no longer good (expired, or
 * `JWT_SECRET` was rotated server-side), so it is cleared here; the auth state
 * observers then send the user back to the sign-in screen.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: AuthTokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isAuthEndpoint = request.url.encodedPath.let {
            it.endsWith("/auth/signup") || it.endsWith("/auth/signin")
        }

        val token = tokenStore.currentToken
        val outgoing = if (isAuthEndpoint || token == null) {
            request
        } else {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        }

        return chain.proceed(outgoing).also { response ->
            if (response.code == 401 && !isAuthEndpoint) tokenStore.clear()
        }
    }
}
