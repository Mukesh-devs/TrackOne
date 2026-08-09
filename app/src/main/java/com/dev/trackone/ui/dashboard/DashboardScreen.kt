package com.dev.trackone.ui.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.ui.theme.Dimens
import java.util.Calendar

/**
 * Dashboard home screen showing overview of all 3 modules.
 */
@Composable
fun DashboardScreen(
    expiringTodayCount: Int,
    todayExpenseTotal: Double,
    credentialCount: Int,
    onMenuClick: () -> Unit,
    onCloudClick: () -> Unit,
    isBackupPending: Boolean?,
    isSignedIn: Boolean,
    onExpiryClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onCredentialClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF8F9FA),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Top bar: Menu + Notification bell + Cloud icon
            item {
                TopBar(
                    onMenuClick = onMenuClick,
                    onCloudClick = onCloudClick,
                    isBackupPending = isBackupPending,
                    isSignedIn = isSignedIn,
                )
            }

            // Greeting
            item {
                GreetingSection()
            }

            // Today's Overview header
            item {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                    modifier = Modifier.padding(
                        start = 24.dp, end = 24.dp,
                        top = 24.dp, bottom = 12.dp
                    ),
                )
            }

            // Overview cards row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OverviewStatCard(
                        icon = Icons.Default.CalendarToday,
                        iconTint = Color(0xFF1B5E20),
                        iconBgColor = Color(0xFFE8F5E9),
                        label = "Expiring Today",
                        value = "$expiringTodayCount",
                        subLabel = "Items",
                        modifier = Modifier.weight(1f),
                    )
                    OverviewStatCard(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFFE65100),
                        iconBgColor = Color(0xFFFFF3E0),
                        label = "Expenses Today",
                        value = CurrencyUtils.formatAmount(todayExpenseTotal),
                        subLabel = "Spent",
                        modifier = Modifier.weight(1f),
                    )
                    OverviewStatCard(
                        icon = Icons.Default.Lock,
                        iconTint = Color(0xFF0D47A1),
                        iconBgColor = Color(0xFFE3F2FD),
                        label = "Saved Credentials",
                        value = "$credentialCount",
                        subLabel = "Accounts",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Quick Access header
            item {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                    modifier = Modifier.padding(
                        start = 24.dp, end = 24.dp,
                        top = 28.dp, bottom = 12.dp
                    ),
                )
            }

            // Quick Access module cards
            item {
                QuickAccessCard(
                    icon = Icons.Default.Timer,
                    title = "Expiry Tracker",
                    subtitle = "Track items and never miss expiry",
                    gradientColors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)),
                    iconBgColor = Color(0xFF1B5E20),
                    chevronColor = Color(0xFF388E3C),
                    onClick = onExpiryClick,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                QuickAccessCard(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Expense Tracker",
                    subtitle = "Manage expenses and stay on budget",
                    gradientColors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
                    iconBgColor = Color(0xFFE65100),
                    chevronColor = Color(0xFFEF6C00),
                    onClick = onExpenseClick,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                QuickAccessCard(
                    icon = Icons.Default.VpnKey,
                    title = "Credential Manager",
                    subtitle = "Securely store and manage accounts",
                    gradientColors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)),
                    iconBgColor = Color(0xFF0D47A1),
                    chevronColor = Color(0xFF1565C0),
                    onClick = onCredentialClick,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

// ─── Top Bar ────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    onMenuClick: () -> Unit,
    onCloudClick: () -> Unit,
    isBackupPending: Boolean?,
    isSignedIn: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF1F1F1F),
            )
        }

        Row {
            // Cloud/Backup icon
            IconButton(onClick = onCloudClick) {
                val cloudIcon = if (!isSignedIn) {
                    Icons.Outlined.CloudOff
                } else when (isBackupPending) {
                    true -> Icons.Outlined.CloudUpload
                    false -> Icons.Outlined.CloudDone
                    null -> Icons.Outlined.Cloud
                }
                Icon(
                    imageVector = cloudIcon,
                    contentDescription = "Cloud Sync",
                    tint = Color(0xFF5F6368),
                )
            }
        }
    }
}

// ─── Greeting ───────────────────────────────────────────────────────────────────

@Composable
private fun GreetingSection() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning,"
        hour < 17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF388E3C),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = "Mukesh 👋",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F1F),
        )
    }
}

// ─── Overview Stat Card ─────────────────────────────────────────────────────────

@Composable
private fun OverviewStatCard(
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    label: String,
    value: String,
    subLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = iconTint,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F),
            )

            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
            )
        }
    }
}

// ─── Quick Access Card ──────────────────────────────────────────────────────────

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    iconBgColor: Color,
    chevronColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = chevronColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
