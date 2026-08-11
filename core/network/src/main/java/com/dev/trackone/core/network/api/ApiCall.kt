package com.dev.trackone.core.network.api

import com.dev.trackone.core.network.model.ApiEnvelope
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** The server clamps `limit` to 1..200; ask for the maximum to minimise round trips. */
private const val MAX_PAGE_SIZE = 200

/**
 * Runs a Retrofit call and returns its envelope, translating every failure mode
 * into a [TrackOneApiException].
 *
 * `CancellationException` is a [kotlin.coroutines.cancellation.CancellationException],
 * not an [IOException], so structured-concurrency cancellation still propagates
 * untouched.
 */
private suspend fun <T : Any> envelope(
    block: suspend () -> ApiEnvelope<T>,
): ApiEnvelope<T> = try {
    block()
} catch (e: HttpException) {
    throw e.toApiException()
} catch (e: TrackOneApiException) {
    throw e
} catch (e: IOException) {
    throw NoConnectivityException(e)
}

/**
 * Runs a call and unwraps `data`, or throws.
 *
 * ```
 * val created = apiCall { api.createExpenseItem(item.toRequest()) }
 * ```
 */
suspend fun <T : Any> apiCall(block: suspend () -> ApiEnvelope<T>): T =
    envelope(block).dataOrThrow()

/**
 * Runs a `DELETE`, which answers `204 No Content`.
 *
 * A `404` is treated as success: the row is gone, which is exactly the state
 * the caller wanted, and failing here would strand a deleted row in the cache.
 */
suspend fun apiDelete(block: suspend () -> Response<Unit>) {
    val response = try {
        block()
    } catch (e: TrackOneApiException) {
        throw e
    } catch (e: IOException) {
        throw NoConnectivityException(e)
    }
    if (!response.isSuccessful && response.code() != 404) {
        throw errorFrom(response.code(), response.errorBody()?.string())
    }
}

/**
 * Walks every page of a list endpoint.
 *
 * Without this the cache would silently truncate at the server's default page
 * size, and the next `replaceAll` would delete the rows that never arrived.
 */
suspend fun <T : Any> fetchAllPages(
    page: suspend (limit: Int, offset: Int) -> ApiEnvelope<List<T>>,
): List<T> {
    val all = ArrayList<T>()
    var offset = 0
    while (true) {
        val envelope = envelope { page(MAX_PAGE_SIZE, offset) }
        val chunk = envelope.dataOrThrow()
        all += chunk
        val hasMore = envelope.meta?.hasMore ?: (chunk.size == MAX_PAGE_SIZE)
        if (!hasMore || chunk.isEmpty()) break
        offset += chunk.size
    }
    return all
}

private fun <T : Any> ApiEnvelope<T>.dataOrThrow(): T {
    val payload = data
    if (success && payload != null) return payload
    val body = error
    throw if (body == null) {
        ApiErrorException(200, "malformed_response", "The server returned an unexpected response.")
    } else {
        ApiErrorException(200, body.code, body.message)
    }
}

private fun HttpException.toApiException(): TrackOneApiException =
    errorFrom(code(), response()?.errorBody()?.string())

/**
 * Parses the `{ error: { code, message } }` body by hand rather than through
 * Moshi: error bodies also arrive from proxies and edge caches that never heard
 * of the envelope, and a parse failure here must not mask the real status code.
 */
private fun errorFrom(httpCode: Int, rawBody: String?): TrackOneApiException {
    var code: String? = null
    var message: String? = null
    if (!rawBody.isNullOrBlank()) {
        runCatching {
            val error = JSONObject(rawBody).optJSONObject("error")
            if (error != null) {
                code = error.optString("code").takeIf { it.isNotBlank() }
                message = error.optString("message").takeIf { it.isNotBlank() }
            }
        }
    }
    if (httpCode == 401) {
        return UnauthorizedException(message ?: "Your session has expired. Please sign in again.")
    }
    return ApiErrorException(
        httpCode = httpCode,
        code = code ?: "http_$httpCode",
        message = message ?: defaultMessageFor(httpCode),
    )
}

private fun defaultMessageFor(httpCode: Int): String = when (httpCode) {
    400 -> "The request was malformed."
    403 -> "You do not have access to that."
    404 -> "That item no longer exists on the server."
    409 -> "That conflicts with something already on the server."
    422 -> "Some fields are invalid. Please check and try again."
    in 500..599 -> "The server had a problem. Please try again shortly."
    else -> "Request failed ($httpCode)."
}
