package com.dev.trackone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.core.ui.theme.TrackOneTheme
import com.dev.trackone.ui.MainViewModel
import com.dev.trackone.ui.TrackOneApp
import com.dev.trackone.ui.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import com.dev.trackone.sync.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import javax.inject.Inject

/**
 * Main (and only) Activity for TrackOne.
 * Uses edge-to-edge rendering and delegates all UI to Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var googleAuthHelper: GoogleAuthHelper

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            task.addOnSuccessListener { account ->
                viewModel.onSignedIn(account)
            }.addOnFailureListener {
                viewModel.resetSyncState()
            }
        } else {
            viewModel.resetSyncState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        splashScreen.setKeepOnScreenCondition {
            viewModel.userPreferences.value == null
        }
        
        enableEdgeToEdge()
        
        // Restore existing sign-in if available
        googleAuthHelper.getSignedInAccount()?.let { account ->
            viewModel.onSignedIn(account)
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()

            TrackOneTheme(themeMode = themeMode) {
                userPreferences?.let { prefs ->
                    if (!prefs.hasSeenOnboarding) {
                        OnboardingScreen(
                            onComplete = {
                                viewModel.completeOnboarding()
                            }
                        )
                    } else {
                        TrackOneApp(
                            viewModel = viewModel,
                            onSignInClick = {
                                signInLauncher.launch(googleAuthHelper.getSignInIntent())
                            },
                            onSignOutClick = {
                                googleAuthHelper.signOut {
                                    viewModel.onSignOut()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
