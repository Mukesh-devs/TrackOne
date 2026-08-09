import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `trackone.android.room` — applies KSP and the Room dependencies. Used by
 * core:database and the feature modules that own their own tables.
 */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add("implementation", libs.findLibrary("room-runtime").get())
                add("implementation", libs.findLibrary("room-ktx").get())
                add("ksp", libs.findLibrary("room-compiler").get())
            }
        }
    }
}
