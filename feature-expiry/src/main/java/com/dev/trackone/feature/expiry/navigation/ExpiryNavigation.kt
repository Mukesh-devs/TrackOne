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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneFAB
import com.dev.trackone.feature.expiry.presentation.ExpiryViewModel
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryAddScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryDetailScreen
import com.dev.trackone.feature.expiry.presentation.screen.ExpiryHomeScreen
import kotlinx.coroutines.launch

/** Route constants for Expiry module internal navigation. */
object ExpiryRoutes {
    const val MAIN = "expiry_main"
    const val HOME = "expiry_home"
    const val ADD = "expiry_add"
    const val CATEGORIES = "expiry_categories"
    const val STATS = "expiry_stats"
    const val MORE = "expiry_more"
    const val DETAIL = "expiry_detail"
    const val EDIT = "expiry_edit"
}

private data class ExpiryTab(val label: String, val icon: ImageVector, val route: String)

private val expiryTabs = listOf(
    ExpiryTab("Home", Icons.Filled.Home, ExpiryRoutes.HOME),
    ExpiryTab("Add Item", Icons.Filled.Add, ExpiryRoutes.ADD),
    ExpiryTab("Categories", Icons.Filled.GridView, ExpiryRoutes.CATEGORIES),
    ExpiryTab("Stats", Icons.Filled.PieChart, ExpiryRoutes.STATS),
    ExpiryTab("More", Icons.Filled.MoreHoriz, ExpiryRoutes.MORE),
)

/**
 * Root of the Expiry module. Fully self-contained: it renders its own header
 * (inside each screen), a floating bottom navigation bar, and a FAB — none of
 * the app-level chrome (drawer / cloud / shared top bar) appears here.
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

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { expiryTabs.size })
    val currentRoute = expiryTabs[pagerState.currentPage].route

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
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        when (expiryTabs[page].route) {
                            ExpiryRoutes.HOME -> ExpiryHomeScreen(
                                uiState = uiState,
                                onItemClick = { item ->
                                    viewModel.selectItem(item)
                                    navController.navigate(ExpiryRoutes.DETAIL)
                                },
                                onDeleteItem = { viewModel.deleteItem(it) },
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
                                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                                    },
                                )
                            }

                            ExpiryRoutes.CATEGORIES -> Placeholder(
                                title = "Categories",
                                icon = Icons.Filled.Category,
                                subtitle = "Group your items by category — coming soon",
                            )

                            ExpiryRoutes.STATS -> Placeholder(
                                title = "Stats",
                                icon = Icons.Filled.PieChart,
                                subtitle = "Insights on your spending and renewals — coming soon",
                            )

                            ExpiryRoutes.MORE -> Placeholder(
                                title = "More",
                                icon = Icons.Filled.MoreHoriz,
                                subtitle = "Additional options — coming soon",
                            )
                        }
                    }

                    // FAB — only on Home
                    if (currentRoute == ExpiryRoutes.HOME) {
                        TrackOneFAB(
                            onClick = {
                                viewModel.initAddForm()
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            contentDescription = "Add item",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 110.dp),
                        )
                    }

                    ExpiryFloatingNav(
                        tabs = expiryTabs,
                        selectedRoute = currentRoute,
                        onSelect = { tab ->
                            val index = expiryTabs.indexOfFirst { it.route == tab.route }
                            if (index >= 0) {
                                if (tab.route == ExpiryRoutes.ADD) viewModel.initAddForm()
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
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
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
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

// ── Small helpers ─────────────────────────────────────────────────────────────

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun Placeholder(title: String, icon: ImageVector, subtitle: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(title)
        TrackOneEmptyState(icon = icon, title = title, subtitle = subtitle)
    }
}
