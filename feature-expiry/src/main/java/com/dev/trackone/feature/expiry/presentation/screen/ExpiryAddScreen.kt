package com.dev.trackone.feature.expiry.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneDatePicker
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expiry.presentation.ExpiryFormState

/**
 * Modern, compact Add/Edit screen for expiry items.
 *
 * Design:
 * - Grouped into visual sections with cards
 * - Clickable date rows with calendar icons (tap to pick)
 * - Collapsible "Credentials" section with + toggle
 * - Clean, minimal vertical spacing
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
    var showCredentials by remember { mutableStateOf(
        formState.isEditing && (
            formState.username.isNotBlank() ||
            formState.email.isNotBlank() ||
            formState.password.isNotBlank()
        )
    ) }
    var showOptionalFields by remember { mutableStateOf(
        formState.isEditing && (
            formState.label.isNotBlank() ||
            formState.amount.isNotBlank() ||
            formState.notes.isNotBlank()
        )
    ) }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm)
            .imePadding(),
    ) {
        // =====================================================================
        // Section 1: Basic Info (Required)
        // =====================================================================
        SectionCard {
            SectionLabel("Basic Information")

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.name,
                onValueChange = onNameChange,
                label = "Name *",
                placeholder = "e.g., Netflix, Amazon Prime",
                leadingIcon = Icons.Default.Title,
                isError = formState.nameError != null,
                errorMessage = formState.nameError,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.provider,
                onValueChange = onProviderChange,
                label = "Provider *",
                placeholder = "e.g., Netflix, Adobe, GoDaddy",
                leadingIcon = Icons.Default.Store,
                isError = formState.providerError != null,
                errorMessage = formState.providerError,
            )
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 2: Dates (Required) — Clickable rows
        // =====================================================================
        SectionCard {
            SectionLabel("Dates")

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
            ) {
                // Purchased Date
                DatePickerRow(
                    label = "Purchased *",
                    dateText = formState.purchasedDate?.let { DateUtils.formatDate(it) },
                    placeholder = "Select",
                    isError = formState.purchasedDateError != null,
                    errorMessage = formState.purchasedDateError,
                    onClick = { showPurchasedDatePicker = true },
                    modifier = Modifier.weight(1f),
                )

                // Expiry Date
                DatePickerRow(
                    label = "Expiry *",
                    dateText = formState.expiryDate?.let { DateUtils.formatDate(it) },
                    placeholder = "Select",
                    isError = formState.expiryDateError != null,
                    errorMessage = formState.expiryDateError,
                    onClick = { showExpiryDatePicker = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 3: Optional Fields (Label, Amount, Notes) — Collapsible
        // =====================================================================
        SectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showOptionalFields = !showOptionalFields }
                    .padding(vertical = Dimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (showOptionalFields) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = "Additional Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    imageVector = if (showOptionalFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = showOptionalFields,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))

                    TrackOneTextField(
                        value = formState.label,
                        onValueChange = onLabelChange,
                        label = "Label",
                        placeholder = "e.g., Personal, Family, Office",
                        leadingIcon = Icons.AutoMirrored.Filled.Label,
                    )

                    TrackOneTextField(
                        value = formState.amount,
                        onValueChange = onAmountChange,
                        label = "Amount",
                        placeholder = "0.00",
                        leadingIcon = Icons.Default.Payment,
                        keyboardType = KeyboardType.Decimal,
                    )

                    TrackOneTextField(
                        value = formState.notes,
                        onValueChange = onNotesChange,
                        label = "Notes",
                        placeholder = "Any additional notes...",
                        leadingIcon = Icons.Default.Description,
                        singleLine = false,
                        maxLines = 3,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 4: Credentials (Optional) — Collapsible
        // =====================================================================
        SectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showCredentials = !showCredentials }
                    .padding(vertical = Dimens.SpacingXs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (showCredentials) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                    Text(
                        text = "Credentials",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Icon(
                    imageVector = if (showCredentials) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = showCredentials,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))

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
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingXl))

        // =====================================================================
        // Save Button
        // =====================================================================
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !formState.isSaving,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = if (formState.isSaving) "Saving..." else if (formState.isEditing) "Update" else "Save",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
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

// =============================================================================
// Reusable sub-components
// =============================================================================

/**
 * A card wrapper for form sections.
 */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpacingLg),
        ) {
            content()
        }
    }
}

/**
 * A small section header label.
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * A clickable row that opens a date picker.
 * Shows a calendar icon, label, and selected date (or placeholder).
 */
@Composable
private fun DatePickerRow(
    label: String,
    dateText: String?,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onClick)
                .padding(vertical = Dimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingMd))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText ?: placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (dateText != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (dateText != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                )
            }
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 40.dp),
            )
        }
    }
}
