package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dev.trackone.feature.expense.presentation.TimePeriod
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.TimeZone

/**
 * "Jump to…" for the date navigator.
 *
 * The arrows step one period at a time, which is fine for last month and
 * useless for last March. This picks a date directly, in whatever granularity
 * the current [period] actually uses — there is no point offering a day grid
 * when the screen is showing a whole year.
 */
@Composable
fun PeriodPickerDialog(
    period: TimePeriod,
    currentDate: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    when (period) {
        TimePeriod.DAY -> DayPickerDialog(currentDate, onPick, onDismiss)
        TimePeriod.MONTH -> MonthPickerDialog(currentDate, onPick, onDismiss)
        TimePeriod.YEAR -> YearPickerDialog(currentDate, onPick, onDismiss)
    }
}

// ── Day ───────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPickerDialog(currentDate: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = currentDate)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onPick(it.toLocalNoon()) }
                    onDismiss()
                },
                enabled = state.selectedDateMillis != null,
            ) { Text("Jump") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = state)
    }
}

/**
 * [DatePicker] hands back midnight **UTC**. Reading that back in a timezone
 * behind UTC lands on the previous day, so the calendar date is re-read in UTC
 * and rebuilt at local noon — far enough from either boundary that no offset or
 * DST shift can move it.
 */
private fun Long.toLocalNoon(): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = this@toLocalNoon }
    return localNoon(
        year = utc.get(Calendar.YEAR),
        month = utc.get(Calendar.MONTH),
        day = utc.get(Calendar.DAY_OF_MONTH),
    )
}

private fun localNoon(year: Int, month: Int, day: Int): Long =
    Calendar.getInstance().apply {
        clear()
        set(year, month, day, 12, 0, 0)
    }.timeInMillis

// ── Month ─────────────────────────────────────────────────────────────────────

/**
 * A year stepper over a grid of months: any month of any year is two taps away,
 * however far back it is.
 */
@Composable
private fun MonthPickerDialog(currentDate: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val selected = remember(currentDate) {
        Calendar.getInstance().apply { timeInMillis = currentDate }
    }
    val selectedYear = selected.get(Calendar.YEAR)
    val selectedMonth = selected.get(Calendar.MONTH)

    var year by remember { mutableIntStateOf(selectedYear) }
    val monthNames = remember { DateFormatSymbols.getInstance().shortMonths.take(MONTHS_IN_YEAR) }

    PickerScaffold(title = "Jump to month", onDismiss = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { year-- }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous year",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "$year",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = { year++ }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next year",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        monthNames.chunked(MONTH_GRID_COLUMNS).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEachIndexed { colIndex, name ->
                    val month = rowIndex * MONTH_GRID_COLUMNS + colIndex
                    PickerCell(
                        label = name,
                        isSelected = year == selectedYear && month == selectedMonth,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onPick(localNoon(year, month, 1))
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}

// ── Year ──────────────────────────────────────────────────────────────────────

/** A scrollable run of years, opened at the one currently in view. */
@Composable
private fun YearPickerDialog(currentDate: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val selectedYear = remember(currentDate) {
        Calendar.getInstance().apply { timeInMillis = currentDate }.get(Calendar.YEAR)
    }
    val years = remember {
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        ((thisYear + YEARS_AHEAD) downTo (thisYear - YEARS_BACK)).toList()
    }
    // Opens with the current year a couple of rows down rather than pinned to
    // the very top, so the neighbouring years are visible to scroll from.
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (years.indexOf(selectedYear) - 2).coerceAtLeast(0),
    )

    PickerScaffold(title = "Jump to year", onDismiss = onDismiss) {
        LazyColumn(
            state = listState,
            modifier = Modifier.heightIn(max = 280.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(years, key = { it }) { year ->
                PickerCell(
                    label = "$year",
                    isSelected = year == selectedYear,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onPick(localNoon(year, Calendar.JANUARY, 1))
                        onDismiss()
                    },
                )
            }
        }
    }
}

// ── Shared pieces ─────────────────────────────────────────────────────────────

@Composable
private fun PickerScaffold(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
private fun PickerCell(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

private const val MONTHS_IN_YEAR = 12
private const val MONTH_GRID_COLUMNS = 3
private const val YEARS_BACK = 20
private const val YEARS_AHEAD = 2
