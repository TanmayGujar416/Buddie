package com.tanmay.buddie.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tanmay.buddie.presentation.theme.*

/**
 * Lightweight shimmer pulse value for skeleton placeholders.
 */
@Composable
fun rememberSkeletonAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    return alpha
}

/**
 * Skeleton loading placeholder for the Home Screen submissions list.
 * Displays during the brief moment before Room's first data emission.
 */
@Composable
fun SubmissionListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    val alpha = rememberSkeletonAlpha()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SubmissionCardSkeleton(alpha = alpha)
        }
    }
}

@Composable
fun SubmissionCardSkeleton(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline.copy(alpha = alpha))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject color indicator placeholder
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(CharcoalSurfaceHighlight.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle lines
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalSurfaceHighlight.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.40f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                )
            }

            // Action button placeholder icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                )
            }
        }
    }
}

/**
 * Skeleton loading placeholder for the Exams Screen list.
 */
@Composable
fun ExamListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 2
) {
    val alpha = rememberSkeletonAlpha()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            ExamCardSkeleton(alpha = alpha)
        }
    }
}

@Composable
fun ExamCardSkeleton(alpha: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, BorderOutline.copy(alpha = alpha))
    ) {
        Column {
            // Header banner placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalSurfaceHighlight.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalSurfaceHighlight.copy(alpha = alpha))
                    )
                }
            }

            // Body placeholders
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalSurfaceHighlight.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                    )
                    Box(
                        modifier = Modifier
                            .width(45.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalSurfaceElevated.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}
