package com.dev.trackone.feature.credential.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.component.TrackOneScreenHeader
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.core.ui.theme.WarningAmber
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.presentation.CredentialUiState

/**
 * Credential home — self-contained (own header), matching the Expiry and
 * Expense modules: header, persistent search, category pills, and a list of
 * services grouped by title.
 */
@Composable
fun CredentialHomeScreen(
    uiState: CredentialUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Group accounts by service name; a service may hold several logins.
    val groups = remember(uiState.items) {
        uiState.items
            .groupBy { it.title }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .map { (title, items) -> title to items }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        TrackOneScreenHeader(
            title = "Credential Manager",
            subtitle = "${uiState.items.size} saved ${if (uiState.items.size == 1) "account" else "accounts"}",
            icon = Icons.Default.VpnKey,
        )

        SearchField(query = uiState.searchQuery, onQueryChange = onSearchQueryChange)

        if (uiState.categories.isNotEmpty()) {
            CategoryPills(
                categories = uiState.categories,
                selected = uiState.selectedCategory,
                onSelect = onCategoryFilterChange,
            )
        }

        when {
            uiState.isLoading -> TrackOneLoadingState(message = "Loading credentials…")

            groups.isEmpty() && uiState.searchQuery.isBlank() && uiState.selectedCategory == null ->
                TrackOneEmptyState(
                    icon = Icons.Default.VpnKey,
                    title = "No credentials saved",
                    subtitle = "Tap + to securely store your first account",
                )

            groups.isEmpty() -> TrackOneEmptyState(
                icon = Icons.Default.SearchOff,
                title = "No matches",
                subtitle = "Nothing matches the current search or filter",
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = Dimens.SpacingSm,
                    bottom = 140.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            ) {
                items(items = groups, key = { it.first }) { (title, items) ->
                    CredentialGroupCard(
                        title = title,
                        items = items,
                        onClick = { onGroupClick(title) },
                    )
                }
            }
        }
    }
}

// ── Search ────────────────────────────────────────────────────────────────────

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingSm),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.CardPadding, vertical = Dimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.IconSizeSm),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingMd))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search accounts…",
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
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// ── Category pills ────────────────────────────────────────────────────────────

@Composable
private fun CategoryPills(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingSm),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        Pill(label = "All", isSelected = selected == null, onClick = { onSelect(null) })
        categories.forEach { category ->
            Pill(
                label = category,
                isSelected = selected == category,
                onClick = { onSelect(if (selected == category) null else category) },
            )
        }
    }
}

@Composable
private fun Pill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (isSelected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.CardPadding, vertical = Dimens.SpacingSm),
        )
    }
}

// ── Group card ────────────────────────────────────────────────────────────────

@Composable
private fun CredentialGroupCard(
    title: String,
    items: List<CredentialItem>,
    onClick: () -> Unit,
) {
    val first = items.first()
    val hasFavorite = items.any { it.isFavorite }
    val subtitle = when {
        items.size > 1 -> "${items.size} accounts"
        first.email.isNotBlank() -> first.email
        first.username.isNotBlank() -> first.username
        first.category.isNotBlank() -> first.category
        else -> "1 account"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.ElevationTonal,
        shadowElevation = Dimens.ElevationListItem,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.CardPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.AvatarSizeMd + 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.trim().take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (hasFavorite) {
                        Spacer(modifier = Modifier.width(Dimens.SpacingXs))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favourite",
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
