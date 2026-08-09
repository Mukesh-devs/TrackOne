package com.dev.trackone.feature.expiry.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.theme.ExpiredRed
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

/**
 * Expired / Trash tab — lists the items the user swiped to trash on Home.
 * Each item can be restored back to Home or permanently deleted.
 */
@Composable
fun ExpiryExpiredScreen(
    items: List<ExpiryItem>,
    onRestore: (ExpiryItem) -> Unit,
    onDeleteForever: (ExpiryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var toDelete by remember { mutableStateOf<ExpiryItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        ) {
            Text(
                text = "Expired",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Items you moved to trash",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (items.isEmpty()) {
            TrackOneEmptyState(
                icon = Icons.Default.EventBusy,
                title = "Trash is empty",
                subtitle = "Swipe an expired item left on Home to move it here",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(items = items, key = { it.id }) { item ->
                    TrashRow(
                        item = item,
                        onRestore = { onRestore(item) },
                        onDelete = { toDelete = item },
                    )
                }
            }
        }
    }

    toDelete?.let { item ->
        TrackOneDeleteDialog(
            itemName = item.label.ifBlank { item.name },
            onConfirm = {
                onDeleteForever(item)
                toDelete = null
            },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun TrashRow(
    item: ExpiryItem,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ExpiredRed.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.provider.ifBlank { item.name }.trim().take(1).uppercase().ifBlank { "•" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpiredRed,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label.ifBlank { item.name },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Expired: ${DateUtils.formatDate(item.expiryDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Default.RestoreFromTrash,
                    contentDescription = "Restore",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete forever",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
