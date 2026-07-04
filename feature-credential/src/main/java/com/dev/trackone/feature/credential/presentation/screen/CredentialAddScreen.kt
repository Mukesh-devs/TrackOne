package com.dev.trackone.feature.credential.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.credential.presentation.CredentialFormState

@Composable
fun CredentialAddScreen(
    formState: CredentialFormState,
    onTitleChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpacingLg)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {
        Text(
            text = if (formState.isEditing) "Edit Credential" else "Add Credential",
            style = MaterialTheme.typography.headlineSmall,
        )
        
        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        TrackOneTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = "Title / Service Name *",
            placeholder = "e.g., Google, Netflix",
            leadingIcon = Icons.Default.Title,
            isError = formState.titleError != null,
            errorMessage = formState.titleError,
        )

        TrackOneTextField(
            value = formState.username,
            onValueChange = onUsernameChange,
            label = "Username (Optional)",
            leadingIcon = Icons.Default.Person,
        )

        TrackOneTextField(
            value = formState.email,
            onValueChange = onEmailChange,
            label = "Email (Optional)",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
        )

        TrackOneTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = "Password *",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            isError = formState.passwordError != null,
            errorMessage = formState.passwordError,
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )

        TrackOneTextField(
            value = formState.url,
            onValueChange = onUrlChange,
            label = "Website URL (Optional)",
            leadingIcon = Icons.Default.Link,
            keyboardType = KeyboardType.Uri,
        )

        TrackOneTextField(
            value = formState.category,
            onValueChange = onCategoryChange,
            label = "Category (Optional)",
            placeholder = "e.g., Social, Work, Finance",
            leadingIcon = Icons.Default.Category,
        )

        TrackOneTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            label = "Notes (Optional)",
            leadingIcon = Icons.Default.Description,
            singleLine = false,
            maxLines = 3,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.SpacingSm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Mark as Favorite",
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = formState.isFavorite,
                onCheckedChange = onFavoriteChange,
            )
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingLg))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !formState.isSaving,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = if (formState.isSaving) "Saving..." else if (formState.isEditing) "Update" else "Save",
                style = MaterialTheme.typography.labelLarge,
            )
        }
        
        Spacer(modifier = Modifier.height(Dimens.SpacingXxl))
    }
}
