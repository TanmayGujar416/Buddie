package com.tanmay.buddie.presentation.components.mascot

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Color tokens for Wisp per docs/mascot-style-guide.md
private val WispOutlineGreyBlue = Color(0xFF5B7B96)
private val WispFeatureGreyBlue = Color(0xFF4A657D)
private val WispPupilDark = Color(0xFF364F66)
private val WispSparkleSky = Color(0xFF7DD3FC)
private val WispSparkleLavender = Color(0xFFC084FC)

private val WispGradColors = listOf(
    Color(0xFFE0F2FE),
    Color(0xFFBAE6FD),
    Color(0xFFC7D2FE),
    Color(0xFFDDD6FE)
)

private val WispOverdueGradColors = listOf(
    Color(0xFFCBD5E1),
    Color(0xFF94A3B8),
    Color(0xFFA5B4FC)
)

/**
 * Wisp (Cloud/Ghost) Mascot Composable
 *
 * Implements Companion 3 per docs/mascot-style-guide.md:
 * - Amorphous soft cloud/ghost silhouette with NO flat bottom — wavy wispy trailing tail at base
 * - Pale sky-blue to soft lavender gradient body, light soft grey-blue outline
 * - No cheek blush (maintains pure ethereal, airy aesthetic)
 * - 4 distinct mood expressions:
 *     CALM: closed dreamy downward-arc eyes, drifting sparkle motifs, swaying wispy tail
 *     AWARE: one eye partially opens in curious glance, slight inquisitive head tilt
 *     URGENT: wispy tail becomes jagged/quivering, eyes open wide and startled round
 *     OVERDUE: body sinks/shrinks deflated, soft teardrop-shaped eyes, tail flattens horizontally
 * - Idle floating bob and sparkle twinkle animations
 */
@Composable
fun WispMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.CALM,
    size: Dp = 110.dp,
    animateIdle: Boolean = true
) {
    // Throttled idle animation (~20 updates/sec)
    val elapsedMs by rememberThrottledAnimationClock(enabled = animateIdle)

    val floatOffset = if (animateIdle && mood != MascotMood.OVERDUE) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -4.5f,
            targetValue = 4.5f,
            durationMs = 2400,
            easing = FastOutSlowInEasing
        )
    } else 0f

    val sparkleAlpha = if (animateIdle && mood == MascotMood.CALM) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = 0.35f,
            targetValue = 1f,
            durationMs = 1600,
            easing = FastOutSlowInEasing
        )
    } else 1f

    val tailSwayAngle = if (animateIdle && mood == MascotMood.CALM) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -3.5f,
            targetValue = 4f,
            durationMs = 2000,
            easing = FastOutSlowInEasing
        )
    } else 0f

    val quiverOffset = if (animateIdle && mood == MascotMood.URGENT) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -1.5f,
            targetValue = 1.5f,
            durationMs = 90,
            easing = LinearEasing
        )
    } else 0f

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Wisp the cloud ghost companion, mood: ${mood.name.lowercase()}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height

            // Wisp reference coordinate space: 100 x 100
            val scale = (canvasW / 100f).coerceAtMost(canvasH / 100f)
            val offsetX = (canvasW - (100f * scale)) / 2f + quiverOffset
            val offsetY = (canvasH - (100f * scale)) / 2f + floatOffset

            fun sx(x: Float): Float = offsetX + (x * scale)
            fun sy(y: Float): Float = offsetY + (y * scale)

            val strokeOutline = (3.2f * scale).coerceAtLeast(2f)
            val strokeFeature = (2.6f * scale).coerceAtLeast(1.8f)

            // 1. Drifting Sparkles for CALM (drawn outside body)
            if (mood == MascotMood.CALM) {
                drawSparkles(sparkleAlpha, scale, ::sx, ::sy)
            }

            // 2. Head Tilt for AWARE
            val rotationAngle = if (mood == MascotMood.AWARE) 5.5f else 0f

            rotate(degrees = rotationAngle, pivot = Offset(sx(50f), sy(50f))) {
                // 3. Amorphous Body with Wispy Trailing Tail (NO flat bottom!)
                drawWispBody(mood, strokeOutline, tailSwayAngle, scale, ::sx, ::sy)

                // 4. Facial Expression per mood
                drawWispFace(mood, strokeFeature, scale, ::sx, ::sy)
            }
        }
    }
}

/**
 * Draws the 4-point sparkle stardust motifs drifting around Wisp in Calm state.
 */
private fun DrawScope.drawSparkles(
    alpha: Float,
    scale: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    fun drawStar(cx: Float, cy: Float, r: Float, color: Color) {
        val path = Path().apply {
            moveTo(cx, cy - r)
            quadraticBezierTo(cx, cy, cx + r, cy)
            quadraticBezierTo(cx, cy, cx, cy + r)
            quadraticBezierTo(cx, cy, cx - r, cy)
            quadraticBezierTo(cx, cy, cx, cy - r)
            close()
        }
        drawPath(path = path, color = color.copy(alpha = alpha))
    }

    // Sparkle 1 (Top-Right)
    drawStar(sx(82f), sy(25f), 5.5f * scale, WispSparkleSky)

    // Sparkle 2 (Left)
    drawStar(sx(16f), sy(40f), 4.5f * scale, WispSparkleLavender)

    // Sparkle 3 (Bottom-Right)
    drawStar(sx(80f), sy(66f), 4f * scale, Color.White)
}

/**
 * Draws Wisp's amorphous cloud/ghost body with its wispy trailing tail per mood state.
 */
private fun DrawScope.drawWispBody(
    mood: MascotMood,
    strokeOutline: Float,
    tailSwayAngle: Float,
    scale: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    val bodyPath = Path()

    when (mood) {
        MascotMood.CALM, MascotMood.AWARE -> {
            bodyPath.moveTo(sx(50f), sy(14f))
            bodyPath.cubicTo(sx(66f), sy(14f), sx(78f), sy(24f), sx(80f), sy(42f))
            bodyPath.cubicTo(sx(82f), sy(56f), sx(76f), sy(66f), sx(70f), sy(72f))
            // Wispy flowing curved tail with gentle sway
            bodyPath.cubicTo(sx(66f), sy(76f), sx(64f), sy(82f), sx(72f + tailSwayAngle), sy(88f))
            bodyPath.cubicTo(sx(62f), sy(90f), sx(52f), sy(84f), sx(46f), sy(78f))
            bodyPath.cubicTo(sx(40f), sy(73f), sx(34f), sy(76f), sx(30f), sy(72f))
            bodyPath.cubicTo(sx(22f), sy(66f), sx(18f), sy(54f), sx(20f), sy(40f))
            bodyPath.cubicTo(sx(22f), sy(24f), sx(34f), sy(14f), sx(50f), sy(14f))
            bodyPath.close()
        }

        MascotMood.URGENT -> {
            // Jagged, quivering agitated trailing tail
            bodyPath.moveTo(sx(50f), sy(14f))
            bodyPath.cubicTo(sx(66f), sy(14f), sx(78f), sy(24f), sx(80f), sy(42f))
            bodyPath.cubicTo(sx(82f), sy(56f), sx(76f), sy(64f), sx(74f), sy(68f))
            bodyPath.lineTo(sx(82f), sy(72f))
            bodyPath.lineTo(sx(72f), sy(78f))
            bodyPath.lineTo(sx(84f), sy(84f))
            bodyPath.lineTo(sx(70f), sy(88f))
            bodyPath.lineTo(sx(76f), sy(94f))
            bodyPath.lineTo(sx(62f), sy(90f))
            bodyPath.lineTo(sx(54f), sy(96f))
            bodyPath.lineTo(sx(46f), sy(86f))
            bodyPath.lineTo(sx(38f), sy(92f))
            bodyPath.lineTo(sx(32f), sy(82f))
            bodyPath.cubicTo(sx(22f), sy(72f), sx(18f), sy(56f), sx(20f), sy(40f))
            bodyPath.cubicTo(sx(22f), sy(24f), sx(34f), sy(14f), sx(50f), sy(14f))
            bodyPath.close()
        }

        MascotMood.OVERDUE -> {
            // Deflated, sunk body; wispy tail flattens out horizontally along ground
            bodyPath.moveTo(sx(50f), sy(28f))
            bodyPath.cubicTo(sx(64f), sy(28f), sx(74f), sy(36f), sx(76f), sy(52f))
            bodyPath.cubicTo(sx(78f), sy(66f), sx(74f), sy(76f), sx(86f), sy(86f))
            bodyPath.cubicTo(sx(66f), sy(88f), sx(34f), sy(88f), sx(14f), sy(86f))
            bodyPath.cubicTo(sx(26f), sy(76f), sx(22f), sy(66f), sx(24f), sy(52f))
            bodyPath.cubicTo(sx(26f), sy(36f), sx(36f), sy(28f), sx(50f), sy(28f))
            bodyPath.close()

            // Ground pool shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.20f),
                topLeft = Offset(sx(12f), sy(85f)),
                size = Size(76f * scale, 7f * scale)
            )
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = if (mood == MascotMood.OVERDUE) WispOverdueGradColors else WispGradColors,
        start = Offset(sx(20f), sy(14f)),
        end = Offset(sx(80f), sy(88f))
    )

    // Fill gradient body
    drawPath(path = bodyPath, brush = gradientBrush)

    // Subtle soft highlight on cloud crest
    if (mood != MascotMood.OVERDUE) {
        val highlightPath = Path().apply {
            moveTo(sx(34f), sy(26f))
            cubicTo(sx(44f), sy(20f), sx(58f), sy(20f), sx(68f), sy(26f))
        }
        drawPath(
            path = highlightPath,
            color = Color.White.copy(alpha = 0.55f),
            style = Stroke(width = (2.2f * scale).coerceAtLeast(1.4f), cap = StrokeCap.Round)
        )
    }

    // Outline stroke
    drawPath(
        path = bodyPath,
        color = WispOutlineGreyBlue,
        style = Stroke(width = strokeOutline, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

/**
 * Draws Wisp's facial expressions per mood state.
 */
private fun DrawScope.drawWispFace(
    mood: MascotMood,
    strokeFeature: Float,
    scale: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    when (mood) {
        MascotMood.CALM -> {
            // Gentle downward dreamy closed arcs
            val leftEye = Path().apply {
                moveTo(sx(35f), sy(44f))
                quadraticBezierTo(sx(41f), sy(49f), sx(47f), sy(44f))
            }
            val rightEye = Path().apply {
                moveTo(sx(55f), sy(44f))
                quadraticBezierTo(sx(61f), sy(49f), sx(67f), sy(44f))
            }
            drawPath(path = leftEye, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature, cap = StrokeCap.Round))
            drawPath(path = rightEye, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature, cap = StrokeCap.Round))

            // Peaceful tiny smile
            val mouth = Path().apply {
                moveTo(sx(48f), sy(53f))
                quadraticBezierTo(sx(51f), sy(55f), sx(54f), sy(53f))
            }
            drawPath(path = mouth, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature * 0.85f, cap = StrokeCap.Round))
        }

        MascotMood.AWARE -> {
            // Left Eye: Gentle closed arc
            val leftEye = Path().apply {
                moveTo(sx(34f), sy(45f))
                quadraticBezierTo(sx(40f), sy(50f), sx(46f), sy(45f))
            }
            drawPath(path = leftEye, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature, cap = StrokeCap.Round))

            // Right Eye: Curious, partially open almond/circular gaze
            drawOval(
                color = Color.White,
                topLeft = Offset(sx(55f), sy(36.5f)),
                size = Size(12f * scale, 13f * scale)
            )
            drawOval(
                color = WispFeatureGreyBlue,
                topLeft = Offset(sx(55f), sy(36.5f)),
                size = Size(12f * scale, 13f * scale),
                style = Stroke(width = strokeFeature * 0.9f)
            )
            drawOval(
                color = WispPupilDark,
                topLeft = Offset(sx(58.5f), sy(39f)),
                size = Size(7f * scale, 8.5f * scale)
            )
            drawCircle(
                color = Color.White,
                radius = 1.4f * scale,
                center = Offset(sx(63.5f), sy(41f))
            )

            // Inquisitive mouth
            val mouth = Path().apply {
                moveTo(sx(49f), sy(53f))
                quadraticBezierTo(sx(52f), sy(55f), sx(55f), sy(53f))
            }
            drawPath(path = mouth, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature * 0.85f, cap = StrokeCap.Round))
        }

        MascotMood.URGENT -> {
            // Startled wide round eyes with white sclera and dark pupils
            drawCircle(
                color = Color.White,
                radius = 7.5f * scale,
                center = Offset(sx(38f), sy(44f))
            )
            drawCircle(
                color = WispFeatureGreyBlue,
                radius = 7.5f * scale,
                center = Offset(sx(38f), sy(44f)),
                style = Stroke(width = strokeFeature * 0.95f)
            )
            drawCircle(
                color = WispPupilDark,
                radius = 2.8f * scale,
                center = Offset(sx(38f), sy(44f))
            )
            drawCircle(
                color = Color.White,
                radius = 1.0f * scale,
                center = Offset(sx(40f), sy(42f))
            )

            drawCircle(
                color = Color.White,
                radius = 7.5f * scale,
                center = Offset(sx(62f), sy(44f))
            )
            drawCircle(
                color = WispFeatureGreyBlue,
                radius = 7.5f * scale,
                center = Offset(sx(62f), sy(44f)),
                style = Stroke(width = strokeFeature * 0.95f)
            )
            drawCircle(
                color = WispPupilDark,
                radius = 2.8f * scale,
                center = Offset(sx(62f), sy(44f))
            )
            drawCircle(
                color = Color.White,
                radius = 1.0f * scale,
                center = Offset(sx(64f), sy(42f))
            )

            // Distressed "o" gasp mouth
            drawCircle(
                color = WispFeatureGreyBlue,
                radius = 2.6f * scale,
                center = Offset(sx(50f), sy(54f)),
                style = Stroke(width = strokeFeature * 0.85f)
            )
        }

        MascotMood.OVERDUE -> {
            // Soft teardrop-shaped sad eyes (drooping in defeat)
            fun drawTeardrop(cx: Float, cy: Float) {
                val tdPath = Path().apply {
                    moveTo(cx, cy - 6f * scale)
                    cubicTo(cx + 3.5f * scale, cy - 2f * scale, cx + 4f * scale, cy + 3.5f * scale, cx, cy + 4.5f * scale)
                    cubicTo(cx - 4f * scale, cy + 3.5f * scale, cx - 3.5f * scale, cy - 2f * scale, cx, cy - 6f * scale)
                    close()
                }
                drawPath(path = tdPath, color = WispPupilDark)
                drawPath(path = tdPath, color = WispFeatureGreyBlue, style = Stroke(width = 1.6f * scale))
            }

            drawTeardrop(sx(38f), sy(48f))
            drawTeardrop(sx(62f), sy(48f))

            // Soft downward deflated mouth
            val mouth = Path().apply {
                moveTo(sx(47f), sy(61f))
                quadraticBezierTo(sx(50f), sy(58f), sx(53f), sy(61f))
            }
            drawPath(path = mouth, color = WispFeatureGreyBlue, style = Stroke(width = strokeFeature * 0.85f, cap = StrokeCap.Round))
        }
    }
}
