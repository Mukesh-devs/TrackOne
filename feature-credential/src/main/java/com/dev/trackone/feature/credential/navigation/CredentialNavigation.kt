package com.dev.trackone.feature.credential.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dev.trackone.core.ui.component.BottomNavItem
import com.dev.trackone.core.ui.component.TrackOneBottomBar
import com.dev.trackone.core.ui.component.TrackOneTopBar
import com.dev.trackone.feature.credential.presentation.CredentialViewModel
import com.dev.trackone.feature.credential.presentation.screen.CredentialAddScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialHomeScreen

object CredentialRoutes {
    const val HOME = "credential_home"
    const val ADD = "credential_add"
    const val SETTINGS = "credential_settings"
    const val EDIT = "credential_edit"
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
                TrackOneTopBar(
                    title = "Edit Credential",
                    showBackButton = true,
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
        },
        bottomBar = {
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
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CredentialRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(CredentialRoutes.HOME) {
                CredentialHomeScreen(
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                    onEditItem = { item ->
                        viewModel.initEditForm(item)
                        navController.navigate(CredentialRoutes.EDIT)
                    },
                    onDeleteItem = { item ->
                        viewModel.deleteItem(item)
                    },
                    onToggleFavorite = { item ->
                        viewModel.toggleFavorite(item)
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
    }
}
