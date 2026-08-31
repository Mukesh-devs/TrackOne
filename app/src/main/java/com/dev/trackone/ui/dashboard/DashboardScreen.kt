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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GridView
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
 * One entry in the Apps section: the module plus the one number worth knowing
 * about it today. Built once and rendered by whichever layout is selected, so
 * list and grid can never drift apart.
 */
private data class AppEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val metricValue: String,
    val metricLabel: String,
    val accent: Color,
    val onClick: () -> Unit,
)

/**
 * Dashboard — the app's home hub.
 *
 * Every module is one row (or tile) carrying its own today's-metric, rather
 * than a separate overview strip repeating the same three destinations. Fully
 * theme-token driven, so it follows the app palette (and renders correctly in
 * dark mode) instead of baking in colours.
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
    isGridView: Boolean,
    onToggleViewMode: (Boolean) -> Unit,
    // Preserves the previous hardcoded greeting; hoisted to a parameter so it
    // can be driven from preferences later without touching this screen.
    userName: String = "Mukesh",
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val apps = remember(expiringTodayCount, todayExpenseTotal, credentialCount, primary) {
        listOf(
            AppEntry(
                icon = Icons.Default.Timer,
                title = "Expiry Tracker",
                subtitle = "Track items before they expire",
                metricValue = "$expiringTodayCount",
                metricLabel = "expiring today",
                accent = WarningAmber,
                onClick = onExpiryClick,
            ),
            AppEntry(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Expense Tracker",
                subtitle = "Manage spending and stay on budget",
                metricValue = CurrencyUtils.formatAmount(todayExpenseTotal),
                metricLabel = "spent today",
                accent = primary,
                onClick = onExpenseClick,
            ),
            AppEntry(
                icon = Icons.Default.VpnKey,
                title = "Credential Manager",
                subtitle = "Securely store and manage accounts",
                metricValue = "$credentialCount",
                metricLabel = "saved accounts",
                accent = ActiveGreen,
                onClick = onCredentialClick,
            ),
        )
    }

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
                AppsSectionHeader(
                    isGridView = isGridView,
                    onToggleViewMode = onToggleViewMode,
                )
            }

            if (isGridView) {
                // Only three modules, so the grid is laid out by hand: a
                // LazyVerticalGrid nested in a LazyColumn has no bounded height
                // to measure against.
                items(apps.chunked(GRID_COLUMNS)) { rowApps ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = Dimens.ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                    ) {
                        rowApps.forEach { app ->
                            AppTile(
                                app = app,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                        // Keeps a short final row's tiles the same width as a full one.
                        repeat(GRID_COLUMNS - rowApps.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))
                }
            } else {
                items(apps) { app ->
                    AppRow(app = app)
                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))
                }
            }
        }
    }
}

private const val GRID_COLUMNS = 2

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
private fun AppsSectionHeader(isGridView: Boolean, onToggleViewMode: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.ScreenPadding,
                end = Dimens.SpacingSm,
                top = Dimens.SpacingXl,
                bottom = Dimens.SpacingMd,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Apps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onToggleViewMode(!isGridView) }) {
            Icon(
                // Shows the layout you'd switch *to*, which is how every
                // gallery app's toggle reads.
                imageVector = if (isGridView) {
                    Icons.AutoMirrored.Filled.ViewList
                } else {
                    Icons.Default.GridView
                },
                contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ─── App card (shared chrome) ───────────────────────────────────────────────────

/**
 * The press-to-shrink surface both layouts sit in, so a tile and a row feel
 * identical under the finger.
 */
@Composable
private fun AppSurface(
    onClick: () -> Unit,
    pressedScale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "app_scale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.ElevationTonal,
        shadowElevation = Dimens.ElevationListItem,
        content = content,
    )
}

@Composable
private fun AppIconBadge(icon: ImageVector, accent: Color, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
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
}

// ─── List layout ────────────────────────────────────────────────────────────────

/**
 * Icon and name on the left, today's number on the right. The metric takes the
 * chevron's place — with a figure sitting there the row still reads as
 * tappable, and the arrow would only crowd it.
 */
@Composable
private fun AppRow(app: AppEntry) {
    AppSurface(
        onClick = app.onClick,
        pressedScale = 0.98f,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconBadge(icon = app.icon, accent = app.accent, size = 48.dp)

            Spacer(modifier = Modifier.width(Dimens.SpacingLg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(Dimens.SpacingMd))

            // Capped so a large total ellipsizes instead of squeezing the title
            // down to nothing — without a bound, Ellipsis never gets to apply.
            Column(
                modifier = Modifier.widthIn(max = 132.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = app.metricValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = app.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.metricLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─── Grid layout ────────────────────────────────────────────────────────────────

/**
 * The same entry as a tile. No subtitle — at half the width it would wrap to
 * three lines and bury the number, which is the point of the tile.
 */
@Composable
private fun AppTile(app: AppEntry, modifier: Modifier = Modifier) {
    AppSurface(onClick = app.onClick, pressedScale = 0.96f, modifier = modifier) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            AppIconBadge(icon = app.icon, accent = app.accent, size = 44.dp)

            Spacer(modifier = Modifier.height(Dimens.SpacingMd))

            Text(
                text = app.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingMd))

            Text(
                text = app.metricValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = app.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.metricLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
