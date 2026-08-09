plugins {
    id("trackone.android.library")
    id("trackone.android.compose")
}

android {
    namespace = "com.dev.trackone.core.ui"
}

dependencies {
    implementation(project(":core:common"))

    // Activity Compose (for edge-to-edge helpers)
    implementation(libs.activity.compose)
}
