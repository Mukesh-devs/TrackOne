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

    implementation(libs.androidx.core.ktx)
    // Permission request launcher for the SMS opt-in
    implementation(libs.activity.compose)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Testing
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
