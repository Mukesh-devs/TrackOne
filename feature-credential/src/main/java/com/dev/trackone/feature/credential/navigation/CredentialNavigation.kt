package com.dev.trackone.feature.credential.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dev.trackone.core.ui.component.FloatingNavItem
import com.dev.trackone.core.ui.component.TrackOneFloatingNav
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.credential.presentation.CredentialViewModel
import com.dev.trackone.feature.credential.presentation.screen.CredentialAddScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialDetailsScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialHomeScreen
import com.dev.trackone.feature.credential.presentation.screen.CredentialSettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch

object CredentialRoutes {
    const val MAIN = "credential_main"
    const val HOME = "credential_home"
    const val ADD = "credential_add"
    const val SETTINGS = "credential_settings"
    const val EDIT = "credential_edit"
    const val DETAILS = "credential_details/{title}"

    fun createDetailsRoute(title: String): String =
        "credential_details/${URLEncoder.encode(title, StandardCharsets.UTF_8.toString())}"
}

private val credentialTabs = listOf(
    FloatingNavItem("Home", Icons.Default.Home, CredentialRoutes.HOME),
    FloatingNavItem("Add", Icons.Default.Add, CredentialRoutes.ADD, emphasized = true),
    FloatingNavItem("Settings", Icons.Default.Settings, CredentialRoutes.SETTINGS),
)

/**
 * Root of the Credential module. Self-contained (each screen owns its header)
 * and section switching is state-based — no pager, no swipe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: CredentialViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(CredentialRoutes.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    // Writes are cloud-first, so a failed save/delete means nothing changed
    // anywhere. Surface it rather than letting the action look like it worked.
    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }
    LaunchedEffect(formState.saveError) {
        formState.saveError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveError()
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = CredentialRoutes.MAIN,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
            ) {
                composable(CredentialRoutes.MAIN) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 14 }) togetherWith
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
                                    CredentialRoutes.HOME -> CredentialHomeScreen(
                                        uiState = uiState,
                                        onSearchQueryChange = viewModel::onSearchQueryChange,
                                        onCategoryFilterChange = viewModel::setCategoryFilter,
                                        onGroupClick = { title ->
                                            navController.navigate(CredentialRoutes.createDetailsRoute(title))
                                        },
                                    )

                                    CredentialRoutes.ADD -> Column(modifier = Modifier.fillMaxSize()) {
                                        ScreenTitle("Add Credential")
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
                                                showMessage("Credential saved")
                                            },
                                        )
                                    }

                                    CredentialRoutes.SETTINGS -> CredentialSettingsScreen(
                                        csvProvider = viewModel::buildCsv,
                                        onMessage = ::showMessage,
                                    )

                                    else -> Unit
                                }
                            }
                        }

                        TrackOneFloatingNav(
                            items = credentialTabs,
                            selectedRoute = currentTab,
                            onSelect = { route ->
                                if (route == CredentialRoutes.ADD) viewModel.initAddForm()
                                currentTab = route
                            },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }

                composable(
                    route = CredentialRoutes.DETAILS,
                    arguments = listOf(navArgument("title") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val encoded = backStackEntry.arguments?.getString("title") ?: ""
                    val groupTitle = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())

                    CredentialDetailsScreen(
                        groupTitle = groupTitle,
                        uiState = uiState,
                        onBack = { navController.popBackStack() },
                        onEditItem = { item ->
                            viewModel.initEditForm(item)
                            navController.navigate(CredentialRoutes.EDIT)
                        },
                        onDeleteItem = { item ->
                            viewModel.deleteItem(item)
                            showMessage("Deleted \"${item.title}\"")
                        },
                        onCopied = ::showMessage,
                    )
                }

                composable(CredentialRoutes.EDIT) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScreenTitle("Edit Credential")
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
                                showMessage("Credential updated")
                            },
                        )
                    }
                }
            }

            // Sits above the floating nav so messages are never hidden by it.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp),
            )
        }
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.SpacingXl,
                bottom = Dimens.SpacingMd,
            ),
    )
}
