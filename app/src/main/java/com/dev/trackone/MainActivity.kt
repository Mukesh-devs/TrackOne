package com.dev.trackone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.core.ui.theme.TrackOneTheme
import com.dev.trackone.ui.MainViewModel
import com.dev.trackone.ui.TrackOneApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main (and only) Activity for TrackOne.
 * Uses edge-to-edge rendering and delegates all UI to Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            TrackOneTheme(themeMode = themeMode) {
                TrackOneApp(viewModel = viewModel)
            }
        }
    }
}
