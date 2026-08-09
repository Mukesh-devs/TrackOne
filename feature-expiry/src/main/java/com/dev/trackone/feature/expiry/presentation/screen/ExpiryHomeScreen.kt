package com.dev.trackone.feature.expiry.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.theme.ActiveGreen
import com.dev.trackone.core.ui.theme.ExpiredRed
import com.dev.trackone.core.ui.theme.WarningAmber
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem
import com.dev.trackone.feature.expiry.presentation.ExpiryUiState

private enum class ExpiryFilter(val label: String) {
    ALL("All"),
    SOON("Expiring Soon"),
    EXPIRED("Expired"),
    OTHERS("Others"),
}

private const val SOON_THRESHOLD_DAYS = 30L

/**
 * Expiry home — a self-contained screen (own header, no shared app chrome):
 * greeting header + bell, search + filter, category pills, and a list of items.
 * Tap an item for its detail; swipe an expired item left to trash it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryHomeScreen(
    uiState: ExpiryUiState,
    onItemClick: (ExpiryItem) -> Unit,
    onDeleteItem: (ExpiryItem) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = System.currentTimeMillis()
    var filter by remember { mutableStateOf(ExpiryFilter.ALL) }
    val query = uiState.searchQuery.trim()

    val searched = remember(uiState.allItems, query) {
        uiState.allItems.filter { query.isBlank() || it.matchesQuery(query) }
    }

    val allCount = searched.size
    val expiredCount = searched.count { it.expiryDate < now }
    val soonCount = searched.count { it.expiryDate >= now && DateUtils.daysUntil(it.expiryDate) <= SOON_THRESHOLD_DAYS }
    val othersCount = searched.count { it.expiryDate >= now && DateUtils.daysUntil(it.expiryDate) > SOON_THRESHOLD_DAYS }

    val visible = remember(searched, filter) {
        when (filter) {
            ExpiryFilter.ALL -> searched.sortedWith(compareBy({ it.expiryDate < now }, { it.expiryDate }))
            ExpiryFilter.SOON -> searched.filter { it.expiryDate >= now && DateUtils.daysUntil(it.expiryDate) <= SOON_THRESHOLD_DAYS }
                .sortedBy { it.expiryDate }
            ExpiryFilter.EXPIRED -> searched.filter { it.expiryDate < now }.sortedBy { it.expiryDate }
            ExpiryFilter.OTHERS -> searched.filter { it.expiryDate >= now && DateUtils.daysUntil(it.expiryDate) > SOON_THRESHOLD_DAYS }
                .sortedBy { it.expiryDate }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ExpiryHeader(onBellClick = { filter = ExpiryFilter.SOON })

        SearchRow(query = uiState.searchQuery, onQueryChange = onSearchQueryChange)

        FilterPills(
            selected = filter,
            counts = mapOf(
                ExpiryFilter.ALL to allCount,
                ExpiryFilter.SOON to soonCount,
                ExpiryFilter.EXPIRED to expiredCount,
                ExpiryFilter.OTHERS to othersCount,
            ),
            onSelect = { filter = it },
        )

        when {
            uiState.isLoading -> TrackOneLoadingState(message = "Loading your items...")

            uiState.allItems.isEmpty() -> TrackOneEmptyState(
                icon = Icons.Default.Inbox,
                title = "No expiry items yet",
                subtitle = "Tap + to track your first subscription, membership, or bill",
            )

            visible.isEmpty() -> TrackOneEmptyState(
                icon = Icons.Default.SearchOff,
                title = "Nothing here",
                subtitle = if (query.isNotBlank()) "No items match \"$query\"" else "No ${filter.label.lowercase()} items",
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                items(items = visible, key = { it.id }) { item ->
                    if (item.expiryDate < now) {
                        SwipeToTrash(onDelete = { onDeleteItem(item) }) {
                            ExpiryItemCard(item = item, now = now, onClick = { onItemClick(item) })
                        }
                    } else {
                        ExpiryItemCard(item = item, now = now, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ExpiryHeader(onBellClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Expiry Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Track your items before they expire",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onBellClick) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Show items expiring soon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Search + filter button ────────────────────────────────────────────────────

@Composable
private fun SearchRow(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search items...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

// ── Filter pills ──────────────────────────────────────────────────────────────

@Composable
private fun FilterPills(
    selected: ExpiryFilter,
    counts: Map<ExpiryFilter, Int>,
    onSelect: (ExpiryFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ExpiryFilter.entries.forEach { f ->
            val accent = when (f) {
                ExpiryFilter.ALL -> MaterialTheme.colorScheme.primary
                ExpiryFilter.SOON -> WarningAmber
                ExpiryFilter.EXPIRED -> ExpiredRed
                ExpiryFilter.OTHERS -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val isSelected = selected == f
            Surface(
                onClick = { onSelect(f) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Text(
                    text = "${f.label} (${counts[f] ?: 0})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

// ── Item card ─────────────────────────────────────────────────────────────────

@Composable
fun ExpiryItemCard(
    item: ExpiryItem,
    now: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = DateUtils.daysUntil(item.expiryDate)
    val expired = item.expiryDate < now
    val accent = when {
        expired -> ExpiredRed
        days <= 7 -> ExpiredRed
        days <= SOON_THRESHOLD_DAYS -> WarningAmber
        else -> ActiveGreen
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Red accent stripe for expired items
            if (expired) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(ExpiredRed),
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.label.ifBlank { item.name },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.provider.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.provider,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Expiry Date: ${DateUtils.formatDate(item.expiryDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                DaysLeftPill(days = days, expired = expired, accent = accent)

                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DaysLeftPill(days: Long, expired: Boolean, accent: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = accent.copy(alpha = 0.14f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    expired -> "Expired"
                    days == 0L -> "Today"
                    days == 1L -> "1 Day Left"
                    else -> "$days Days Left"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

// ── Swipe-to-trash (expired items) ────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToTrash(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                },
                label = "trash_bg",
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(end = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Move to trash",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trash",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
        content = { content() },
    )
}

private fun ExpiryItem.matchesQuery(q: String): Boolean {
    val s = q.lowercase()
    return name.lowercase().contains(s) ||
        provider.lowercase().contains(s) ||
        label.lowercase().contains(s) ||
        (email?.lowercase()?.contains(s) == true) ||
        (username?.lowercase()?.contains(s) == true)
}
