package com.dev.trackone.feature.credential.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dev.trackone.core.ui.component.BottomNavItem
import com.dev.trackone.core.ui.component.TrackOneBottomBar
import com.dev.trackone.core.ui.component.TrackOneTopBar
import com.dev.trackone.feature.credential.presentation.CredentialViewModel
import com.dev.trackone.feature.credential.presentation.screen.CredentialAddScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialDetailsScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialHomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object CredentialRoutes {
    const val HOME = "credential_home"
    const val ADD = "credential_add"
    const val SETTINGS = "credential_settings"
    const val EDIT = "credential_edit"
    const val DETAILS = "credential_details/{title}"
    
    fun createDetailsRoute(title: String): String {
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        return "credential_details/$encodedTitle"
    }
}

private val credentialBottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, CredentialRoutes.HOME),
    BottomNavItem("Add", Icons.Default.Add, CredentialRoutes.ADD),
    BottomNavItem("Settings", Icons.Default.Settings, CredentialRoutes.SETTINGS),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialNavigation() {
    val navController = rememberNavController()
    val viewModel: CredentialViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: CredentialRoutes.HOME
    
    val isOnMainScreen = currentRoute in listOf(
        CredentialRoutes.HOME, CredentialRoutes.ADD, CredentialRoutes.SETTINGS
    )

    com.dev.trackone.core.ui.theme.CredentialModuleTheme {
    Scaffold(
        topBar = {
            if (isOnMainScreen) {
                TrackOneTopBar(
                    title = when (currentRoute) {
                        CredentialRoutes.HOME -> "Credential Manager"
                        CredentialRoutes.ADD -> "Add Credential"
                        CredentialRoutes.SETTINGS -> "Settings"
                        else -> "Credentials"
                    },
                    showSearchIcon = currentRoute == CredentialRoutes.HOME,
                    onSearchClick = { viewModel.toggleSearch() },
                )
            } else {
                val title = when (currentRoute) {
                    CredentialRoutes.EDIT -> "Edit Credential"
                    CredentialRoutes.DETAILS -> {
                        val argTitle = navBackStackEntry?.arguments?.getString("title") ?: "Details"
                        URLDecoder.decode(argTitle, StandardCharsets.UTF_8.toString())
                    }
                    else -> "Details"
                }
                TrackOneTopBar(
                    title = title,
                    showBackButton = true,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = CredentialRoutes.HOME,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
            ) {
                composable(CredentialRoutes.HOME) {
                    CredentialHomeScreen(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                        onGroupClick = { title ->
                            navController.navigate(CredentialRoutes.createDetailsRoute(title))
                        }
                    )
                }
                
                composable(
                    route = CredentialRoutes.DETAILS,
                    arguments = listOf(navArgument("title") { type = NavType.StringType })
                ) { backStackEntry ->
                    val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
                    val groupTitle = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
                    
                    CredentialDetailsScreen(
                        groupTitle = groupTitle,
                        uiState = uiState,
                        onEditItem = { item ->
                            viewModel.initEditForm(item)
                            navController.navigate(CredentialRoutes.EDIT)
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                            // If it was the last item for this title, we should probably go back, 
                            // but UI state will handle the empty list gracefully or we could pop back automatically.
                        }
                    )
                }

                composable(CredentialRoutes.ADD) {
                    CredentialAddScreen(
                        formState = formState,
                        onTitleChange = viewModel::updateFormTitle,
                        onUsernameChange = viewModel::updateFormUsername,
                        onEmailChange = viewModel::updateFormEmail,
                        onPasswordChange = viewModel::updateFormPassword,
                        onUrlChange = viewModel::updateFormUrl,
                        onCategoryChange = viewModel::updateFormCategory,
                        onNotesChange = viewModel::updateFormNotes,
                        onFavoriteChange = viewModel::updateFormFavorite,
                        onSave = viewModel::saveForm,
                        onSaveSuccess = {
                            viewModel.resetSaveSuccess()
                            navController.navigate(CredentialRoutes.HOME) {
                                popUpTo(CredentialRoutes.HOME) { inclusive = true }
                            }
                        },
                    )
                }

                composable(CredentialRoutes.SETTINGS) {
                    // Placeholder settings screen
                    com.dev.trackone.core.ui.component.TrackOneEmptyState(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        subtitle = "Credential module settings coming soon (Biometrics & Encryption)",
                    )
                }

                composable(CredentialRoutes.EDIT) {
                    CredentialAddScreen(
                        formState = formState,
                        onTitleChange = viewModel::updateFormTitle,
                        onUsernameChange = viewModel::updateFormUsername,
                        onEmailChange = viewModel::updateFormEmail,
                        onPasswordChange = viewModel::updateFormPassword,
                        onUrlChange = viewModel::updateFormUrl,
                        onCategoryChange = viewModel::updateFormCategory,
                        onNotesChange = viewModel::updateFormNotes,
                        onFavoriteChange = viewModel::updateFormFavorite,
                        onSave = viewModel::saveForm,
                        onSaveSuccess = {
                            viewModel.resetSaveSuccess()
                            navController.popBackStack()
                        },
                    )
                }
            }

            // Floating bottom bar overlay — transparent behind the pill
            if (isOnMainScreen) {
                TrackOneBottomBar(
                    items = credentialBottomNavItems,
                    selectedRoute = currentRoute,
                    onItemSelected = { item ->
                        if (item.route != currentRoute) {
                            if (item.route == CredentialRoutes.ADD) {
                                viewModel.initAddForm()
                            }
                            navController.navigate(item.route) {
                                popUpTo(CredentialRoutes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
    }
}
