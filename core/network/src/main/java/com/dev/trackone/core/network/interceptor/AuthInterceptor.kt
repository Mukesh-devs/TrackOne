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
 * Two things retire a token, and both end the same way: [AuthTokenStore.session]
 * goes null, and the auth-state observers put the user back on the sign-in
 * screen.
 *
 * - **It has passed its expiry.** Dropped before the request goes out, since
 *   the answer is a foregone `401` — and offline there would be no answer at
 *   all, only an `IOException`, which would leave the dead token in place.
 * - **A `401` came back anyway.** Server-side expiry, or `JWT_SECRET` rotated
 *   under us.
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

        if (!isAuthEndpoint) tokenStore.clearIfExpired()

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
