package com.dev.trackone.feature.expiry.presentation.screen

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDatePicker
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expiry.presentation.ExpiryFormState

/**
 * Add/Edit screen for expiry items.
 */
@Composable
fun ExpiryAddScreen(
    formState: ExpiryFormState,
    onNameChange: (String) -> Unit,
    onProviderChange: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onPurchasedDateChange: (Long?) -> Unit,
    onExpiryDateChange: (Long?) -> Unit,
    onNotesChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPurchasedDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }

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
            text = if (formState.isEditing) "Edit Item" else "Add New Item",
            style = MaterialTheme.typography.headlineSmall,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingSm))

        // Name
        TrackOneTextField(
            value = formState.name,
            onValueChange = onNameChange,
            label = "Name *",
            placeholder = "e.g., Netflix, Amazon Prime",
            leadingIcon = Icons.Default.Title,
            isError = formState.nameError != null,
            errorMessage = formState.nameError,
        )

        // Provider
        TrackOneTextField(
            value = formState.provider,
            onValueChange = onProviderChange,
            label = "Provider *",
            placeholder = "e.g., Netflix, Adobe, GoDaddy",
            leadingIcon = Icons.Default.Store,
            isError = formState.providerError != null,
            errorMessage = formState.providerError,
        )

        // Label
        TrackOneTextField(
            value = formState.label,
            onValueChange = onLabelChange,
            label = "Label",
            placeholder = "e.g., Personal, Family, Office",
            leadingIcon = Icons.Default.Label,
        )

        // Date row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            // Purchased Date
            TrackOneTextField(
                value = formState.purchasedDate?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                label = "Purchased *",
                leadingIcon = Icons.Default.CalendarMonth,
                readOnly = true,
                isError = formState.purchasedDateError != null,
                errorMessage = formState.purchasedDateError,
                modifier = Modifier
                    .weight(1f),
            )

            // Expiry Date
            TrackOneTextField(
                value = formState.expiryDate?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = {},
                label = "Expiry *",
                leadingIcon = Icons.Default.CalendarMonth,
                readOnly = true,
                isError = formState.expiryDateError != null,
                errorMessage = formState.expiryDateError,
                modifier = Modifier
                    .weight(1f),
            )
        }

        // Tap-to-pick buttons for dates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
        ) {
            Button(
                onClick = { showPurchasedDatePicker = true },
                modifier = Modifier.weight(1f),
            ) {
                Text("Pick Purchase Date")
            }
            Button(
                onClick = { showExpiryDatePicker = true },
                modifier = Modifier.weight(1f),
            ) {
                Text("Pick Expiry Date")
            }
        }

        // Amount
        TrackOneTextField(
            value = formState.amount,
            onValueChange = onAmountChange,
            label = "Amount",
            placeholder = "0.00",
            leadingIcon = Icons.Default.Payment,
            keyboardType = KeyboardType.Decimal,
        )

        // Notes
        TrackOneTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            label = "Notes",
            placeholder = "Any additional notes...",
            leadingIcon = Icons.Default.Description,
            singleLine = false,
            maxLines = 3,
        )

        // Credentials section
        Text(
            text = "Credentials (Optional)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.SpacingSm),
        )

        TrackOneTextField(
            value = formState.username,
            onValueChange = onUsernameChange,
            label = "Username",
            leadingIcon = Icons.Default.Person,
        )

        TrackOneTextField(
            value = formState.email,
            onValueChange = onEmailChange,
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
        )

        TrackOneTextField(
            value = formState.password,
            onValueChange = onPasswordChange,
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            imeAction = ImeAction.Done,
        )

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // Save button
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

    // Date pickers
    if (showPurchasedDatePicker) {
        TrackOneDatePicker(
            onDateSelected = { onPurchasedDateChange(it) },
            onDismiss = { showPurchasedDatePicker = false },
            initialDate = formState.purchasedDate,
            title = "Select Purchase Date",
        )
    }

    if (showExpiryDatePicker) {
        TrackOneDatePicker(
            onDateSelected = { onExpiryDateChange(it) },
            onDismiss = { showExpiryDatePicker = false },
            initialDate = formState.expiryDate,
            title = "Select Expiry Date",
        )
    }
}
