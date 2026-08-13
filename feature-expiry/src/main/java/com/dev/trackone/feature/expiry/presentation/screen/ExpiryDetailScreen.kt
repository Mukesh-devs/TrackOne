package com.dev.trackone.feature.expiry.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.theme.ActiveGreen
import com.dev.trackone.core.ui.theme.ExpiredRed
import com.dev.trackone.core.ui.theme.WarningAmber
import com.dev.trackone.feature.expiry.domain.model.ExpiryItem

private const val SOON_DAYS = 30L

/**
 * Detail screen for a single expiry item (self-contained: own back arrow).
 */
@Composable
fun ExpiryDetailScreen(
    item: ExpiryItem,
    onBack: () -> Unit,
    onEdit: (ExpiryItem) -> Unit,
    onDelete: (ExpiryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val now = System.currentTimeMillis()
    val days = DateUtils.daysUntil(item.expiryDate)
    val expired = item.expiryDate < now
    val accent = when {
        expired || days <= 7 -> ExpiredRed
        days <= SOON_DAYS -> WarningAmber
        else -> ActiveGreen
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Back on the left, the item's actions on the right. They sit up here
        // rather than in a bottom bar so the system navigation bar can never
        // cover them, whichever gesture/button mode the device is in.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HeaderAction(
                icon = Icons.Default.Edit,
                description = "Edit",
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onEdit(item) },
            )
            HeaderAction(
                icon = Icons.Default.Delete,
                description = "Delete",
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.error,
                onClick = { showDeleteDialog = true },
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                // The list now runs to the bottom of the window, so it has to
                // clear the navigation bar itself.
                bottom = 24.dp + navigationBarInset,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeroCard(item = item, days = days, expired = expired, accent = accent) }

            item {
                SectionCard(icon = Icons.Default.Description, title = "Overview") {
                    if (item.provider.isNotBlank()) {
                        DetailRow("Provider", item.provider)
                    }
                    item.amount?.let { DetailRow("Amount", CurrencyUtils.formatAmount(it)) }
                    DetailRow("Purchased On", DateUtils.formatDate(item.purchasedDate))
                    DetailRow("Expiry Date", DateUtils.formatDate(item.expiryDate), valueColor = accent)
                }
            }

            if (!item.notes.isNullOrBlank()) {
                item {
                    SectionCard(icon = Icons.Default.Notes, title = "Notes") {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            // Credentials, only if present
            if (!item.username.isNullOrBlank() || !item.email.isNullOrBlank() || !item.password.isNullOrBlank()) {
                item {
                    SectionCard(icon = Icons.Default.Lock, title = "Credentials") {
                        item.username?.takeIf { it.isNotBlank() }?.let {
                            CopyRow("Username", it) { copyToClipboard(context, it, "Username") }
                        }
                        item.email?.takeIf { it.isNotBlank() }?.let {
                            CopyRow("Email", it) { copyToClipboard(context, it, "Email") }
                        }
                        item.password?.takeIf { it.isNotBlank() }?.let { pwd ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Password",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = if (passwordVisible) pwd else "••••••••",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide" else "Show",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                IconButton(onClick = { copyToClipboard(context, pwd, "Password") }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy password",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(icon = Icons.Default.History, title = "History") {
                    DetailRow("Added", DateUtils.formatDate(item.createdAt))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    DetailRow("Last Updated", DateUtils.formatDate(item.updatedAt))
                }
            }
        }
    }

    if (showDeleteDialog) {
        TrackOneDeleteDialog(
            itemName = item.label.ifBlank { item.name },
            onConfirm = { onDelete(item); showDeleteDialog = false },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(item: ExpiryItem, days: Long, expired: Boolean, accent: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = item.label.ifBlank { item.name },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val chip = item.label.ifBlank { item.provider }
            if (chip.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Left: expiry date block
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Expiry Date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = DateUtils.formatDate(item.expiryDate),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = DateUtils.formatDate(item.expiryDate, "EEEE"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(64.dp).padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                // Right: status box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    expired -> "Expired"
                                    days == 0L -> "Today"
                                    days == 1L -> "1 Day Left"
                                    else -> "$days Days Left"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (expired) {
                                "Expired on ${DateUtils.formatDate(item.expiryDate, "EEEE")}"
                            } else {
                                "Will expire on ${DateUtils.formatDate(item.expiryDate, "EEEE")}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ── Section card + rows ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
        )
    }
}

@Composable
private fun CopyRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** A tinted circular icon button for the header's Edit / Delete actions. */
@Composable
private fun HeaderAction(
    icon: ImageVector,
    description: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .background(container, CircleShape),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = content,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
