package com.dev.trackone.feature.credential.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.credential.domain.model.CredentialItem
import com.dev.trackone.feature.credential.presentation.CredentialUiState

@Composable
fun CredentialDetailsScreen(
    groupTitle: String,
    uiState: CredentialUiState,
    onEditItem: (CredentialItem) -> Unit,
    onDeleteItem: (CredentialItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<CredentialItem?>(null) }
    
    // Filter the items that match the exact title
    val items = uiState.items.filter { it.title == groupTitle }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpacingLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg)
    ) {
        items.forEachIndexed { index, item ->
            AccountDetailBlock(
                item = item,
                onEdit = { onEditItem(item) },
                onDelete = { itemToDelete = item },
                onCopy = { text, label -> copyToClipboard(context, text, label) }
            )
            
            if (index < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Dimens.SpacingMd),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(90.dp)) // Bottom Nav Bar clearance
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
private fun AccountDetailBlock(
    item: CredentialItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (String, String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (item.category.isNotBlank()) {
            Text(
                text = item.category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = Dimens.SpacingXs)
            )
        }

        if (item.username.isNotBlank()) {
            InputBarField(
                value = item.username,
                onCopy = { onCopy(item.username, "Username") }
            )
        }
        
        if (item.email.isNotBlank()) {
            InputBarField(
                value = item.email,
                onCopy = { onCopy(item.email, "Email") }
            )
        }
        
        if (item.password.isNotBlank()) {
            PasswordInputBarField(
                value = item.password,
                onCopy = { onCopy(item.password, "Password") }
            )
        }
        
        if (item.url.isNotBlank()) {
            InputBarField(
                value = item.url,
                onCopy = { onCopy(item.url, "URL") }
            )
        }
        
        if (item.notes?.isNotBlank() == true) {
            InputBarField(
                value = item.notes ?: "",
                onCopy = { onCopy(item.notes ?: "", "Notes") }
            )
        }

        // Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.SpacingSm),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun InputBarField(
    value: String,
    onCopy: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        trailingIcon = {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PasswordInputBarField(
    value: String,
    onCopy: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = if (passwordVisible) value else "•".repeat(value.length.coerceAtMost(20).coerceAtLeast(8)),
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        trailingIcon = {
            Row {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
