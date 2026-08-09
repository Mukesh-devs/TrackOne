plugins {
    id("trackone.android.library")
}

android {
    namespace = "com.dev.trackone.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
