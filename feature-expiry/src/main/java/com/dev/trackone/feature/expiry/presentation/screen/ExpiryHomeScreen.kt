package com.dev.trackone.feature.expiry.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.component.TrackOneSearchBar
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expiry.domain.model.ExpiryGroup
import com.dev.trackone.feature.expiry.presentation.ExpiryUiState

/**
 * Home screen showing grouped expiry items.
 * Items with the same name are grouped into a single card.
 */
@Composable
fun ExpiryHomeScreen(
    uiState: ExpiryUiState,
    onGroupClick: (ExpiryGroup) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
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
                placeholder = "Search by name, provider, email...",
                modifier = Modifier.padding(bottom = Dimens.SpacingSm),
            )
        }

        when {
            uiState.isLoading -> {
                TrackOneLoadingState(message = "Loading your items...")
            }
            uiState.groups.isEmpty() -> {
                TrackOneEmptyState(
                    icon = Icons.Default.Inbox,
                    title = "No expiry items yet",
                    subtitle = "Tap + to add your first subscription, membership, or expiry item",
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = Dimens.SpacingLg,
                        vertical = Dimens.SpacingSm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                ) {
                    items(
                        items = uiState.groups,
                        key = { it.name },
                    ) { group ->
                        ExpiryGroupCard(
                            group = group,
                            onClick = { onGroupClick(group) },
                        )
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
private fun ExpiryGroupCard(
    group: ExpiryGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = group.items.first()
    val nearestExpiry = group.nearestExpiry

    TrackOneCard(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (group.hasExpiredItems) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(Dimens.IconSizeLg),
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingMd))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (group.hasMultiple) {
                            Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Text(
                                    text = "${group.itemCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.provider,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (nearestExpiry != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = DateUtils.getExpiryLabel(nearestExpiry),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (DateUtils.isExpired(nearestExpiry)) {
                                MaterialTheme.colorScheme.error
                            } else if (DateUtils.isWithinDays(nearestExpiry, 7)) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                if (group.totalAmount > 0) {
                    Text(
                        text = CurrencyUtils.formatAmount(group.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
