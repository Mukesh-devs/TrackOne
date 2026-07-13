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
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
    var passwordVisible by remember { mutableStateOf(false) }
    val isExpired = DateUtils.isExpired(item.expiryDate)
    val daysLeft = DateUtils.daysUntil(item.expiryDate)
    val isUrgent = isExpired || daysLeft < 7

    val urgencyColor = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val bg = if (isExpired) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface

    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = urgencyColor.copy(alpha = 0.2f),
                ambientColor = urgencyColor.copy(alpha = 0.1f)
            )
            .border(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(urgencyColor.copy(alpha = 0.4f), urgencyColor.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = bg,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Title & Provider
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = item.label.ifBlank { item.name },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.provider.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.provider,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Right: Days Left Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isExpired) "Expired" else "${daysLeft}d",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = urgencyColor
                    )
                    Text(
                        text = if (isExpired) "ago" else "left",
                        style = MaterialTheme.typography.labelMedium,
                        color = urgencyColor.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Edit/Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            DashedDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))

            // Info Details (Receipt Style)
            ReceiptRow(label = "Purchased", value = DateUtils.formatDate(item.purchasedDate))
            Spacer(modifier = Modifier.height(16.dp))
            ReceiptRow(
                label = "Expires", 
                value = DateUtils.formatDate(item.expiryDate), 
                valueColor = urgencyColor
            )
            
            item.amount?.let { amount ->
                Spacer(modifier = Modifier.height(16.dp))
                ReceiptRow(label = "Amount", value = CurrencyUtils.formatAmount(amount))
            }
            
            if (!item.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Column {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Credentials Vault Section
            if (!item.username.isNullOrBlank() || !item.email.isNullOrBlank() || !item.password.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Secure Credentials",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        item.username?.takeIf { it.isNotBlank() }?.let { username ->
                            CopyableRow(
                                icon = Icons.Default.Person,
                                label = "Username",
                                value = username
                            ) { onCopy(username, "Username") }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        item.email?.takeIf { it.isNotBlank() }?.let { email ->
                            CopyableRow(
                                icon = Icons.Default.Email,
                                label = "Email",
                                value = email
                            ) { onCopy(email, "Email") }
                            Spacer(modifier = Modifier.height(12.dp))
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
}

@Composable
private fun ReceiptRow(
    label: String, 
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    dashLength: androidx.compose.ui.unit.Dp = 6.dp
) {
    Canvas(modifier = modifier.fillMaxWidth().height(thickness)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), dashLength.toPx()), 0f)
        )
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

