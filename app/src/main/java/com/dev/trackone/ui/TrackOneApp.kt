package com.dev.trackone.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * Root composable for TrackOne.
 *
 * Hosts the dashboard plus one full-screen page per feature module, with the
 * navigation drawer and the Settings overlay layered above them.
 */
@Composable
fun TrackOneApp(
    viewModel: MainViewModel,
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

    // Dashboard ViewModel
    val dashboardViewModel: com.dev.trackone.ui.dashboard.DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val expiringTodayCount by dashboardViewModel.expiringTodayCount.collectAsStateWithLifecycle()
    val todayExpenseTotal by dashboardViewModel.todayExpenseTotal.collectAsStateWithLifecycle()
    val credentialCount by dashboardViewModel.credentialCount.collectAsStateWithLifecycle()

    // Sync state with ViewModel
    LaunchedEffect(currentModuleIndex) {
        viewModel.setCurrentPage(currentModuleIndex)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var showSettingsScreen by remember { mutableStateOf(false) }

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
    
    // System back closes the Settings overlay instead of exiting the app.
    androidx.activity.compose.BackHandler(enabled = showSettingsScreen) {
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
        )
    }
}
