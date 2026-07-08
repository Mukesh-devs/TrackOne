package com.dev.trackone.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val SkyBlue = Color(0xFF00B4D8)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            title = "Track Expiries",
            description = "Never miss an expiration date again. Keep track of your food, documents, and subscriptions.",
            color = Color(0xFF4CAF50)
        ),
        OnboardingPage(
            title = "Manage Expenses",
            description = "Stay on top of your budget. Log your daily expenses and view intuitive analytics.",
            color = Color(0xFFFF9800)
        ),
        OnboardingPage(
            title = "Secure Credentials",
            description = "Store your passwords securely in one place and access them whenever you need.",
            color = Color(0xFF2196F3)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, end = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onComplete) {
                Text(
                    text = "Skip",
                    color = SkyBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        // Bottom Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pager Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) SkyBlue else Color.LightGray)
                            .size(if (isSelected) 10.dp else 8.dp)
                    )
                }
            }

            // Next / Get Started Button
            Button(
                onClick = {
                    if (pagerState.currentPage == pages.size - 1) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for an illustration
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(color = page.color.copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // You can replace this with an actual image/illustration later
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(color = page.color, shape = CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}

private data class OnboardingPage(
    val title: String,
    val description: String,
    val color: Color
)
