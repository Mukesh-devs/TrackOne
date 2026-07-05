package com.dev.trackone.feature.credential.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneChip
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.component.TrackOneSearchBar
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.presentation.CredentialUiState

@Composable
fun CredentialHomeScreen(
    uiState: CredentialUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onEditItem: (CredentialItem) -> Unit,
    onDeleteItem: (CredentialItem) -> Unit,
    onToggleFavorite: (CredentialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<CredentialItem?>(null) }

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

                    if (uiState.items.isEmpty()) {
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
                        items(uiState.items, key = { it.id }) { item ->
                            CredentialItemCard(
                                item = item,
                                onEdit = { onEditItem(item) },
                                onDelete = { itemToDelete = item },
                                onToggleFavorite = { onToggleFavorite(item) },
                                onCopy = { text, label -> copyToClipboard(context, text, label) },
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

    itemToDelete?.let { item ->
        TrackOneDeleteDialog(
            itemName = item.title,
            onConfirm = {
                onDeleteItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null },
        )
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
private fun CredentialItemCard(
    item: CredentialItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopy: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(modifier = modifier) {
        Column {
            var passwordVisible by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (item.category.isNotBlank()) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Toggle favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.SpacingSm),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
            
            if (item.username.isNotBlank()) {
                CopyableRow("Username", item.username) { onCopy(item.username, "Username") }
            }
            if (item.email.isNotBlank()) {
                CopyableRow("Email", item.email) { onCopy(item.email, "Email") }
            }
            
            CredentialPasswordRow(
                password = item.password,
                passwordVisible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                onCopy = { onCopy(item.password, "Password") },
            )
            
            if (item.url.isNotBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialPasswordRow(
    password: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Password",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (passwordVisible) password else "••••••••",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Password",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CopyableRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
