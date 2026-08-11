package com.dev.trackone.core.network.auth

import com.dev.trackone.core.network.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Where the API lives, as a runtime setting rather than a constant.
 *
 * The build only supplies a *seed* ([BuildConfig.DEFAULT_API_BASE_URL], itself
 * fed from `local.properties` / `-Ptrackone.api.baseUrl`). Once the app has run
 * once the stored value wins, so moving the Worker to a different domain is a
 * change in Settings → Cloud account, not a code change or a new build.
 *
 * Backed by SharedPreferences rather than DataStore because an OkHttp
 * interceptor needs the value synchronously on an arbitrary thread.
 */
@Singleton
class ApiConfigStore @Inject constructor() {
    private val _baseUrl = MutableStateFlow(BuildConfig.DEFAULT_API_BASE_URL)

    /** The configured origin, always with a trailing slash. */
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    /** Parsed form used by the interceptor; `null` when the stored value is unusable. */
    val resolvedUrl: HttpUrl?
        get() = _baseUrl.value.toHttpUrlOrNull()

    /** The build-time seed, so Settings can offer a "reset to default" affordance. */
    val defaultBaseUrl: String get() = BuildConfig.DEFAULT_API_BASE_URL

}
