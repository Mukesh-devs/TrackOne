package com.dev.trackone.feature.expiry.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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
        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = Dimens.SpacingLg,
                vertical = Dimens.SpacingSm,
                // Extra bottom padding for floating bottom bar
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            if (group.hasMultiple) {
                item {
                    Text(
                        text = "${group.itemCount} entries",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Dimens.SpacingSm),
                    )
                }
            }

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

            // Bottom spacer for floating bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        var passwordVisible by remember { mutableStateOf(false) }
        val isExpired = DateUtils.isExpired(item.expiryDate)
        
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    if (item.label.isNotBlank()) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (item.provider.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.provider,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Row(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                    )
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Badge
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isExpired) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = DateUtils.getExpiryLabel(item.expiryDate),
                        color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.DateRange,
                    label = "Purchased",
                    value = DateUtils.formatDate(item.purchasedDate)
                )
                
                DetailRow(
                    icon = Icons.Default.Event,
                    label = "Expires",
                    value = DateUtils.formatDate(item.expiryDate),
                    valueColor = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )

                item.amount?.let { amount ->
                    DetailRow(
                        icon = Icons.Default.AttachMoney,
                        label = "Amount",
                        value = CurrencyUtils.formatAmount(amount)
                    )
                }
                
                if (!item.notes.isNullOrBlank()) {
                    DetailRow(
                        icon = Icons.Default.Notes,
                        label = "Notes",
                        value = item.notes
                    )
                }
            }

            // Credentials Section
            if (!item.username.isNullOrBlank() || !item.email.isNullOrBlank() || !item.password.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Credentials",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item.username?.takeIf { it.isNotBlank() }?.let { username ->
                        CopyableRow(
                            icon = Icons.Default.Person,
                            label = "Username",
                            value = username
                        ) { onCopy(username, "Username") }
                    }
                    item.email?.takeIf { it.isNotBlank() }?.let { email ->
                        CopyableRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = email
                        ) { onCopy(email, "Email") }
                    }
                    item.password?.takeIf { it.isNotBlank() }?.let { password ->
                        PasswordToggleRow(
                            icon = Icons.Default.Lock,
                            password = password,
                            passwordVisible = passwordVisible,
                            onToggleVisibility = { passwordVisible = !passwordVisible },
                            onCopy = { onCopy(password, "Password") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PasswordToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    password: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (passwordVisible) password else "••••••••",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Row {
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Password",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CopyableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onCopy: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

