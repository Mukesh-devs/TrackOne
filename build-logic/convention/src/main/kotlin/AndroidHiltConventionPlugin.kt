import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `trackone.android.hilt` — applies Hilt (+ KSP for its compiler) and the
 * Hilt dependencies used across the app and feature modules.
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
