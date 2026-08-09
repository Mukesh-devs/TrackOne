plugins {
    id("trackone.android.library")
    id("trackone.android.compose")
}

android {
    namespace = "com.dev.trackone.core.navigation"
}

dependencies {
    // Compose UI (ModuleEntry exposes @Composable content) is provided by the
    // compose convention plugin.
}
