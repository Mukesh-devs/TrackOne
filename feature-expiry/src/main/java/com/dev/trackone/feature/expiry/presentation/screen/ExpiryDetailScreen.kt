package com.dev.trackone.feature.expiry.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expiry.domain.model.ExpiryGroup
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

/**
 * Detail screen showing all entries within a group.
 * Supports edit, delete, and copy actions for credentials.
 */
@Composable
fun ExpiryDetailScreen(
    group: ExpiryGroup,
    onEditItem: (ExpiryItem) -> Unit,
    onDeleteItem: (ExpiryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<ExpiryItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Group header
        Text(
            text = group.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = Dimens.SpacingLg,
                vertical = Dimens.SpacingMd,
            ),
        )

        if (group.hasMultiple) {
            Text(
                text = "${group.itemCount} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Dimens.SpacingLg),
            )
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = Dimens.SpacingLg,
                vertical = Dimens.SpacingSm,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            items(
                items = group.items,
                key = { it.id },
            ) { item ->
                ExpiryDetailCard(
                    item = item,
                    onEdit = { onEditItem(item) },
                    onDelete = { itemToDelete = item },
                    onCopy = { text, label -> copyToClipboard(context, text, label) },
                )
            }
        }
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        TrackOneDeleteDialog(
            itemName = item.label.ifBlank { item.name },
            onConfirm = {
                onDeleteItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null },
        )
    }
}

@Composable
private fun ExpiryDetailCard(
    item: ExpiryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(modifier = modifier) {
        // Title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (item.label.isNotBlank()) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = item.provider,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = Dimens.SpacingSm),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        // Dates
        DetailRow("Purchased", DateUtils.formatDate(item.purchasedDate))
        DetailRow(
            label = "Expires",
            value = DateUtils.formatDate(item.expiryDate),
            valueColor = if (DateUtils.isExpired(item.expiryDate)) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        DetailRow("Status", DateUtils.getExpiryLabel(item.expiryDate))

        // Amount
        item.amount?.let { amount ->
            DetailRow("Amount", CurrencyUtils.formatAmount(amount))
        }

        // Notes
        if (!item.notes.isNullOrBlank()) {
            DetailRow("Notes", item.notes)
        }

        // Credentials
        if (!item.username.isNullOrBlank() || !item.email.isNullOrBlank() || !item.password.isNullOrBlank()) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.SpacingSm),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            item.username?.takeIf { it.isNotBlank() }?.let { username ->
                CopyableRow("Username", username) { onCopy(username, "Username") }
            }
            item.email?.takeIf { it.isNotBlank() }?.let { email ->
                CopyableRow("Email", email) { onCopy(email, "Email") }
            }
            item.password?.takeIf { it.isNotBlank() }?.let { password ->
                CopyableRow("Password", "••••••••") { onCopy(password, "Password") }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
        )
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
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

