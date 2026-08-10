package com.dev.trackone.ui

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.data.UserPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material.icons.filled.Swipe
import com.dev.trackone.core.ui.theme.Dimens
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import com.dev.trackone.ui.sync.CloudSyncBottomSheet

/**
 * Root composable for TrackOne.
 * Hosts a HorizontalPager that lets users swipe between feature modules.
 * Each page is a full-screen module with its own internal navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOneApp(
    viewModel: MainViewModel,
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val modules = viewModel.modules

    if (modules.isEmpty()) {
        // Fallback — shouldn't happen with proper module registration
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No modules available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    var showDashboard by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    var currentModuleIndex by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    var showSyncSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // Dashboard ViewModel
    val dashboardViewModel: com.dev.trackone.ui.dashboard.DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val expiringTodayCount by dashboardViewModel.expiringTodayCount.collectAsStateWithLifecycle()
    val todayExpenseTotal by dashboardViewModel.todayExpenseTotal.collectAsStateWithLifecycle()
    val credentialCount by dashboardViewModel.credentialCount.collectAsStateWithLifecycle()
    val syncSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sync state with ViewModel
    LaunchedEffect(currentModuleIndex) {
        viewModel.setCurrentPage(currentModuleIndex)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val syncUiState by viewModel.syncUiState.collectAsStateWithLifecycle()
    val isBackupPending by viewModel.isBackupPending.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showBackupScreen by remember { mutableStateOf(false) }

    val currentModuleTitle = if (currentModuleIndex in modules.indices) modules[currentModuleIndex].title else ""
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            com.dev.trackone.core.ui.theme.DynamicModuleTheme(moduleTitle = currentModuleTitle) {
                ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 4.dp
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "TrackOne",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Home / Dashboard item
                NavigationDrawerItem(
                    label = { 
                        Text(
                            text = "Home",
                            fontWeight = if (showDashboard) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                        ) 
                    },
                    selected = showDashboard,
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            showDashboard = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                
                modules.forEachIndexed { index, module ->
                    val isSelected = !showDashboard && currentModuleIndex == index
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = module.title,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                            ) 
                        },
                        selected = isSelected,
                        icon = { Icon(module.icon, contentDescription = null) },
                        onClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                showDashboard = false
                                currentModuleIndex = index
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                
                NavigationDrawerItem(
                    label = { 
                        Text(
                            text = "Settings",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        ) 
                    },
                    selected = false,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            showSettingsScreen = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val versionName = remember(context) {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (e: Exception) {
                        "Unknown"
                    }
                }
                
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.labelMedium,
                    color = androidx.compose.ui.graphics.Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, top = 8.dp)
                )
            }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (showDashboard) {
                    // Dashboard home screen
                    com.dev.trackone.ui.dashboard.DashboardScreen(
                        expiringTodayCount = expiringTodayCount,
                        todayExpenseTotal = todayExpenseTotal,
                        credentialCount = credentialCount,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onCloudClick = { showSyncSheet = true },
                        isBackupPending = isBackupPending,
                        isSignedIn = isSignedIn,
                        onExpiryClick = {
                            showDashboard = false
                            currentModuleIndex = modules.indexOfFirst { it.title.contains("Expiry", ignoreCase = true) }.coerceAtLeast(0)
                        },
                        onExpenseClick = {
                            showDashboard = false
                            currentModuleIndex = modules.indexOfFirst { it.title.contains("Expense", ignoreCase = true) }.coerceAtLeast(0)
                        },
                        onCredentialClick = {
                            showDashboard = false
                            currentModuleIndex = modules.indexOfFirst { it.title.contains("Credential", ignoreCase = true) }.coerceAtLeast(0)
                        },
                    )
                } else {
                    // Back press returns to dashboard
                    androidx.activity.compose.BackHandler(enabled = true) {
                        showDashboard = true
                    }
                    // Full-screen module — each module owns its header + bottom nav.
                    // No app-level chrome (drawer / cloud / shared top bar) here.
                    androidx.compose.animation.AnimatedContent(
                        targetState = currentModuleIndex,
                        transitionSpec = {
                            androidx.compose.animation.fadeIn(
                                androidx.compose.animation.core.tween(220)
                            ) togetherWith androidx.compose.animation.fadeOut(
                                androidx.compose.animation.core.tween(220)
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "ModuleTransition",
                    ) { targetIndex ->
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            modules[targetIndex].Content()
                        }
                    }
                }
                
            }
        }
    }
    
    if (showSyncSheet) {
        com.dev.trackone.core.ui.theme.DynamicModuleTheme(moduleTitle = currentModuleTitle) {
            CloudSyncBottomSheet(
                onDismissRequest = {
                    showSyncSheet = false
                    viewModel.resetSyncState()
                },
                sheetState = syncSheetState,
                uiState = syncUiState,
                onSignInClick = onSignInClick,
                onSignOutClick = onSignOutClick,
                onBackupClick = { viewModel.onBackup() },
                onRestoreClick = { viewModel.onRestore() }
            )
        }
    }

    // System back closes the Backup / Settings overlays instead of exiting the app.
    androidx.activity.compose.BackHandler(enabled = showBackupScreen) {
        showBackupScreen = false
        showSettingsScreen = true
    }
    androidx.activity.compose.BackHandler(enabled = showSettingsScreen && !showBackupScreen) {
        showSettingsScreen = false
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = showSettingsScreen,
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut()
    ) {
        com.dev.trackone.ui.settings.SettingsScreen(
            onNavigateBack = { showSettingsScreen = false },
            onNavigateToBackup = { 
                showSettingsScreen = false
                showBackupScreen = true 
            }
        )
    }

    androidx.compose.animation.AnimatedVisibility(
        visible = showBackupScreen,
        enter = androidx.compose.animation.slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut()
    ) {
        com.dev.trackone.ui.settings.BackupScreen(
            syncUiState = syncUiState,
            onBackupClick = { viewModel.onBackup() },
            onRestoreClick = { viewModel.onRestore() },
            onClose = { 
                showBackupScreen = false 
                showSettingsScreen = true
            }
        )
    }
}

/**
 * Module indicator showing the current module with icon, title, and page dots.
 */
@Composable
private fun ModuleIndicator(
    modules: List<com.dev.trackone.core.navigation.ModuleEntry>,
    currentPage: Int,
    onMenuClick: () -> Unit,
    onCloudClick: () -> Unit,
    isBackupPending: Boolean?,
    isSignedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(top = Dimens.SpacingSm, bottom = Dimens.SpacingSm), // Reduced top padding
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top row with Menu, Title/Icon, Cloud
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Current module title and icon
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (currentPage in modules.indices) {
                    val module = modules[currentPage]
                    Icon(
                        imageVector = module.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } // Close AnimatedVisibility
            
            IconButton(onClick = onCloudClick) {
                val cloudIcon = if (!isSignedIn) {
                    Icons.Outlined.CloudOff
                } else when (isBackupPending) {
                    true -> Icons.Outlined.CloudUpload
                    false -> Icons.Outlined.CloudDone
                    null -> Icons.Outlined.Cloud
                }
                val cloudDesc = if (!isSignedIn) {
                    "Not Signed In"
                } else when (isBackupPending) {
                    true -> "Backup Pending"
                    false -> "Synced"
                    null -> "Cloud Sync"
                }
                Icon(
                    imageVector = cloudIcon,
                    contentDescription = cloudDesc,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        } // Close outer Row
    }
}
