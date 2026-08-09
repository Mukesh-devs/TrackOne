import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Accessor for the `libs` version catalog from within convention plugins.
 * (The generated `libs` accessor available in `build.gradle.kts` files is not
 * visible in precompiled/standalone plugin classes, so we look it up manually.)
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
