package com.tanmay.buddie.presentation.components.mascot

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mascot mood states defined in mascot-style-guide.md
 */
enum class MascotMood {
    CALM,
    AWARE,
    URGENT,
    OVERDUE
}

// Warm dark-brown linework color matching reference image (not black, not brand blue)
private val OutlineBrown = Color(0xFF55332A)

/**
 * Buddie Mascot Composable
 *
 * Implements the character 1:1 matching the reference image:
 * - Smooth, oblate mochi silhouette (continuous sweeping curves, no flat edges or sharp corners)
 * - Bottom-heavy silhouette: plump dough ball settling slightly under its own weight
 * - Continuous, sweeping rounded bottom curve
 * - Squat proportions: width:height ratio ~1.116
 * - Perfect vertical symmetry
 * - Soft warm creamy peach inner shading & specular shine streak along bottom-left
 * - Large shiny eyes with dual top-left catchlights
 * - Soft pastel-pink cheek blush overlapping the lower-outer eye quadrant
 * - Gentle upturned "U" smile
 * - 4 distinct mood expressions (Calm, Aware, Urgent, Overdue)
 * - Idle breathing micro-animation
 */
@Composable
fun BuddieMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.CALM,
    size: Dp = 110.dp,
    animateIdle: Boolean = true
) {
    // Throttled idle breathing & floating micro-animation (~20 updates/sec)
    val elapsedMs by rememberThrottledAnimationClock(enabled = animateIdle)
    val floatOffset = if (animateIdle) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -3.5f,
            targetValue = 3.5f,
            durationMs = 1800,
            easing = FastOutSlowInEasing
        )
    } else 0f

    val breathScaleY = if (animateIdle) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = 0.985f,
            targetValue = 1.015f,
            durationMs = 1800,
            easing = FastOutSlowInEasing
        )
    } else 1f

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Buddie mascot, mood: ${mood.name.lowercase()}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            val centerX = canvasWidth / 2f

            // Proportions matching reference image: aspect ratio ~1.116:1 (squat, oblate dough ball)
            val bodyWidth = canvasWidth * 0.88f
            val bodyHeight = (bodyWidth / 1.116f) * breathScaleY
            val bodyCenterY = (canvasHeight * 0.46f) + floatOffset
            val halfW = bodyWidth / 2f
            val halfH = bodyHeight / 2f
            val bottomY = bodyCenterY + halfH

            // 1. Subtle floating elliptical drop-shadow on background beneath character
            val shadowCenterY = canvasHeight * 0.90f
            val shadowWidth = bodyWidth * (0.74f - (floatOffset / 90f))
            val shadowHeight = canvasHeight * (0.08f - (floatOffset / 180f))
            drawOval(
                color = Color.Black.copy(alpha = 0.22f),
                topLeft = Offset(centerX - shadowWidth / 2f, shadowCenterY - shadowHeight / 2f),
                size = Size(shadowWidth, shadowHeight)
            )

            // 2. Outline stroke weight
            val strokeWidth = (bodyWidth * 0.022f).coerceAtLeast(2.4f)

            // 3. Build smooth oblate mochi path (continuous curves, no flat edges or sharp angles)
            val mochiPath = buildMochiOblatePath(
                centerX = centerX,
                centerY = bodyCenterY,
                width = bodyWidth,
                height = bodyHeight
            )

            // 4. Fill solid bright white body
            drawPath(
                path = mochiPath,
                color = Color.White
            )

            // 5. Soft warm creamy peach inner shading along bottom and right inner curve
            drawPath(
                path = mochiPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color(0xFFF7ECE6).copy(alpha = 0.60f),
                        Color(0xFFEFE0D7).copy(alpha = 0.90f)
                    ),
                    center = Offset(centerX - halfW * 0.15f, bodyCenterY - halfH * 0.20f),
                    radius = halfW * 1.35f
                )
            )

            // 6. Subtle white specular shine streak along inner bottom-left curve
            val shinePath = Path().apply {
                moveTo(centerX - halfW * 0.85f, bodyCenterY + halfH * 0.42f)
                cubicTo(
                    centerX - halfW * 0.75f, bodyCenterY + halfH * 0.72f,
                    centerX - halfW * 0.45f, bottomY - halfH * 0.06f,
                    centerX - halfW * 0.22f, bottomY - halfH * 0.04f
                )
            }
            drawPath(
                path = shinePath,
                color = Color.White.copy(alpha = 0.65f),
                style = Stroke(width = strokeWidth * 0.55f, cap = StrokeCap.Round)
            )

            // 7. Draw warm dark-brown outline
            drawPath(
                path = mochiPath,
                color = OutlineBrown,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 8. Facial features coordinates
            val eyeSpacing = bodyWidth * 0.205f
            val eyeY = bodyCenterY + bodyHeight * 0.008f

            when (mood) {
                MascotMood.CALM -> drawCalmFace(
                    centerX = centerX,
                    eyeY = eyeY,
                    eyeSpacing = eyeSpacing,
                    bodyCenterY = bodyCenterY,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    strokeWidth = strokeWidth
                )
                MascotMood.AWARE -> drawAwareFace(
                    centerX = centerX,
                    eyeY = eyeY,
                    eyeSpacing = eyeSpacing,
                    bodyCenterY = bodyCenterY,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    strokeWidth = strokeWidth
                )
                MascotMood.URGENT -> drawUrgentFace(
                    centerX = centerX,
                    eyeY = eyeY,
                    eyeSpacing = eyeSpacing,
                    bodyCenterY = bodyCenterY,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    strokeWidth = strokeWidth
                )
                MascotMood.OVERDUE -> drawOverdueFace(
                    centerX = centerX,
                    eyeY = eyeY,
                    eyeSpacing = eyeSpacing,
                    bodyCenterY = bodyCenterY,
                    bodyWidth = bodyWidth,
                    bodyHeight = bodyHeight,
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

/**
 * Builds the smooth, oblate mochi silhouette:
 * - Plump, gently squished marshmallow / dough ball
 * - Relies entirely on continuous curves without any sharp angles, flat edges, or distinct limbs
 * - Continuous, sweeping rounded bottom curve (no flat base)
 * - Bottom-heavy: slightly wider near the lower base than at the top
 * - Squat proportions: width:height ratio ~1.116
 * - Perfectly symmetrical along vertical axis
 */
private fun buildMochiOblatePath(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
): Path {
    val halfW = width / 2f
    val halfH = height / 2f
    val topY = centerY - halfH
    val bottomY = centerY + halfH
    // Plump widest bulge settles slightly below vertical center
    val bulgeY = centerY + height * 0.04f

    val topRoundness = 0.78f
    val bottomRoundness = 0.72f

    return Path().apply {
        // Start at top center apex (tangent is strictly horizontal)
        moveTo(centerX, topY)

        // 1. Top center to right cheek apex
        cubicTo(
            centerX + halfW * topRoundness, topY,
            centerX + halfW, bulgeY - halfH * 0.45f,
            centerX + halfW, bulgeY
        )

        // 2. Right cheek apex down through rounded bottom corner to bottom center apex
        cubicTo(
            centerX + halfW, bulgeY + halfH * 0.55f,
            centerX + halfW * bottomRoundness, bottomY,
            centerX, bottomY
        )

        // 3. Bottom center apex through rounded bottom-left corner to left cheek apex
        cubicTo(
            centerX - halfW * bottomRoundness, bottomY,
            centerX - halfW, bulgeY + halfH * 0.55f,
            centerX - halfW, bulgeY
        )

        // 4. Left cheek apex to top center apex
        cubicTo(
            centerX - halfW, bulgeY - halfH * 0.45f,
            centerX - halfW * topRoundness, topY,
            centerX, topY
        )

        close()
    }
}

/**
 * CALM Expression:
 * Matches reference image:
 * Large dark-brown circular eyes with dual top-left catchlights (large at 10:30, small at 8:00),
 * soft round pastel-pink blush circles overlapping the lower-outer eye quadrant with white accents,
 * and a warm, playful "U"-shaped smile.
 */
private fun DrawScope.drawCalmFace(
    centerX: Float,
    eyeY: Float,
    eyeSpacing: Float,
    bodyCenterY: Float,
    bodyWidth: Float,
    bodyHeight: Float,
    strokeWidth: Float
) {
    val eyeRadius = bodyWidth * 0.062f
    val leftEyeX = centerX - eyeSpacing
    val rightEyeX = centerX + eyeSpacing

    // 1. Cheek blush circles overlapping lower-outer portion of eyes
    val blushColor = Color(0xFFF8B6BF).copy(alpha = 0.65f)
    val blushRadiusX = bodyWidth * 0.082f
    val blushRadiusY = bodyHeight * 0.075f
    val blushY = bodyCenterY + bodyHeight * 0.038f
    val leftBlushX = centerX - bodyWidth * 0.285f
    val rightBlushX = centerX + bodyWidth * 0.285f

    drawOval(
        color = blushColor,
        topLeft = Offset(leftBlushX - blushRadiusX, blushY - blushRadiusY),
        size = Size(blushRadiusX * 2f, blushRadiusY * 2f)
    )
    drawOval(
        color = blushColor,
        topLeft = Offset(rightBlushX - blushRadiusX, blushY - blushRadiusY),
        size = Size(blushRadiusX * 2f, blushRadiusY * 2f)
    )

    // Left blush shine arc
    val leftShinePath = Path().apply {
        moveTo(leftBlushX - blushRadiusX * 0.35f, blushY - blushRadiusY * 0.25f)
        cubicTo(
            leftBlushX - blushRadiusX * 0.55f, blushY - blushRadiusY * 0.05f,
            leftBlushX - blushRadiusX * 0.55f, blushY + blushRadiusY * 0.25f,
            leftBlushX - blushRadiusX * 0.25f, blushY + blushRadiusY * 0.40f
        )
    }
    drawPath(
        path = leftShinePath,
        color = Color.White.copy(alpha = 0.75f),
        style = Stroke(width = strokeWidth * 0.55f, cap = StrokeCap.Round)
    )

    // Right blush 3 tiny diagonal ticks
    val tickLen = blushRadiusX * 0.28f
    for (i in -1..1) {
        val dx = i * blushRadiusX * 0.26f
        val dy = i * blushRadiusY * 0.15f
        drawLine(
            color = Color.White.copy(alpha = 0.70f),
            start = Offset(rightBlushX + dx - tickLen * 0.4f, blushY + dy - tickLen * 0.5f),
            end = Offset(rightBlushX + dx + tickLen * 0.4f, blushY + dy + tickLen * 0.5f),
            strokeWidth = strokeWidth * 0.45f,
            cap = StrokeCap.Round
        )
    }

    // 2. Eyes in OutlineBrown
    drawCircle(color = OutlineBrown, radius = eyeRadius, center = Offset(leftEyeX, eyeY))
    drawCircle(color = OutlineBrown, radius = eyeRadius, center = Offset(rightEyeX, eyeY))

    // 3. Catchlight highlights (both eyes have dual highlights on the upper-left matching light source)
    for (eyeX in listOf(leftEyeX, rightEyeX)) {
        // Large highlight at 10:30 o'clock
        drawCircle(
            color = Color.White,
            radius = eyeRadius * 0.38f,
            center = Offset(eyeX - eyeRadius * 0.30f, eyeY - eyeRadius * 0.28f)
        )
        // Small highlight at 8 o'clock
        drawCircle(
            color = Color.White,
            radius = eyeRadius * 0.19f,
            center = Offset(eyeX - eyeRadius * 0.44f, eyeY + eyeRadius * 0.18f)
        )
    }

    // 4. Gentle, warm "U"-shaped smile
    val mouthW = bodyWidth * 0.125f
    val mouthH = bodyHeight * 0.042f
    val mouthY = bodyCenterY + bodyHeight * 0.038f
    val smilePath = Path().apply {
        moveTo(centerX - mouthW / 2f, mouthY)
        quadraticBezierTo(centerX, mouthY + mouthH * 1.55f, centerX + mouthW / 2f, mouthY)
    }
    drawPath(
        path = smilePath,
        color = OutlineBrown,
        style = Stroke(width = strokeWidth * 0.88f, cap = StrokeCap.Round)
    )
}

/**
 * AWARE Expression:
 * Eyes shrink to small dots, mouth forms a small round "o", blush fades.
 */
private fun DrawScope.drawAwareFace(
    centerX: Float,
    eyeY: Float,
    eyeSpacing: Float,
    bodyCenterY: Float,
    bodyWidth: Float,
    bodyHeight: Float,
    strokeWidth: Float
) {
    val dotRadius = (bodyWidth * 0.034f).coerceAtLeast(2.0f)

    // Eyes as small dots
    drawCircle(color = OutlineBrown, radius = dotRadius, center = Offset(centerX - eyeSpacing, eyeY))
    drawCircle(color = OutlineBrown, radius = dotRadius, center = Offset(centerX + eyeSpacing, eyeY))

    // Small round "o" mouth
    val mouthRadius = (bodyWidth * 0.032f).coerceAtLeast(2.0f)
    val mouthY = bodyCenterY + bodyHeight * 0.055f
    drawCircle(
        color = OutlineBrown,
        radius = mouthRadius,
        center = Offset(centerX, mouthY),
        style = Stroke(width = strokeWidth * 0.85f)
    )
}

/**
 * URGENT Expression:
 * Small furrowed-brow lines above eyes, inverted "V" mouth, deeper bolder pink cheeks
 * overlapping lower portion of eyes.
 */
private fun DrawScope.drawUrgentFace(
    centerX: Float,
    eyeY: Float,
    eyeSpacing: Float,
    bodyCenterY: Float,
    bodyWidth: Float,
    bodyHeight: Float,
    strokeWidth: Float
) {
    val eyeRadius = bodyWidth * 0.052f
    val leftEyeX = centerX - eyeSpacing
    val rightEyeX = centerX + eyeSpacing

    // Deeper/bolder pink cheeks overlapping lower portion of eyes
    val urgentBlushColor = Color(0xFFFB7185).copy(alpha = 0.75f)
    val blushRadiusX = bodyWidth * 0.075f
    val blushRadiusY = bodyHeight * 0.068f
    val blushY = bodyCenterY + bodyHeight * 0.040f
    val leftBlushX = centerX - bodyWidth * 0.285f
    val rightBlushX = centerX + bodyWidth * 0.285f

    drawOval(
        color = urgentBlushColor,
        topLeft = Offset(leftBlushX - blushRadiusX, blushY - blushRadiusY),
        size = Size(blushRadiusX * 2f, blushRadiusY * 2f)
    )
    drawOval(
        color = urgentBlushColor,
        topLeft = Offset(rightBlushX - blushRadiusX, blushY - blushRadiusY),
        size = Size(blushRadiusX * 2f, blushRadiusY * 2f)
    )

    // Medium eyes with top-left highlight
    drawCircle(color = OutlineBrown, radius = eyeRadius, center = Offset(leftEyeX, eyeY))
    drawCircle(
        color = Color.White,
        radius = eyeRadius * 0.32f,
        center = Offset(leftEyeX - eyeRadius * 0.28f, eyeY - eyeRadius * 0.28f)
    )

    drawCircle(color = OutlineBrown, radius = eyeRadius, center = Offset(rightEyeX, eyeY))
    drawCircle(
        color = Color.White,
        radius = eyeRadius * 0.32f,
        center = Offset(rightEyeX - eyeRadius * 0.28f, eyeY - eyeRadius * 0.28f)
    )

    // Small furrowed-brow lines angled downward toward center
    val browLen = bodyWidth * 0.065f
    val browDistY = bodyHeight * 0.075f
    // Left brow
    drawLine(
        color = OutlineBrown,
        start = Offset(leftEyeX - browLen * 0.55f, eyeY - browDistY - browLen * 0.25f),
        end = Offset(leftEyeX + browLen * 0.45f, eyeY - browDistY + browLen * 0.25f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )
    // Right brow
    drawLine(
        color = OutlineBrown,
        start = Offset(rightEyeX + browLen * 0.55f, eyeY - browDistY - browLen * 0.25f),
        end = Offset(rightEyeX - browLen * 0.45f, eyeY - browDistY + browLen * 0.25f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )

    // Inverted-"V" mouth (^)
    val mouthW = bodyWidth * 0.11f
    val mouthH = bodyHeight * 0.040f
    val mouthY = bodyCenterY + bodyHeight * 0.045f
    val invertedVMouth = Path().apply {
        moveTo(centerX - mouthW / 2f, mouthY + mouthH)
        lineTo(centerX, mouthY)
        lineTo(centerX + mouthW / 2f, mouthY + mouthH)
    }
    drawPath(
        path = invertedVMouth,
        color = OutlineBrown,
        style = Stroke(
            width = strokeWidth * 0.85f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * OVERDUE Expression:
 * Eyes droop, tiny blue tear-drops at corners, wobbly frown, blush fades away entirely.
 */
private fun DrawScope.drawOverdueFace(
    centerX: Float,
    eyeY: Float,
    eyeSpacing: Float,
    bodyCenterY: Float,
    bodyWidth: Float,
    bodyHeight: Float,
    strokeWidth: Float
) {
    val leftEyeX = centerX - eyeSpacing
    val rightEyeX = centerX + eyeSpacing

    val eyeW = bodyWidth * 0.09f
    val eyeH = bodyHeight * 0.040f

    // Drooping / sad eyes (curved arcs: ︶  ︶)
    val leftEyePath = Path().apply {
        moveTo(leftEyeX - eyeW / 2f, eyeY - eyeH * 0.2f)
        quadraticBezierTo(leftEyeX, eyeY + eyeH * 1.0f, leftEyeX + eyeW / 2f, eyeY - eyeH * 0.2f)
    }
    drawPath(
        path = leftEyePath,
        color = OutlineBrown,
        style = Stroke(width = strokeWidth * 1.05f, cap = StrokeCap.Round)
    )

    val rightEyePath = Path().apply {
        moveTo(rightEyeX - eyeW / 2f, eyeY - eyeH * 0.2f)
        quadraticBezierTo(rightEyeX, eyeY + eyeH * 1.0f, rightEyeX + eyeW / 2f, eyeY - eyeH * 0.2f)
    }
    drawPath(
        path = rightEyePath,
        color = OutlineBrown,
        style = Stroke(width = strokeWidth * 1.05f, cap = StrokeCap.Round)
    )

    // Tiny blue tear-drops at outer corners
    val tearColor = Color(0xFF60A5FA)
    val tearR = bodyWidth * 0.024f

    drawTeardrop(
        centerX = leftEyeX - eyeW * 0.55f,
        topY = eyeY + eyeH * 0.20f,
        radius = tearR,
        color = tearColor
    )
    drawTeardrop(
        centerX = rightEyeX + eyeW * 0.55f,
        topY = eyeY + eyeH * 0.20f,
        radius = tearR,
        color = tearColor
    )

    // Wobbly frown mouth
    val mouthW = bodyWidth * 0.11f
    val mouthH = bodyHeight * 0.038f
    val mouthY = bodyCenterY + bodyHeight * 0.048f
    val wobblyFrown = Path().apply {
        moveTo(centerX - mouthW / 2f, mouthY + mouthH * 0.5f)
        quadraticBezierTo(
            centerX - mouthW * 0.15f, mouthY - mouthH * 0.6f,
            centerX, mouthY - mouthH * 0.35f
        )
        quadraticBezierTo(
            centerX + mouthW * 0.15f, mouthY - mouthH * 0.6f,
            centerX + mouthW / 2f, mouthY + mouthH * 0.5f
        )
    }
    drawPath(
        path = wobblyFrown,
        color = OutlineBrown,
        style = Stroke(
            width = strokeWidth * 0.85f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draws a cute tiny teardrop droplet
 */
private fun DrawScope.drawTeardrop(
    centerX: Float,
    topY: Float,
    radius: Float,
    color: Color
) {
    val teardropPath = Path().apply {
        moveTo(centerX, topY)
        cubicTo(
            centerX + radius * 1.25f, topY + radius * 1.0f,
            centerX + radius * 1.25f, topY + radius * 2.4f,
            centerX, topY + radius * 2.4f
        )
        cubicTo(
            centerX - radius * 1.25f, topY + radius * 2.4f,
            centerX - radius * 1.25f, topY + radius * 1.0f,
            centerX, topY
        )
        close()
    }
    drawPath(path = teardropPath, color = color)
}
