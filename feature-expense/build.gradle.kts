plugins {
    id("trackone.android.library")
    id("trackone.android.compose")
    id("trackone.android.hilt")
    id("trackone.android.room")
}

android {
    namespace = "com.dev.trackone.feature.expense"
}

// Export the detected-transactions database schema so future migrations have a
// baseline to diff against (exportSchema = true on DetectedTransactionsDatabase).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:navigation"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
