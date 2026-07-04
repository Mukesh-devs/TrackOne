package com.dev.trackone.feature.expiry.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
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
import com.dev.trackone.feature.expiry.presentation.ExpiryViewModel
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryAddScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryDetailScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryExpiredScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryHomeScreen

/**
 * Route constants for Expiry module internal navigation.
 */
object ExpiryRoutes {
    const val HOME = "expiry_home"
    const val ADD = "expiry_add"
    const val EXPIRED = "expiry_expired"
    const val SETTINGS = "expiry_settings"
    const val DETAIL = "expiry_detail"
    const val EDIT = "expiry_edit"
}

/**
 * Bottom navigation items for the Expiry module.
 */
private val expiryBottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, ExpiryRoutes.HOME),
    BottomNavItem("Add", Icons.Default.Add, ExpiryRoutes.ADD),
    BottomNavItem("Expired", Icons.Default.EventBusy, ExpiryRoutes.EXPIRED),
    BottomNavItem("Settings", Icons.Default.Settings, ExpiryRoutes.SETTINGS),
)

/**
 * Root composable for the Expiry module.
 * Contains its own NavHost and BottomNavigationBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryNavigation() {
    val navController = rememberNavController()
    val viewModel: ExpiryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ExpiryRoutes.HOME
    
    val isOnMainScreen = currentRoute in listOf(
        ExpiryRoutes.HOME, ExpiryRoutes.ADD, ExpiryRoutes.EXPIRED, ExpiryRoutes.SETTINGS
    )

    Scaffold(
        topBar = {
            if (isOnMainScreen) {
                TrackOneTopBar(
                    title = when (currentRoute) {
                        ExpiryRoutes.HOME -> "Expiry Tracker"
                        ExpiryRoutes.ADD -> "Add Item"
                        ExpiryRoutes.EXPIRED -> "Expired Items"
                        ExpiryRoutes.SETTINGS -> "Settings"
                        else -> "Expiry"
                    },
                    showSearchIcon = currentRoute == ExpiryRoutes.HOME,
                    onSearchClick = { viewModel.toggleSearch() },
                )
            } else {
                TrackOneTopBar(
                    title = if (uiState.selectedGroup != null) uiState.selectedGroup!!.name else "Details",
                    showBackButton = true,
                    onBackClick = {
                        viewModel.clearGroupSelection()
                        viewModel.clearItemSelection()
                        navController.popBackStack()
                    },
                )
            }
        },
        bottomBar = {
            if (isOnMainScreen) {
                TrackOneBottomBar(
                    items = expiryBottomNavItems,
                    selectedRoute = currentRoute,
                    onItemSelected = { item ->
                        if (item.route != currentRoute) {
                            if (item.route == ExpiryRoutes.ADD) {
                                viewModel.initAddForm()
                            }
                            navController.navigate(item.route) {
                                popUpTo(ExpiryRoutes.HOME) { inclusive = false }
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
            startDestination = ExpiryRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(ExpiryRoutes.HOME) {
                ExpiryHomeScreen(
                    uiState = uiState,
                    onGroupClick = { group ->
                        viewModel.selectGroup(group)
                        navController.navigate(ExpiryRoutes.DETAIL)
                    },
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                )
            }

            composable(ExpiryRoutes.ADD) {
                ExpiryAddScreen(
                    formState = formState,
                    onNameChange = viewModel::updateFormName,
                    onProviderChange = viewModel::updateFormProvider,
                    onLabelChange = viewModel::updateFormLabel,
                    onPurchasedDateChange = viewModel::updateFormPurchasedDate,
                    onExpiryDateChange = viewModel::updateFormExpiryDate,
                    onNotesChange = viewModel::updateFormNotes,
                    onAmountChange = viewModel::updateFormAmount,
                    onUsernameChange = viewModel::updateFormUsername,
                    onEmailChange = viewModel::updateFormEmail,
                    onPasswordChange = viewModel::updateFormPassword,
                    onSave = viewModel::saveForm,
                    onSaveSuccess = {
                        viewModel.resetSaveSuccess()
                        navController.navigate(ExpiryRoutes.HOME) {
                            popUpTo(ExpiryRoutes.HOME) { inclusive = true }
                        }
                    },
                )
            }

            composable(ExpiryRoutes.EXPIRED) {
                ExpiryExpiredScreen(
                    uiState = uiState,
                    onGroupClick = { group ->
                        viewModel.selectGroup(group)
                        navController.navigate(ExpiryRoutes.DETAIL)
                    },
                )
            }

            composable(ExpiryRoutes.SETTINGS) {
                // Placeholder settings screen
                com.dev.trackone.core.ui.component.TrackOneEmptyState(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "Expiry module settings coming soon",
                )
            }

            composable(ExpiryRoutes.DETAIL) {
                uiState.selectedGroup?.let { group ->
                    ExpiryDetailScreen(
                        group = group,
                        onEditItem = { item ->
                            viewModel.initEditForm(item)
                            navController.navigate(ExpiryRoutes.EDIT)
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                        },
                    )
                }
            }

            composable(ExpiryRoutes.EDIT) {
                ExpiryAddScreen(
                    formState = formState,
                    onNameChange = viewModel::updateFormName,
                    onProviderChange = viewModel::updateFormProvider,
                    onLabelChange = viewModel::updateFormLabel,
                    onPurchasedDateChange = viewModel::updateFormPurchasedDate,
                    onExpiryDateChange = viewModel::updateFormExpiryDate,
                    onNotesChange = viewModel::updateFormNotes,
                    onAmountChange = viewModel::updateFormAmount,
                    onUsernameChange = viewModel::updateFormUsername,
                    onEmailChange = viewModel::updateFormEmail,
                    onPasswordChange = viewModel::updateFormPassword,
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
