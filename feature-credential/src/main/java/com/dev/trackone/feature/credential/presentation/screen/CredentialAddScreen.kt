package com.dev.trackone.feature.credential.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.ui.component.TrackOneTextField
import com.dev.trackone.core.ui.theme.ActiveGreen
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.core.ui.theme.ExpiredRed
import com.dev.trackone.core.ui.theme.WarningAmber
import com.dev.trackone.feature.credential.presentation.CredentialFormState

/**
 * Add / edit a credential.
 *
 * Mirrors the Expiry form: required fields up front in a "Sign-in details"
 * card, everything else tucked into collapsible sections so the screen opens
 * short and uncluttered.
 */
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
    var showOptionalFields by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }

    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) onSaveSuccess()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpacingLg, vertical = Dimens.SpacingSm)
            .imePadding(),
    ) {
        // =====================================================================
        // Section 1: Sign-in details (required)
        // =====================================================================
        SectionCard {
            SectionLabel("Sign-in Details")

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.title,
                onValueChange = onTitleChange,
                label = "Service Name *",
                placeholder = "e.g., Google, Netflix",
                leadingIcon = Icons.Default.Title,
                isError = formState.titleError != null,
                errorMessage = formState.titleError,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.username,
                onValueChange = onUsernameChange,
                label = "Username",
                placeholder = "Your login name",
                leadingIcon = Icons.Default.Person,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "you@example.com",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSm))

            TrackOneTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = "Password *",
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                isError = formState.passwordError != null,
                errorMessage = formState.passwordError,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                        )
                    }
                },
            )

            // Live feedback while typing — no extra tap required.
            if (formState.password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                PasswordStrengthBar(password = formState.password)
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 2: Additional details (collapsible)
        // =====================================================================
        SectionCard {
            ExpandableHeader(
                title = "Additional Details",
                expanded = showOptionalFields,
                accent = MaterialTheme.colorScheme.primary,
                onToggle = { showOptionalFields = !showOptionalFields },
            )

            AnimatedVisibility(
                visible = showOptionalFields,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
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
                        value = formState.url,
                        onValueChange = onUrlChange,
                        label = "Website URL",
                        placeholder = "https://…",
                        leadingIcon = Icons.Default.Link,
                        keyboardType = KeyboardType.Uri,
                    )

                    TrackOneTextField(
                        value = formState.category,
                        onValueChange = onCategoryChange,
                        label = "Category",
                        placeholder = "e.g., Social, Work, Finance",
                        leadingIcon = Icons.Default.Category,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 3: Notes (collapsible)
        // =====================================================================
        SectionCard {
            ExpandableHeader(
                title = "Notes",
                expanded = showNotes,
                accent = MaterialTheme.colorScheme.tertiary,
                onToggle = { showNotes = !showNotes },
            )

            AnimatedVisibility(
                visible = showNotes,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Spacer(modifier = Modifier.height(Dimens.SpacingXs))
                    TrackOneTextField(
                        value = formState.notes,
                        onValueChange = onNotesChange,
                        label = "Notes",
                        placeholder = "Recovery codes, security questions…",
                        leadingIcon = Icons.Default.Description,
                        singleLine = false,
                        maxLines = 4,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingMd))

        // =====================================================================
        // Section 4: Favourite toggle
        // =====================================================================
        SectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .clickable { onFavoriteChange(!formState.isFavorite) }
                    .padding(vertical = Dimens.SpacingXs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (formState.isFavorite) {
                        WarningAmber
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(Dimens.IconSizeSm),
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mark as favourite",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Pin this account to the top of your list",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = formState.isFavorite,
                    onCheckedChange = onFavoriteChange,
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacingXl))

        // =====================================================================
        // Save
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
                text = when {
                    formState.isSaving -> "Saving..."
                    formState.isEditing -> "Update"
                    else -> "Save"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// =============================================================================
// Reusable sub-components (mirrors ExpiryAddScreen)
// =============================================================================

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
        Column(modifier = Modifier.padding(Dimens.SpacingLg)) {
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ExpandableHeader(
    title: String,
    expanded: Boolean,
    accent: Color,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onToggle)
            .padding(vertical = Dimens.SpacingXs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (expanded) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingSm))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse $title" else "Expand $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A four-step strength meter. Purely advisory — it never blocks saving.
 */
@Composable
private fun PasswordStrengthBar(password: String) {
    val score = remember(password) { passwordScore(password) }
    val (label, accent) = when (score) {
        0, 1 -> "Weak" to ExpiredRed
        2 -> "Fair" to WarningAmber
        3 -> "Good" to MaterialTheme.colorScheme.primary
        else -> "Strong" to ActiveGreen
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXs),
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(
                            if (index < score) accent
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.SpacingXs))
        Text(
            text = "Password strength: $label",
            style = MaterialTheme.typography.bodySmall,
            color = accent,
        )
    }
}

private fun passwordScore(password: String): Int {
    if (password.length < 4) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isDigit() } && password.any { it.isLetter() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score.coerceIn(0, 4)
}
