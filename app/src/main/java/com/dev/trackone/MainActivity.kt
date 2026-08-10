package com.dev.trackone

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

import androidx.fragment.app.FragmentActivity

/**
 * Main (and only) Activity for TrackOne.
 * Uses edge-to-edge rendering and delegates all UI to Compose.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

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

        // The app holds passwords, so hide its content from the recent-apps
        // switcher. Deliberately NOT FLAG_SECURE: that would also block the
        // user taking their own screenshots. To lock the app down completely,
        // replace this with:
        //   window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
        //                   WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }


        // Restore existing sign-in if available
        googleAuthHelper.getSignedInAccount()?.let { account ->
            viewModel.onSignedIn(account)
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
            var isAuthenticated by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

            // Reset authentication when app goes to background
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                        isAuthenticated = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // dynamicColor = false so our sky-blue palette applies on every device
            // (Android 12+ would otherwise override it with wallpaper colors).
            TrackOneTheme(themeMode = themeMode, dynamicColor = false) {
                userPreferences?.let { prefs ->
                    if (!prefs.hasSeenOnboarding) {
                        OnboardingScreen(
                            onComplete = {
                                viewModel.completeOnboarding()
                            }
                        )
                    } else {
                        com.dev.trackone.ui.lock.AuthGate(
                            isAppLockEnabled = prefs.isAppLockEnabled,
                            hashedPin = prefs.hashedPin,
                            isAuthenticated = isAuthenticated,
                            onUnlocked = { isAuthenticated = true }
                        ) {
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
}
