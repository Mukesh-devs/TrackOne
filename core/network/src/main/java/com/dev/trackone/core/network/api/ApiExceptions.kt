package com.dev.trackone.core.network.api

import java.io.IOException

/**
 * Every failure the API layer raises is one of these, so callers can catch a
 * single type and put [message] straight in front of the user.
 *
 * They extend [IOException] because that is what Retrofit/OkHttp already throw
 * for transport problems — nothing upstream has to learn a new hierarchy.
 */
sealed class TrackOneApiException(
    message: String,
    cause: Throwable? = null,
) : IOException(message, cause)

/** The request never reached the server (airplane mode, DNS, timeout, TLS). */
class NoConnectivityException(
    cause: Throwable? = null,
) : TrackOneApiException("No internet connection. Check your network and try again.", cause)

/** No server URL configured yet, or the stored one is not a valid http(s) URL. */
class ServerNotConfiguredException :
    TrackOneApiException("No server URL configured. Set it in Settings → Cloud account.")

/** Not signed in, or the stored token expired / was signed with a rotated secret. */
class UnauthorizedException(
    message: String = "Your session has expired. Please sign in again.",
) : TrackOneApiException(message)

/** The server answered, but with an error envelope or a non-2xx status. */
class ApiErrorException(
    val httpCode: Int,
    val code: String,
    message: String,
) : TrackOneApiException(message)
