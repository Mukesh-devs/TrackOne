import java.util.Properties

plugins {
    id("trackone.android.library")
    id("trackone.android.hilt")
}

/**
 * Default API origin baked into BuildConfig.
 *
 * Resolution order (first hit wins):
 *   1. `local.properties`  — per-machine, git-ignored
 *   2. `-Ptrackone.api.baseUrl=…` / `gradle.properties`
 *   3. the emulator loopback, so a fresh clone talks to `wrangler dev`
 *
 * This is only a *default*: the value is copied into DataStore on first launch
 * and can be changed at runtime from Settings → Cloud account, so pointing the
 * app at a new domain never needs a rebuild. No URL is hardcoded in Kotlin.
 */
val defaultApiBaseUrl: String = run {
    val localProps = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }
    val raw = localProps.getProperty("trackone.api.baseUrl")
        ?: providers.gradleProperty("trackone.api.baseUrl").orNull
        ?: "http://10.0.2.2:8787/"
    if (raw.endsWith("/")) raw else "$raw/"
}

android {
    namespace = "com.dev.trackone.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "DEFAULT_API_BASE_URL", "\"$defaultApiBaseUrl\"")
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    api(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    // api, not implementation: the app builds raw JSON request bodies for the
    // whole-database sync endpoint.
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    api(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
