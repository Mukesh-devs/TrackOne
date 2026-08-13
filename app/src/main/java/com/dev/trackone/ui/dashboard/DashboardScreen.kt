package com.dev.trackone.ui.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.ui.theme.ActiveGreen
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.core.ui.theme.WarningAmber
import java.util.Calendar

/**
 * Dashboard — the app's home hub.
 *
 * Shows today's snapshot across all three modules and acts as the single entry
 * point into each of them. Fully theme-token driven, so it follows the app
 * palette (and renders correctly in dark mode) instead of baking in colours.
 */
@Composable
fun DashboardScreen(
    expiringTodayCount: Int,
    todayExpenseTotal: Double,
    credentialCount: Int,
    onMenuClick: () -> Unit,
    onExpiryClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onCredentialClick: () -> Unit,
    // Preserves the previous hardcoded greeting; hoisted to a parameter so it
    // can be driven from preferences later without touching this screen.
    userName: String = "Mukesh",
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.SpacingXxl),
        ) {
            item { TopBar(onMenuClick = onMenuClick) }

            item { GreetingSection(userName = userName) }

            item {
                SectionTitle(text = "Today's Overview", topPadding = Dimens.SpacingXl)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Equal-height cards regardless of label wrapping.
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                ) {
                    OverviewStatCard(
                        icon = Icons.Default.CalendarToday,
                        accent = WarningAmber,
                        label = "Expiring",
                        value = "$expiringTodayCount",
                        subLabel = "today",
                        onClick = onExpiryClick,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    OverviewStatCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        accent = MaterialTheme.colorScheme.primary,
                        label = "Spent",
                        value = CurrencyUtils.formatAmount(todayExpenseTotal),
                        subLabel = "today",
                        onClick = onExpenseClick,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    OverviewStatCard(
                        icon = Icons.Default.VpnKey,
                        accent = ActiveGreen,
                        label = "Saved",
                        value = "$credentialCount",
                        subLabel = "accounts",
                        onClick = onCredentialClick,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }

            item { SectionTitle(text = "Modules", topPadding = Dimens.SpacingXl) }

            item {
                ModuleCard(
                    icon = Icons.Default.Timer,
                    title = "Expiry Tracker",
                    subtitle = "Track items before they expire",
                    accent = WarningAmber,
                    onClick = onExpiryClick,
                )
            }
            item { Spacer(modifier = Modifier.height(Dimens.SpacingMd)) }
            item {
                ModuleCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Expense Tracker",
                    subtitle = "Manage spending and stay on budget",
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = onExpenseClick,
                )
            }
            item { Spacer(modifier = Modifier.height(Dimens.SpacingMd)) }
            item {
                ModuleCard(
                    icon = Icons.Default.VpnKey,
                    title = "Credential Manager",
                    subtitle = "Securely store and manage accounts",
                    accent = ActiveGreen,
                    onClick = onCredentialClick,
                )
            }
        }
    }
}

// ─── Top bar ────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpacingSm, vertical = Dimens.SpacingSm),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open menu",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

// ─── Greeting ───────────────────────────────────────────────────────────────────

@Composable
private fun GreetingSection(userName: String) {
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpacingXs)) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = topPadding,
            bottom = Dimens.SpacingMd,
        ),
    )
}

// ─── Overview stat card ─────────────────────────────────────────────────────────

@Composable
private fun OverviewStatCard(
    icon: ImageVector,
    accent: Color,
    label: String,
    value: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "stat_scale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.ElevationTonal,
        shadowElevation = Dimens.ElevationListItem,
    ) {
        Column(modifier = Modifier.padding(Dimens.SpacingMd)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingMd))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingXs))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                // Long currency values shrink rather than truncate on small screens.
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

// ─── Module card ────────────────────────────────────────────────────────────────

@Composable
private fun ModuleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "module_scale",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding)
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.ElevationTonal,
        shadowElevation = Dimens.ElevationListItem,
    ) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(Dimens.IconSizeMd),
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.IconSizeMd),
            )
        }
    }
}
