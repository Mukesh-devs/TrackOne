package com.dev.trackone.core.network.interceptor

import com.dev.trackone.core.network.api.ServerNotConfiguredException
import com.dev.trackone.core.network.auth.ApiConfigStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rewrites every request onto the origin currently stored in [ApiConfigStore].
 *
 * Retrofit needs *some* base URL at construction time, so it is built with a
 * placeholder and this interceptor swaps in the real scheme/host/port (and any
 * path prefix, for deployments that sit behind a proxy path) on each call. That
 * is what makes the server address a runtime setting instead of a constant.
 */
@Singleton
class BaseUrlInterceptor @Inject constructor(
    private val config: ApiConfigStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val target = config.resolvedUrl ?: throw ServerNotConfiguredException()
        val request = chain.request()
        val prefix = target.encodedPath.removeSuffix("/")

        val rewritten = request.url.newBuilder()
            .scheme(target.scheme)
            .host(target.host)
            .port(target.port)
            .encodedPath(prefix + request.url.encodedPath)
            .build()

        return chain.proceed(request.newBuilder().url(rewritten).build())
    }
}
