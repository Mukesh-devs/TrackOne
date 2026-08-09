plugins {
    id("trackone.android.library")
    id("trackone.android.room")
}

android {
    namespace = "com.dev.trackone.core.database"
}

dependencies {
    implementation(project(":core:common"))

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
