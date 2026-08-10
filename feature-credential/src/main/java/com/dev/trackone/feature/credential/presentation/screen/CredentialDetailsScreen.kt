package com.dev.trackone.feature.credential.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.component.TrackOneSectionCard
import com.dev.trackone.core.ui.component.TrackOneStatusPill
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.core.ui.theme.WarningAmber
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.presentation.CredentialUiState

/**
 * All accounts stored under one service name. Self-contained: owns its back
 * header, matching the Expiry and Expense detail screens.
 */
@Composable
fun CredentialDetailsScreen(
    groupTitle: String,
    uiState: CredentialUiState,
    onBack: () -> Unit,
    onEditItem: (CredentialItem) -> Unit,
    onDeleteItem: (CredentialItem) -> Unit,
    onCopied: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<CredentialItem?>(null) }

    val items = uiState.items.filter { it.title == groupTitle }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Dimens.SpacingSm, vertical = Dimens.SpacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.SpacingSm,
                bottom = 140.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
        ) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                AccountCard(
                    item = item,
                    index = index,
                    total = items.size,
                    onEdit = { onEditItem(item) },
                    onDelete = { itemToDelete = item },
                    onCopy = { text, label ->
                        copyToClipboard(context, text, label)
                        onCopied("$label copied")
                    },
                )
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
private fun AccountCard(
    item: CredentialItem,
    index: Int,
    total: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String, String) -> Unit,
) {
    TrackOneSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (total > 1) "Account ${index + 1} of $total" else "Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (item.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                    TrackOneStatusPill(
                        text = item.category,
                        accent = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (item.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favourite",
                    tint = WarningAmber,
                    modifier = Modifier.size(Dimens.IconSizeSm),
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        if (item.username.isNotBlank()) {
            CopyableField("Username", item.username) { onCopy(item.username, "Username") }
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
        }
        if (item.email.isNotBlank()) {
            CopyableField("Email", item.email) { onCopy(item.email, "Email") }
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
        }
        if (item.password.isNotBlank()) {
            PasswordField(item.password) { onCopy(item.password, "Password") }
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
        }
        if (item.url.isNotBlank()) {
            CopyableField("Website", item.url) { onCopy(item.url, "URL") }
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
        }
        if (!item.notes.isNullOrBlank()) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
            Text(
                text = item.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMd))
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingXs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            ActionButton(
                text = "Edit",
                icon = Icons.Default.Edit,
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onEdit,
            )
            ActionButton(
                text = "Delete",
                icon = Icons.Default.Delete,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun CopyableField(label: String, value: String, onCopy: () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(start = Dimens.SpacingLg, end = Dimens.SpacingXs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp),
                )
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordField(value: String, onCopy: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    Column {
        Text(
            text = "Password",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(start = Dimens.SpacingLg, end = Dimens.SpacingXs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    // Masked by default so a shoulder-surfer doesn't get it for free.
                    text = if (visible) value else "•".repeat(value.length.coerceIn(8, 16)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp),
                )
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy password",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingSm))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = content,
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
