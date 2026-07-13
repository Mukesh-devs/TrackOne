package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHomeScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onSearchQueryChange: (String) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    onItemClick: (ExpenseItem) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onScrollingDown: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    // Track scroll direction for bottom bar hide/show
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Detect actual gesture/fling scroll direction using NestedScrollConnection
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                // Threshold prevents micro-jitters from triggering hide/show
                if (delta < -10f) { 
                    // Content moves up -> user is scrolling down -> hide bars
                    onScrollingDown?.invoke(true)
                } else if (delta > 10f) { 
                    // Content moves down -> user is scrolling up -> show bars
                    onScrollingDown?.invoke(false)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
         when {
            uiState.isLoading -> {
                TrackOneLoadingState(message = "Loading transactions...")
            }
            uiState.items.isEmpty() -> {
                // When empty, show everything fixed (no scroll needed)
                SharedTimePeriodToggle(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = onPeriodChange,
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpacingLg,
                        vertical = Dimens.SpacingSm,
                    ),
                )
                SharedDateNavigationRow(
                    dateLabel = dateLabel,
                    onPrevious = { onNavigateDate(false) },
                    onNext = { onNavigateDate(true) },
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpacingLg,
                    ),
                )
                BalanceCard(
                    balance = uiState.balance,
                    income = uiState.totalIncome,
                    expense = uiState.totalExpense,
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpacingLg,
                        vertical = Dimens.SpacingSm,
                    ),
                )
                TrackOneEmptyState(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "No transactions",
                    subtitle = "No transactions found for this period",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.SpacingXxl),
                )
            }
            else -> {
                // Collapsing header: filter scrolls away, balance card sticks
                @OptIn(ExperimentalFoundationApi::class)
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(
                            bottom = 80.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(nestedScrollConnection),
                    ) {
                    // Scrollable: Period Toggle (scrolls away)
                    item(key = "period_toggle") {
                        SharedTimePeriodToggle(
                            selectedPeriod = uiState.selectedPeriod,
                            onPeriodSelected = onPeriodChange,
                            modifier = Modifier.padding(
                                horizontal = Dimens.SpacingLg,
                                vertical = Dimens.SpacingSm,
                            ),
                        )
                    }

                    // Scrollable: Date Navigation (scrolls away)
                    item(key = "date_nav") {
                        SharedDateNavigationRow(
                            dateLabel = dateLabel,
                            onPrevious = { onNavigateDate(false) },
                            onNext = { onNavigateDate(true) },
                            modifier = Modifier.padding(
                                horizontal = Dimens.SpacingLg,
                            ),
                        )
                    }

                    // Sticky: Balance Card (pins to top when reached)
                    stickyHeader(key = "balance_card") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(
                                    horizontal = Dimens.SpacingLg,
                                    vertical = Dimens.SpacingSm,
                                )
                        ) {
                            BalanceCard(
                                balance = uiState.balance,
                                income = uiState.totalIncome,
                                expense = uiState.totalExpense,
                            )
                        }
                    }

                    // Transaction items
                    items(uiState.items, key = { it.id }) { item ->
                        ExpenseItemCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.padding(horizontal = Dimens.SpacingLg),
                        )
                    }
                }
            }
        }
    }
    }
}



@Composable
private fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(24.dp)
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface

    // Glossy gradient: stronger primaryContainer for a distinct look
    val gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            primaryContainer,
            primaryContainer,
            primaryContainer.copy(alpha = 0.85f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape),
    ) {
        // Card background with gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(brush = gradientBrush),
        )

        // Glossy shine overlay — subtle top highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // Subtle border for glass edge
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.25f),
                            primary.copy(alpha = 0.08f),
                        ),
                    ),
                    shape = cardShape,
                ),
        )

        // Card content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = onPrimaryContainer.copy(alpha = 0.6f),
                letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyUtils.formatAmount(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = onPrimaryContainer,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted-glass stat chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlossyBalanceStat(
                    label = "Income",
                    amount = income,
                    icon = Icons.Default.ArrowUpward,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    chipBackground = primary.copy(alpha = 0.08f),
                    chipBorder = primary.copy(alpha = 0.15f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                GlossyBalanceStat(
                    label = "Expense",
                    amount = expense,
                    icon = Icons.Default.ArrowDownward,
                    accentColor = MaterialTheme.colorScheme.error,
                    chipBackground = primary.copy(alpha = 0.08f),
                    chipBorder = primary.copy(alpha = 0.15f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GlossyBalanceStat(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    chipBackground: Color,
    chipBorder: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(chipBackground)
            .border(
                width = 1.dp,
                color = chipBorder,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = labelColor,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = CurrencyUtils.formatAmount(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpenseItemCard(
    item: ExpenseItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = if (item.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (item.isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens.IconSizeSm),
                )
                Spacer(modifier = Modifier.width(Dimens.SpacingSm))
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = item.paymentMode.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (item.isIncome) "+" else "-"}${CurrencyUtils.formatAmount(item.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                )
                Text(
                    text = DateUtils.formatDate(item.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
