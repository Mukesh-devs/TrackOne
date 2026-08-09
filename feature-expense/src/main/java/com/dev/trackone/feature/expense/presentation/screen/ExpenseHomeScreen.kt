package com.dev.trackone.feature.expense.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.trackone.core.common.util.CurrencyUtils
import com.dev.trackone.core.common.util.DateUtils
import com.dev.trackone.core.ui.component.TrackOneEmptyState
import com.dev.trackone.feature.expense.domain.model.ExpenseItem
import com.dev.trackone.feature.expense.presentation.ExpenseUiState
import com.dev.trackone.feature.expense.presentation.TimePeriod
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHomeScreen(
    uiState: ExpenseUiState,
    dateLabel: String,
    onSearchQueryChange: (String) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit,
    onNavigateDate: (Boolean) -> Unit,
    onItemClick: (ExpenseItem) -> Unit,
    onViewAllClick: () -> Unit,
    onMenuClick: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onScrollingDown: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val notImplementedToast = {
        Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Hero Card
                item {
                    ExpenseHeroCard(
                        amount = uiState.totalExpense,
                        monthLabel = DateUtils.formatMonthYear(System.currentTimeMillis())
                    )
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onViewAllClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100) // Orange color
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View All",
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Recent Transactions List (max 5)
                val recentItems = uiState.items.take(5)
                if (recentItems.isEmpty()) {
                    item {
                        TrackOneEmptyState(
                            icon = Icons.Default.Add,
                            title = "No transactions",
                            subtitle = "Add some expenses to see them here",
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                } else {
                    items(recentItems, key = { it.id }) { item ->
                        ExpenseListItem(
                            item = item,
                            onClick = { onItemClick(item) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
        }
    }
}

@Composable
fun ExpenseHeroCard(
    amount: Double,
    monthLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9800), // Vibrant orange
                            Color(0xFFE65100)  // Deep orange
                        )
                    )
                )
                .padding(24.dp)
        ) {
            // Content
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    // Month dropdown pill
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            contentColor = Color(0xFFE65100),
                            modifier = Modifier.clickable { expanded = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = monthLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            val context = LocalContext.current
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            months.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(month) },
                                    onClick = {
                                        expanded = false
                                        Toast.makeText(context, "$month selected (Coming soon)", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = CurrencyUtils.formatAmount(amount),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Decorative line chart at the bottom right
            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(60.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    quadraticBezierTo(size.width * 0.1f, size.height * 0.5f, size.width * 0.3f, size.height * 0.7f)
                    quadraticBezierTo(size.width * 0.5f, size.height * 0.9f, size.width * 0.6f, size.height * 0.4f)
                    quadraticBezierTo(size.width * 0.7f, 0f, size.width * 0.85f, size.height * 0.3f)
                    quadraticBezierTo(size.width * 0.95f, size.height * 0.5f, size.width, size.height * 0.4f)
                }
                
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.5f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                
                // Draw dots at peaks
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(size.width * 0.3f, size.height * 0.7f))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(size.width * 0.6f, size.height * 0.4f))
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(size.width * 0.85f, size.height * 0.3f))
            }
        }
    }
}
