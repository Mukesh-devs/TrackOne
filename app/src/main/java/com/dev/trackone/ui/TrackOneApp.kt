package com.dev.trackone.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.theme.Dimens

/**
 * Root composable for TrackOne.
 * Hosts a HorizontalPager that lets users swipe between feature modules.
 * Each page is a full-screen module with its own internal navigation.
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { modules.size },
    )

    // Sync pager state with ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.setCurrentPage(page)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentModuleTitle = if (pagerState.currentPage in modules.indices) modules[pagerState.currentPage].title else ""
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            com.dev.trackone.core.ui.theme.DynamicModuleTheme(moduleTitle = currentModuleTitle) {
                ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                drawerContainerColor = androidx.compose.ui.graphics.Color.White,
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
                        color = androidx.compose.ui.graphics.Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                modules.forEachIndexed { index, module ->
                    val isSelected = pagerState.currentPage == index
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
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = androidx.compose.material3.NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.Black,
                            unselectedTextColor = androidx.compose.ui.graphics.Color.Black,
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                }
            }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
            // Module indicator header
                com.dev.trackone.core.ui.theme.DynamicModuleTheme(moduleTitle = currentModuleTitle) {
                    ModuleIndicator(
                        modules = modules,
                        currentPage = pagerState.currentPage,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } },
                        onCloudClick = { /* Will implement in future */ },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Module pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1,
                key = { modules[it].order },
            ) { pageIndex ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    modules[pageIndex].Content()
                }
            }
            }
        }
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
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
                    tint = MaterialTheme.colorScheme.onSurface
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        } // Close AnimatedVisibility
            
            IconButton(onClick = onCloudClick) {
                Icon(
                    imageVector = Icons.Outlined.Cloud,
                    contentDescription = "Cloud Sync",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } // Close outer Row

        Spacer(modifier = Modifier.height(2.dp)) // Drastically reduced spacer height

        // Page indicator dots
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            modules.forEachIndexed { index, _ ->
                val isSelected = index == currentPage

                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 24.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "dot_width",
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    label = "dot_color",
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
        }
    }
}
