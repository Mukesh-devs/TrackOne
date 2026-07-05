package com.dev.trackone.feature.expiry.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expiry.domain.model.ExpiryGroup
import com.dev.trackone.feature.expiry.presentation.ExpiryUiState

/**
 * Screen showing only expired items, sorted by expiry date.
 */
@Composable
fun ExpiryExpiredScreen(
    uiState: ExpiryUiState,
    onGroupClick: (ExpiryGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            TrackOneLoadingState(modifier = modifier)
        }
        uiState.expiredItems.isEmpty() -> {
            TrackOneEmptyState(
                icon = Icons.Default.EventBusy,
                title = "No expired items",
                subtitle = "All your subscriptions and memberships are active!",
                modifier = modifier,
            )
        }
        else -> {
            val expiredGroups = uiState.expiredItems
                .groupBy { it.provider }
                .map { (provider, items) -> ExpiryGroup(name = provider, items = items) }

            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = Dimens.SpacingLg,
                    vertical = Dimens.SpacingSm,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            ) {
                items(
                    items = expiredGroups,
                    key = { it.name },
                ) { group ->
                    ExpiredItemCard(group = group, onClick = { onGroupClick(group) })
                }

                // Bottom spacer for floating bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ExpiredItemCard(
    group: ExpiryGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Reuses the same card style as home screen but with expired styling
    androidx.compose.foundation.layout.Row(modifier = modifier) {
        com.dev.trackone.core.ui.component.TrackOneCard(onClick = onClick) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    androidx.compose.material3.Text(
                        text = group.name,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                    if (group.hasMultiple) {
                        androidx.compose.material3.Text(
                            text = "${group.itemCount} items expired",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        val item = group.items.first()
                        androidx.compose.material3.Text(
                            text = com.dev.trackone.core.common.util.DateUtils.getExpiryLabel(item.expiryDate),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        )
                    }
                }
                if (group.totalAmount > 0) {
                    androidx.compose.material3.Text(
                        text = com.dev.trackone.core.common.util.CurrencyUtils.formatAmount(group.totalAmount),
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
