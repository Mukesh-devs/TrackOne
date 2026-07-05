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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ExpiryGroupCard(
    group: ExpiryGroup,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = group.items.first()
    
    // If nearestExpiry is null, all items are expired. Use the max expiry date of the group as fallback.
    val effectiveExpiry = group.nearestExpiry ?: group.items.maxOfOrNull { it.expiryDate } ?: item.expiryDate
    val nearestItem = group.items.find { it.expiryDate == effectiveExpiry } ?: item
    val daysLeft = DateUtils.daysUntil(effectiveExpiry)
    val isExpired = daysLeft < 0

    val urgencyColor = when {
        isExpired -> MaterialTheme.colorScheme.error
        daysLeft in 0..10 -> MaterialTheme.colorScheme.error // Urgent
        daysLeft in 11..30 -> MaterialTheme.colorScheme.tertiary // Approaching
        else -> MaterialTheme.colorScheme.primary // Safe
    }

    val totalDuration = effectiveExpiry - nearestItem.purchasedDate
    val remaining = effectiveExpiry - System.currentTimeMillis()
    val progress = if (totalDuration > 0) {
        (remaining.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress"
    )

    TrackOneCard(
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Name + Badge
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

                // Right: Amount
                if (group.totalAmount > 0) {
                    Text(
                        text = CurrencyUtils.formatAmount(group.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Dates
                val purchasedStr = DateUtils.formatDateShort(nearestItem.purchasedDate)
                val expiryStr = DateUtils.formatDateShort(effectiveExpiry)
                Text(
                    text = "$purchasedStr  →  $expiryStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.2.sp
                )

                // Right: Days badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = urgencyColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = urgencyColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExpired) "Expired" else "${daysLeft}d",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = urgencyColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = urgencyColor,
                trackColor = urgencyColor.copy(alpha = 0.12f)
            )
        }
    }
}
