import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `trackone.android.application` — base plugin for the single `:app` module.
 * Handles the shared SDK/Java/Kotlin config; app-specific bits (applicationId,
 * versionCode, buildTypes, packaging, …) stay in app/build.gradle.kts.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.testInstrumentationRunner =
                    "androidx.test.runner.AndroidJUnitRunner"
            }
        }
    }
}
