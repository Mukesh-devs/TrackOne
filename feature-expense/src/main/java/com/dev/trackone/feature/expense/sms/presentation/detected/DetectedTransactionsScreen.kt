package com.dev.trackone.feature.expense.sms.presentation.detected

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.feature.expense.R
import com.dev.trackone.feature.expense.sms.domain.model.DetectedTransaction
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import java.math.BigDecimal

// Same income/expense colours as the transaction list.
private val CreditColor = Color(0xFF2E7D32)
private val DebitColor = Color(0xFFC5221F)

/**
 * Transactions found in bank SMS, waiting for the user to accept (upload) or
 * decline (delete) each one.
 */
@Composable
fun DetectedTransactionsScreen(
    items: List<DetectedTransaction>,
    onAccept: (Long) -> Unit,
    onDecline: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.detected_transactions_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Text(
                text = stringResource(R.string.detected_transactions_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (items.isEmpty()) {
            TrackOneEmptyState(
                icon = Icons.Outlined.NotificationsNone,
                title = stringResource(R.string.detected_transactions_empty_title),
                subtitle = stringResource(R.string.detected_transactions_empty_subtitle),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    DetectedTransactionRow(
                        item = item,
                        onAccept = { onAccept(item.id) },
                        onDecline = { onDecline(item.id) },
                        modifier = Modifier
                            .animateItem()
                            .padding(bottom = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetectedTransactionRow(
    item: DetectedTransaction,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isCredit = item.type == TransactionType.CREDIT
    val color = if (isCredit) CreditColor else DebitColor
    val formattedAmount = CurrencyUtils.formatAmount(BigDecimal.valueOf(item.amountPaise, 2).toDouble())
    // Sign, arrow and word all say the direction, so it never rests on colour alone.
    val signedAmount = stringResource(
        if (isCredit) R.string.detected_transaction_credit_amount else R.string.detected_transaction_debit_amount,
        formattedAmount,
    )
    val typeLabel = stringResource(if (isCredit) R.string.detected_transaction_credit else R.string.detected_transaction_debit)
    // Device locale and time zone.
    val dateTime = DateUtils.formatDateTime(
        context,
        item.occurredAt,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) { },
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$signedAmount  $typeLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    maxLines = 1,
                )
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            IconButton(onClick = onDecline) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.detected_transaction_decline, item.name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(onClick = onAccept) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.detected_transaction_accept, item.name),
                )
            }
        }
    }
}
