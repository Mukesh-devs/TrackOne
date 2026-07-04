package com.dev.trackone.feature.expense.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneCard
import com.dev.trackone.core.ui.component.TrackOneChip
import com.dev.trackone.core.ui.component.TrackOneDeleteDialog
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.core.ui.component.TrackOneLoadingState
import com.dev.trackone.core.ui.component.TrackOneSearchBar
import com.dev.trackone.core.ui.theme.Dimens
import com.dev.trackone.feature.expense.domain.model.ExpenseCategory
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState

@Composable
fun ExpenseHomeScreen(
    uiState: ExpenseUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (ExpenseCategory?) -> Unit,
    onEditItem: (ExpenseItem) -> Unit,
    onDeleteItem: (ExpenseItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var itemToDelete by remember { mutableStateOf<ExpenseItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.isSearchActive,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TrackOneSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                isActive = uiState.isSearchActive,
                onActiveChange = {},
                placeholder = "Search expenses...",
                modifier = Modifier.padding(bottom = Dimens.SpacingSm),
            )
        }

        when {
            uiState.isLoading -> {
                TrackOneLoadingState(message = "Loading transactions...")
            }
            uiState.items.isEmpty() && !uiState.isSearchActive && uiState.selectedCategory == null -> {
                TrackOneEmptyState(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "No transactions yet",
                    subtitle = "Tap + to add your first income or expense",
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = Dimens.SpacingLg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                ) {
                    item {
                        BalanceCard(
                            balance = uiState.balance,
                            income = uiState.totalIncome,
                            expense = uiState.totalExpense,
                            modifier = Modifier.padding(
                                horizontal = Dimens.SpacingLg,
                                vertical = Dimens.SpacingSm
                            )
                        )
                    }

                    item {
                        CategoryFilterRow(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = onCategoryFilterChange,
                        )
                    }

                    if (uiState.items.isEmpty()) {
                        item {
                            TrackOneEmptyState(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = "No results found",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Dimens.SpacingXxl),
                            )
                        }
                    } else {
                        items(uiState.items, key = { it.id }) { item ->
                            ExpenseItemCard(
                                item = item,
                                onEdit = { onEditItem(item) },
                                onDelete = { itemToDelete = item },
                                modifier = Modifier.padding(horizontal = Dimens.SpacingLg)
                            )
                        }
                    }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        TrackOneDeleteDialog(
            itemName = item.title,
            onConfirm = {
                onDeleteItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null },
        )
    }
}

@Composable
private fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingXs))
            Text(
                text = CurrencyUtils.formatAmount(balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
            
            Spacer(modifier = Modifier.height(Dimens.SpacingLg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BalanceStat(
                    label = "Income",
                    amount = income,
                    icon = Icons.Default.ArrowUpward,
                    color = MaterialTheme.colorScheme.primary,
                )
                
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                ) {}
                
                BalanceStat(
                    label = "Expense",
                    amount = expense,
                    icon = Icons.Default.ArrowDownward,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun BalanceStat(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimens.IconSizeSm),
            )
            Spacer(modifier = Modifier.width(Dimens.SpacingXs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = CurrencyUtils.formatAmount(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: ExpenseCategory?,
    onCategorySelected: (ExpenseCategory?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.SpacingLg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
        modifier = Modifier.padding(vertical = Dimens.SpacingSm),
    ) {
        item {
            TrackOneChip(
                label = "All",
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(ExpenseCategory.entries.toTypedArray()) { category ->
            TrackOneChip(
                label = category.displayName,
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
            )
        }
    }
}

@Composable
private fun ExpenseItemCard(
    item: ExpenseItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrackOneCard(modifier = modifier) {
        Column {
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
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        tint = if (item.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
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
                            text = item.category.displayName,
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
                        color = if (item.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = DateUtils.formatDate(item.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            if (!item.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Dimens.SpacingSm))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.SpacingSm),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.IconSizeMd),
                    )
                }
            }
        }
    }
}
