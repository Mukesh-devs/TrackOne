import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Enables Jetpack Compose and wires the common Compose dependencies. Applied by
 * [AndroidComposeConventionPlugin] to any module that also applies an Android
 * base plugin (application or library).
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(bom))
        add("implementation", libs.findLibrary("compose-ui").get())
        add("implementation", libs.findLibrary("compose-ui-graphics").get())
        add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("compose-material3").get())
        add("implementation", libs.findLibrary("compose-material-icons-extended").get())
        add("implementation", libs.findLibrary("compose-foundation").get())
        add("implementation", libs.findLibrary("compose-animation").get())
        add("implementation", libs.findLibrary("compose-runtime").get())
        add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
    }
}
