plugins {
    id("trackone.android.application")
    id("trackone.android.compose")
    id("trackone.android.hilt")
    id("trackone.android.room")
}

android {
    namespace = "com.dev.trackone"

    defaultConfig {
        applicationId = "com.dev.trackone"
        targetSdk = 36
        versionCode = 5
        versionName = "5.1"
    }

    applicationVariants.all {
        outputs.all {
            val outputImpl =
                this as com.android.build.gradle.internal.api.BaseVariantOutputImpl

            outputImpl.outputFileName =
                "TrackOne-v${versionName}.apk"
        }
    }

    buildTypes {
        release {
            // R8: shrink + obfuscate. Keep rules live in proguard-rules.pro.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Faster incremental builds while developing.
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

// Export Room schemas to app/schemas/ so future migrations have a baseline to
// diff against (required by exportSchema = true on AppDatabase).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Modules
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))
    implementation(project(":feature-expiry"))
    implementation(project(":feature-expense"))
    implementation(project(":feature-credential"))

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.biometric)

    // Activity
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
