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
import androidx.compose.runtime.Composable
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Module indicator header
            ModuleIndicator(
                modules = modules,
                currentPage = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
            )

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

/**
 * Module indicator showing the current module with icon, title, and page dots.
 */
@Composable
private fun ModuleIndicator(
    modules: List<com.dev.trackone.core.navigation.ModuleEntry>,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = Dimens.SpacingSm, bottom = Dimens.SpacingSm), // Reduced top padding
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
        }

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
