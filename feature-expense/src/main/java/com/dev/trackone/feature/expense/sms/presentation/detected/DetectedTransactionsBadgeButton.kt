package com.dev.trackone.feature.expense.sms.presentation.detected

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.dev.trackone.feature.expense.R

private const val MAX_BADGE_COUNT = 99

/** Bell in the top bar; the badge shows how many detected transactions await review. */
@Composable
fun DetectedTransactionsBadgeButton(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = if (count > 0) {
        pluralStringResource(R.plurals.detected_transactions_pending_description, count, count)
    } else {
        stringResource(R.string.detected_transactions_title)
    }
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    // The button's description already carries the count.
                    Badge(modifier = Modifier.clearAndSetSemantics { }) {
                        Text(
                            text = if (count > MAX_BADGE_COUNT) {
                                stringResource(R.string.detected_transactions_badge_overflow)
                            } else {
                                count.toString()
                            },
                        )
                    }
                }
            },
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
