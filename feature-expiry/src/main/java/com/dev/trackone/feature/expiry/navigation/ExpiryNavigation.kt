package com.dev.trackone.feature.expiry.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.trackone.feature.expiry.presentation.ExpiryViewModel
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryAddScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryDetailScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryExpiredScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryHomeScreen

/** Route constants for Expiry module internal navigation. */
object ExpiryRoutes {
    const val MAIN = "expiry_main"
    const val HOME = "expiry_home"
    const val ADD = "expiry_add"
    const val EXPIRED = "expiry_expired"
    const val DETAIL = "expiry_detail"
    const val EDIT = "expiry_edit"
}

private data class ExpiryTab(val label: String, val icon: ImageVector, val route: String)

private val expiryTabs = listOf(
    ExpiryTab("Home", Icons.Filled.Home, ExpiryRoutes.HOME),
    ExpiryTab("Add", Icons.Filled.Add, ExpiryRoutes.ADD),
    ExpiryTab("Expired", Icons.Filled.EventBusy, ExpiryRoutes.EXPIRED),
)

/**
 * Root of the Expiry module. Fully self-contained (own header + floating bottom
 * nav). Sections switch purely by the bottom nav — only the selected section is
 * composed, so there's no swipe gesture and no page fly-through.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModel: ExpiryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    var currentRoute by remember { mutableStateOf(ExpiryRoutes.HOME) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        NavHost(
            navController = navController,
            startDestination = ExpiryRoutes.MAIN,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            composable(ExpiryRoutes.MAIN) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Only the selected section renders — no pager, no swipe.
                    when (currentRoute) {
                        ExpiryRoutes.HOME -> ExpiryHomeScreen(
                            uiState = uiState,
                            onItemClick = { item ->
                                viewModel.selectItem(item)
                                navController.navigate(ExpiryRoutes.DETAIL)
                            },
                            onTrashItem = { viewModel.trashItem(it) },
                            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                        )

                        ExpiryRoutes.ADD -> Column(modifier = Modifier.fillMaxSize()) {
                            ScreenTitle("Add Item")
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
                                    currentRoute = ExpiryRoutes.HOME
                                },
                            )
                        }

                        ExpiryRoutes.EXPIRED -> ExpiryExpiredScreen(
                            items = uiState.allItems.filter { it.id in uiState.trashedIds },
                            onRestore = { viewModel.restoreItem(it) },
                            onDeleteForever = { viewModel.deleteForever(it) },
                        )

                        else -> Unit
                    }

                    ExpiryFloatingNav(
                        tabs = expiryTabs,
                        selectedRoute = currentRoute,
                        onSelect = { tab ->
                            if (tab.route == ExpiryRoutes.ADD) viewModel.initAddForm()
                            currentRoute = tab.route
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            composable(ExpiryRoutes.DETAIL) {
                uiState.selectedItem?.let { item ->
                    ExpiryDetailScreen(
                        item = item,
                        onBack = {
                            viewModel.clearItemSelection()
                            navController.popBackStack()
                        },
                        onEdit = { edited ->
                            viewModel.initEditForm(edited)
                            navController.navigate(ExpiryRoutes.EDIT)
                        },
                        onDelete = { deleted ->
                            viewModel.deleteItem(deleted)
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable(ExpiryRoutes.EDIT) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ScreenTitle("Edit Item")
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
}

// ── Floating bottom navigation (selected item = filled circle) ────────────────

@Composable
private fun ExpiryFloatingNav(
    tabs: List<ExpiryTab>,
    selectedRoute: String,
    onSelect: (ExpiryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
    )
}
