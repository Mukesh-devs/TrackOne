package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.SpendingFilter
import java.util.Calendar

/**
 * The transactions behind one slice of the statistics breakdown.
 *
 * Opened by tapping a legend row, so it inherits that slice's accent colour —
 * with the chart gone from the screen, the colour is what says which slice you
 * are looking at. [items] is already narrowed to the period the chart was
 * showing, so this only has to apply [filter] on top.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseBreakdownDetailScreen(
    filter: SpendingFilter,
    items: List<ExpenseItem>,
    periodLabel: String,
    periodTotal: Double,
    onItemClick: (ExpenseItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val matching = remember(items, filter) { items.filter(filter::matches) }
    val total = remember(matching) { matching.sumOf { it.amount } }
    val grouped = remember(matching) {
        matching
            .groupBy { DateUtils.getStartOfDay(it.date) }
            .toSortedMap(compareByDescending { it })
    }
    val share = if (periodTotal > 0.0) (total / periodTotal).toFloat() else 0f
    val accent = filter.accentColor()

    val cal = Calendar.getInstance()
    val todayStart = DateUtils.getStartOfDay(cal.timeInMillis)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = DateUtils.getStartOfDay(cal.timeInMillis)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 20.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            // Same dot as the chart legend, so the row you tapped and the screen
            // you landed on read as the same thing.
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = Dimens.SpacingMd,
                bottom = Dimens.SpacingXxl +
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                SummaryCard(
                    total = total,
                    count = matching.size,
                    share = share,
                    periodLabel = periodLabel,
                    accent = accent,
                    modifier = Modifier.padding(horizontal = Dimens.SpacingLg),
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingMd))
            }

            if (grouped.isEmpty()) {
                item {
                    TrackOneEmptyState(
                        icon = Icons.Default.FilterList,
                        title = "No transactions",
                        subtitle = "Nothing spent on ${filter.label} in $periodLabel.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Dimens.SpacingXxl),
                    )
                }
            } else {
                grouped.forEach { (dayMillis, itemsForDay) ->
                    stickyHeader(key = "header_$dayMillis") {
                        val dateStr = when (dayMillis) {
                            todayStart -> "Today, ${DateUtils.formatDate(dayMillis)}"
                            yesterdayStart -> "Yesterday, ${DateUtils.formatDate(dayMillis)}"
                            else -> DateUtils.formatDate(dayMillis)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(
                                    horizontal = Dimens.SpacingLg,
                                    vertical = Dimens.SpacingSm,
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = CurrencyUtils.formatAmount(itemsForDay.sumOf { it.amount }),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    items(items = itemsForDay, key = { it.id }) { item ->
                        ExpenseListItem(
                            item = item,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.padding(
                                horizontal = Dimens.SpacingLg,
                                vertical = 4.dp,
                            ),
                        )
                    }

                    item { Spacer(modifier = Modifier.height(Dimens.SpacingMd)) }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    total: Double,
    count: Int,
    share: Float,
    periodLabel: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = accent.copy(alpha = 0.10f),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total spent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyUtils.formatAmount(total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.20f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(share.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(accent),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (count == 1) "1 transaction" else "$count transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${(share * 100).toInt()}% of $periodLabel",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
        }
    }
}
