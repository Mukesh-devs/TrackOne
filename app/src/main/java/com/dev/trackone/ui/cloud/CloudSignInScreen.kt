package com.dev.trackone.ui.cloud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.trackone.core.ui.theme.Dimens

/**
 * Gate in front of the app: TrackOne now keeps its data in the cloud, so there
 * is an account to sign into before there is anything to show.
 *
 * A stored token lasts 30 days, so this appears on first launch and then only
 * rarely — the [CloudAuthState.Checking] branch keeps the UI still while a
 * restored token is validated rather than flashing the form at a signed-in user.
 */
@Composable
fun CloudAccountGate(
    modifier: Modifier = Modifier,
    viewModel: CloudAuthViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        CloudAuthState.Checking -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        CloudAuthState.SignedOut -> CloudSignInScreen(viewModel = viewModel, modifier = modifier)

        is CloudAuthState.SignedIn -> content()
    }
}

@Composable
private fun CloudSignInScreen(
    viewModel: CloudAuthViewModel,
    modifier: Modifier = Modifier,
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Dimens.SpacingXxxl))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Dimens.IconSizeLg),
            )
        }

        Spacer(Modifier.height(Dimens.SpacingXl))

        Text(
            text = if (form.isRegistering) "Create your account" else "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Dimens.SpacingSm))
        Text(
            text = if (form.isRegistering) {
                "Your expiries, expenses and credentials sync to your own cloud account."
            } else {
                "Sign in to sync your TrackOne data across devices."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(Dimens.SpacingXl))

        OutlinedTextField(
            value = form.email,
            onValueChange = viewModel::setEmail,
            label = { Text("Email") },
            singleLine = true,
            enabled = !form.isSubmitting,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            shape = RoundedCornerShape(Dimens.CardCornerRadius),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Dimens.SpacingMd))

        OutlinedTextField(
            value = form.password,
            onValueChange = viewModel::setPassword,
            label = { Text("Password") },
            singleLine = true,
            enabled = !form.isSubmitting,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Hide password"
                        } else {
                            "Show password"
                        },
                    )
                }
            },
            supportingText = if (form.isRegistering) {
                { Text("At least 8 characters") }
            } else {
                null
            },
            shape = RoundedCornerShape(Dimens.CardCornerRadius),
            modifier = Modifier.fillMaxWidth(),
        )

        AnimatedVisibility(
            visible = form.error != null,
            enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(Dimens.CardCornerRadius),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.SpacingMd),
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(Dimens.IconSizeSm),
                    )
                    Spacer(Modifier.size(Dimens.SpacingSm))
                    Text(
                        text = form.error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.SpacingXl))

        Button(
            onClick = viewModel::submit,
            enabled = !form.isSubmitting,
            shape = RoundedCornerShape(Dimens.CardCornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            if (form.isSubmitting) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(Dimens.IconSizeSm),
                )
            } else {
                Text(
                    text = if (form.isRegistering) "Create account" else "Sign in",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        TextButton(onClick = viewModel::toggleMode, enabled = !form.isSubmitting) {
            Text(
                text = if (form.isRegistering) {
                    "Already have an account? Sign in"
                } else {
                    "New here? Create an account"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(Dimens.SpacingLg))

        Spacer(Modifier.height(Dimens.SpacingXxl))
    }
}

