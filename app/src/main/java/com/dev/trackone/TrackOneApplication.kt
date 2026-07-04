package com.dev.trackone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * TrackOne Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class TrackOneApplication : Application()
