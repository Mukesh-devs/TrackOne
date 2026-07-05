package com.dev.trackone.feature.credential.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneChip
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.component.TrackOneSearchBar
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.credential.presentation.CredentialUiState

@Composable
fun CredentialHomeScreen(
    uiState: CredentialUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedItems = uiState.items.groupBy { it.title }.toSortedMap(String.CASE_INSENSITIVE_ORDER)

    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.isSearchActive,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TrackOneSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                isActive = uiState.isSearchActive,
                onActiveChange = {},
                placeholder = "Search credentials...",
                modifier = Modifier.padding(bottom = Dimens.SpacingSm),
            )
        }

        when {
            uiState.isLoading -> {
                TrackOneLoadingState(message = "Loading credentials...")
            }
            uiState.items.isEmpty() && !uiState.isSearchActive && uiState.selectedCategory == null -> {
                TrackOneEmptyState(
                    icon = Icons.Default.Lock,
                    title = "No credentials saved",
                    subtitle = "Tap + to securely store your passwords",
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = Dimens.SpacingLg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    if (uiState.categories.isNotEmpty()) {
                        item {
                            CategoryFilterRow(
                                categories = uiState.categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = onCategoryFilterChange,
                            )
                        }
                    }

                    if (groupedItems.isEmpty()) {
                        item {
                            TrackOneEmptyState(
                                icon = Icons.Default.Lock,
                                title = "No results found",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXxl),
                            )
                        }
                    } else {
                        items(groupedItems.keys.toList()) { title ->
                            val groupCount = groupedItems[title]?.size ?: 0
                            CredentialGroupCard(
                                title = title,
                                count = groupCount,
                                onClick = { onGroupClick(title) },
                                modifier = Modifier.padding(horizontal = Dimens.SpacingLg)
                            )
                        }
                    }

                    // Bottom spacer for floating bar
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.SpacingLg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        modifier = Modifier.padding(vertical = Dimens.SpacingSm),
    ) {
        item {
            TrackOneChip(
                label = "All",
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(categories) { category ->
            TrackOneChip(
                label = category,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun CredentialGroupCard(
    title: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.SpacingSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (count > 1) {
                    Text(
                        text = "$count accounts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
