package com.dev.trackone.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dev.trackone.core.ui.theme.Dimens

/**
 * Styled card with consistent elevation, shape, and padding.
 * Supports click and animated content size changes.
 */
@Composable
fun TrackOneCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: androidx.compose.ui.graphics.Color? = null,
    elevation: androidx.compose.ui.unit.Dp = Dimens.CardElevation,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
            ),
        ) {
            Column(
                modifier = Modifier.padding(Dimens.CardPadding),
                content = content,
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor ?: MaterialTheme.colorScheme.surfaceVariant,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
            ),
        ) {
            Column(
                modifier = Modifier.padding(Dimens.CardPadding),
                content = content,
            )
        }
    }
}
