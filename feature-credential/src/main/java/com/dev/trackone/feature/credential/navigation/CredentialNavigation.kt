package com.dev.trackone.feature.credential.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dev.trackone.core.ui.component.BottomNavItem
import com.dev.trackone.core.ui.component.FloatingNavItem
import com.dev.trackone.core.ui.component.TrackOneFloatingNav
import com.dev.trackone.core.ui.component.TrackOneTopBar
import com.dev.trackone.feature.credential.presentation.CredentialViewModel
import com.dev.trackone.feature.credential.presentation.screen.CredentialAddScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialDetailsScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialHomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object CredentialRoutes {
    const val MAIN_PAGER = "credential_main_pager"
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

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun CredentialNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: CredentialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(CredentialRoutes.HOME) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentNavRoute = navBackStackEntry?.destination?.route

    val currentRoute = if (currentNavRoute == CredentialRoutes.MAIN_PAGER || currentNavRoute == null) {
        currentTab
    } else {
        currentNavRoute
    }
    
    val isOnMainScreen = currentNavRoute == CredentialRoutes.MAIN_PAGER || currentNavRoute == null

    com.dev.trackone.core.ui.theme.CredentialModuleTheme {
    Scaffold(
        topBar = {
            if (isOnMainScreen) {
                if (currentRoute == CredentialRoutes.HOME) {
                    // Icon + title header, matching the Expense/Expiry modules.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Credential Management",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    TrackOneTopBar(
                        title = when (currentRoute) {
                            CredentialRoutes.ADD -> "Add Credential"
                            CredentialRoutes.SETTINGS -> "Settings"
                            else -> "Credentials"
                        },
                    )
                }
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
                startDestination = CredentialRoutes.MAIN_PAGER,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
            ) {
                composable(CredentialRoutes.MAIN_PAGER) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            (fadeIn(tween(220)) + androidx.compose.animation.slideInHorizontally(tween(220)) { it / 14 }) togetherWith
                                fadeOut(tween(160))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "credential_tabs",
                    ) { tab ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                        ) {
                    when (tab) {
                            CredentialRoutes.HOME -> {
                                CredentialHomeScreen(
                                    uiState = uiState,
                                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                    onGroupClick = { title ->
                                        navController.navigate(CredentialRoutes.createDetailsRoute(title))
                                    },
                                    isRefreshing = uiState.isRefreshing,
                                    onRefresh = { viewModel.refresh() }
                                )
                            }
                            CredentialRoutes.ADD -> {
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
                                        currentTab = CredentialRoutes.HOME
                                    },
                                )
                            }
                            CredentialRoutes.SETTINGS -> {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                com.dev.trackone.feature.credential.presentation.screen.CredentialSettingsScreen(
                                    onExportCsv = { uri -> viewModel.exportCsv(uri, context) }
                                )
                            }
                            else -> Unit
                        }
                        }
                    }
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

            // Floating bottom nav (shared, icon-only, center Add emphasized)
            if (isOnMainScreen) {
                TrackOneFloatingNav(
                    items = credentialBottomNavItems.map {
                        FloatingNavItem(it.label, it.icon, it.route, emphasized = it.route == CredentialRoutes.ADD)
                    },
                    selectedRoute = currentRoute,
                    onSelect = { route ->
                        if (route == CredentialRoutes.ADD) viewModel.initAddForm()
                        currentTab = route
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
    }
}
