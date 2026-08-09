import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

/**
 * `trackone.android.compose` — enables Compose and adds the common Compose
 * dependencies. Apply it AFTER an Android base plugin (library/application),
 * since it reads the already-registered Android extension.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // The Android extension is registered under its concrete type, so
            // resolve whichever one this module applied.
            extensions.findByType<ApplicationExtension>()?.let { configureAndroidCompose(it) }
            extensions.findByType<LibraryExtension>()?.let { configureAndroidCompose(it) }
        }
    }
}
