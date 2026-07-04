pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TrackOne"

// App
include(":app")

// Core modules
include(":core:common")
include(":core:database")
include(":core:navigation")
include(":core:ui")

// Feature modules
include(":feature-expiry")
include(":feature-expense")
include(":feature-credential")