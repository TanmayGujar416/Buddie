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

// Color tokens for Momo per docs/mascot-style-guide.md
private val MomoBodyLavender = Color(0xFFD4B8E8)
private val MomoOutlinePlum = Color(0xFF421D3B)
private val MomoInnerEarPink = Color(0xFFF4BFD3)
private val MomoBlushPink = Color(0xFFFFAEC0)
private val MomoUrgentBlushPink = Color(0xFFFF8CA3)

/**
 * Momo (Cat) Mascot Composable
 *
 * Implements Companion 2 per docs/mascot-style-guide.md:
 * - Sleek, slightly elongated oval body (taller than wide, distinct from Buddie's squat mochi)
 * - Two triangular pointed ears with inner ear shading
 * - Curled tail extending from base (adapts shape per mood)
 * - Thin whisker lines on cheeks
 * - Soft lavender-purple body, deep plum outline, soft pink blush
 * - 4 distinct mood expressions:
 *     CALM: half-lidded smirk, tail curled loosely at side
 *     AWARE: ears perk upright, eyes widen, tail straightens/flicks
 *     URGENT: wide eyes with shrunk pupils, ears flatten back ("airplane ears"), tail puffs up
 *     OVERDUE: eyes droop, tail drags, slumped posture
 * - Idle breathing and tail sway animation
 */
@Composable
fun MomoCatMascot(
    modifier: Modifier = Modifier,
    mood: MascotMood = MascotMood.CALM,
    size: Dp = 110.dp,
    animateIdle: Boolean = true
) {
    // Throttled idle animation (~20 updates/sec)
    val elapsedMs by rememberThrottledAnimationClock(enabled = animateIdle)
    val floatOffset = if (animateIdle) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -3f,
            targetValue = 3f,
            durationMs = 2200,
            easing = FastOutSlowInEasing
        )
    } else 0f

    val breathScaleY = if (animateIdle) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = 0.985f,
            targetValue = 1.015f,
            durationMs = 2200,
            easing = FastOutSlowInEasing
        )
    } else 1f

    val tailWagAngle = if (animateIdle && mood == MascotMood.CALM) {
        calculateThrottledValue(
            elapsedMs = elapsedMs,
            initialValue = -5f,
            targetValue = 6f,
            durationMs = 1600,
            easing = FastOutSlowInEasing
        )
    } else 0f

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Momo the cat companion, mood: ${mood.name.lowercase()}" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height

            // Momo base reference coordinate space: 100 x 95
            // Compute uniform scale factor to fit cleanly with breathing/floating
            val scale = (canvasW / 100f).coerceAtMost(canvasH / 95f)
            val offsetX = (canvasW - (100f * scale)) / 2f
            val offsetY = (canvasH - (95f * scale)) / 2f + floatOffset

            fun sx(x: Float): Float = offsetX + (x * scale)
            fun sy(y: Float): Float {
                // Apply subtle breath scaling around the center y (55)
                val relY = (y - 55f) * breathScaleY + 55f
                return offsetY + (relY * scale)
            }

            val strokeMain = (3.5f * scale).coerceAtLeast(2f)
            val strokeThick = (4.2f * scale).coerceAtLeast(2.4f)
            val strokeWhisker = (1.6f * scale).coerceAtLeast(1.2f)
            val strokeFeature = (2.2f * scale).coerceAtLeast(1.5f)

            // 1. Floating drop shadow beneath Momo
            val shadowWidth = 56f * scale
            val shadowHeight = 8f * scale
            val shadowY = sy(88f)
            drawOval(
                color = Color.Black.copy(alpha = 0.20f),
                topLeft = Offset(sx(50f) - shadowWidth / 2f, shadowY),
                size = Size(shadowWidth, shadowHeight)
            )

            // 2. Tail (drawn behind body)
            drawTail(mood, tailWagAngle, strokeThick, ::sx, ::sy)

            // 3. Ears (drawn behind/intersecting top of head)
            drawEars(mood, strokeMain, ::sx, ::sy)

            // 4. Body (elongated oval)
            drawBody(mood, strokeMain, ::sx, ::sy)

            // 5. Cheeks & Whiskers
            drawCheeksAndWhiskers(mood, strokeWhisker, ::sx, ::sy, scale)

            // 6. Facial Features (Eyes, Nose, Mouth per mood)
            drawFace(mood, strokeFeature, strokeMain, ::sx, ::sy, scale)
        }
    }
}

/**
 * Draws Momo's tail adapted for each mood expression.
 */
private fun DrawScope.drawTail(
    mood: MascotMood,
    tailWagAngle: Float,
    strokeThick: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    val tailPath = Path()
    when (mood) {
        MascotMood.CALM -> {
            // Curled loosely at right side
            rotate(degrees = tailWagAngle, pivot = Offset(sx(74f), sy(68f))) {
                tailPath.moveTo(sx(74f), sy(68f))
                tailPath.cubicTo(
                    sx(84f), sy(64f),
                    sx(92f), sy(56f),
                    sx(90f), sy(48f)
                )
                tailPath.cubicTo(
                    sx(88f), sy(42f),
                    sx(82f), sy(44f),
                    sx(82f), sy(48f)
                )
                tailPath.cubicTo(
                    sx(82f), sy(54f),
                    sx(78f), sy(62f),
                    sx(70f), sy(66f)
                )
                drawPath(
                    path = tailPath,
                    color = MomoOutlinePlum,
                    style = Stroke(width = strokeThick, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        MascotMood.AWARE -> {
            // Tail straightens/flicks alertly upward
            tailPath.moveTo(sx(74f), sy(68f))
            tailPath.quadraticBezierTo(sx(84f), sy(56f), sx(86f), sy(38f))
            tailPath.quadraticBezierTo(sx(87f), sy(32f), sx(90f), sy(34f))
            drawPath(
                path = tailPath,
                color = MomoOutlinePlum,
                style = Stroke(width = strokeThick, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        MascotMood.URGENT -> {
            // Puffed up bristled zig-zag tail
            val urgentStroke = strokeThick * 1.3f
            tailPath.moveTo(sx(74f), sy(66f))
            tailPath.lineTo(sx(80f), sy(62f))
            tailPath.lineTo(sx(78f), sy(57f))
            tailPath.lineTo(sx(85f), sy(52f))
            tailPath.lineTo(sx(82f), sy(46f))
            tailPath.lineTo(sx(88f), sy(40f))
            tailPath.lineTo(sx(82f), sy(36f))
            tailPath.lineTo(sx(86f), sy(30f))
            drawPath(
                path = tailPath,
                color = MomoOutlinePlum,
                style = Stroke(width = urgentStroke, cap = StrokeCap.Round, join = StrokeJoin.Bevel)
            )
        }
        MascotMood.OVERDUE -> {
            // Tail droops down and drags limp along ground
            tailPath.moveTo(sx(74f), sy(72f))
            tailPath.cubicTo(
                sx(80f), sy(82f),
                sx(86f), sy(86f),
                sx(94f), sy(86f)
            )
            drawPath(
                path = tailPath,
                color = MomoOutlinePlum,
                style = Stroke(width = strokeThick, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Draws Momo's triangular pointed ears with inner ear shading per mood.
 */
private fun DrawScope.drawEars(
    mood: MascotMood,
    strokeMain: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    val leftOuter = Path()
    val leftInner = Path()
    val rightOuter = Path()
    val rightInner = Path()

    when (mood) {
        MascotMood.CALM -> {
            // Relaxed pointed ears
            leftOuter.moveTo(sx(26f), sy(34f))
            leftOuter.lineTo(sx(32f), sy(8f))
            leftOuter.lineTo(sx(45f), sy(26f))
            leftOuter.close()

            leftInner.moveTo(sx(29f), sy(31f))
            leftInner.lineTo(sx(34f), sy(14f))
            leftInner.lineTo(sx(42f), sy(26f))
            leftInner.close()

            rightOuter.moveTo(sx(74f), sy(34f))
            rightOuter.lineTo(sx(68f), sy(8f))
            rightOuter.lineTo(sx(55f), sy(26f))
            rightOuter.close()

            rightInner.moveTo(sx(71f), sy(31f))
            rightInner.lineTo(sx(66f), sy(14f))
            rightInner.lineTo(sx(58f), sy(26f))
            rightInner.close()
        }
        MascotMood.AWARE -> {
            // Ears perk fully upright and tall
            leftOuter.moveTo(sx(26f), sy(30f))
            leftOuter.lineTo(sx(29f), sy(2f))
            leftOuter.lineTo(sx(45f), sy(24f))
            leftOuter.close()

            leftInner.moveTo(sx(29f), sy(27f))
            leftInner.lineTo(sx(31f), sy(8f))
            leftInner.lineTo(sx(42f), sy(24f))
            leftInner.close()

            rightOuter.moveTo(sx(74f), sy(30f))
            rightOuter.lineTo(sx(71f), sy(2f))
            rightOuter.lineTo(sx(55f), sy(24f))
            rightOuter.close()

            rightInner.moveTo(sx(71f), sy(27f))
            rightInner.lineTo(sx(69f), sy(8f))
            rightInner.lineTo(sx(58f), sy(24f))
            rightInner.close()
        }
        MascotMood.URGENT -> {
            // Flattened airplane ears pointing out
            leftOuter.moveTo(sx(26f), sy(38f))
            leftOuter.lineTo(sx(12f), sy(22f))
            leftOuter.lineTo(sx(38f), sy(28f))
            leftOuter.close()

            leftInner.moveTo(sx(26f), sy(35f))
            leftInner.lineTo(sx(16f), sy(25f))
            leftInner.lineTo(sx(35f), sy(28f))
            leftInner.close()

            rightOuter.moveTo(sx(74f), sy(38f))
            rightOuter.lineTo(sx(88f), sy(22f))
            rightOuter.lineTo(sx(62f), sy(28f))
            rightOuter.close()

            rightInner.moveTo(sx(74f), sy(35f))
            rightInner.lineTo(sx(84f), sy(25f))
            rightInner.lineTo(sx(65f), sy(28f))
            rightInner.close()
        }
        MascotMood.OVERDUE -> {
            // Drooping, flattened slumped ears
            leftOuter.moveTo(sx(26f), sy(44f))
            leftOuter.lineTo(sx(14f), sy(36f))
            leftOuter.lineTo(sx(38f), sy(32f))
            leftOuter.close()

            leftInner.moveTo(sx(26f), sy(41f))
            leftInner.lineTo(sx(17f), sy(37f))
            leftInner.lineTo(sx(34f), sy(33f))
            leftInner.close()

            rightOuter.moveTo(sx(74f), sy(44f))
            rightOuter.lineTo(sx(86f), sy(36f))
            rightOuter.lineTo(sx(62f), sy(32f))
            rightOuter.close()

            rightInner.moveTo(sx(74f), sy(41f))
            rightInner.lineTo(sx(83f), sy(37f))
            rightInner.lineTo(sx(66f), sy(33f))
            rightInner.close()
        }
    }

    // Fill left ear outer lavender & inner pink
    drawPath(path = leftOuter, color = MomoBodyLavender)
    drawPath(path = leftInner, color = MomoInnerEarPink)
    drawPath(path = leftOuter, color = MomoOutlinePlum, style = Stroke(width = strokeMain, join = StrokeJoin.Round))

    // Fill right ear outer lavender & inner pink
    drawPath(path = rightOuter, color = MomoBodyLavender)
    drawPath(path = rightInner, color = MomoInnerEarPink)
    drawPath(path = rightOuter, color = MomoOutlinePlum, style = Stroke(width = strokeMain, join = StrokeJoin.Round))
}

/**
 * Draws Momo's sleek elongated oval body.
 */
private fun DrawScope.drawBody(
    mood: MascotMood,
    strokeMain: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float
) {
    val bodyPath = Path()
    if (mood == MascotMood.OVERDUE) {
        // Slumped, flatter body
        bodyPath.moveTo(sx(50f), sy(26f))
        bodyPath.cubicTo(sx(28f), sy(26f), sx(20f), sy(42f), sx(20f), sy(66f))
        bodyPath.cubicTo(sx(20f), sy(84f), sx(30f), sy(90f), sx(50f), sy(90f))
        bodyPath.cubicTo(sx(70f), sy(90f), sx(80f), sy(84f), sx(80f), sy(66f))
        bodyPath.cubicTo(sx(80f), sy(42f), sx(72f), sy(26f), sx(50f), sy(26f))
        bodyPath.close()
    } else {
        // Standard sleek elongated oval
        val topY = if (mood == MascotMood.URGENT) 22f else 20f
        val botY = if (mood == MascotMood.URGENT) 88f else 86f
        bodyPath.moveTo(sx(50f), sy(topY))
        bodyPath.cubicTo(sx(30f), sy(topY), sx(22f), sy(36f), sx(22f), sy(60f))
        bodyPath.cubicTo(sx(22f), sy(78f), sx(32f), sy(botY), sx(50f), sy(botY))
        bodyPath.cubicTo(sx(68f), sy(botY), sx(78f), sy(78f), sx(78f), sy(60f))
        bodyPath.cubicTo(sx(78f), sy(36f), sx(70f), sy(topY), sx(50f), sy(topY))
        bodyPath.close()
    }

    // Fill lavender body
    drawPath(path = bodyPath, color = MomoBodyLavender)

    // Subtle soft gradient highlight on body for depth
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.Transparent,
                MomoOutlinePlum.copy(alpha = 0.08f)
            ),
            center = Offset(sx(48f), sy(48f)),
            radius = (36f * (sx(10f) - sx(0f))).coerceAtLeast(30f)
        )
    )

    // Outline
    drawPath(
        path = bodyPath,
        color = MomoOutlinePlum,
        style = Stroke(width = strokeMain, join = StrokeJoin.Round)
    )
}

/**
 * Draws Momo's cheeks (blush) and whiskers.
 */
private fun DrawScope.drawCheeksAndWhiskers(
    mood: MascotMood,
    strokeWhisker: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    scale: Float
) {
    // 1. Blush circles
    when (mood) {
        MascotMood.CALM -> {
            drawOval(
                color = MomoBlushPink.copy(alpha = 0.85f),
                topLeft = Offset(sx(32f) - 5f * scale, sy(62f) - 3f * scale),
                size = Size(10f * scale, 6f * scale)
            )
            drawOval(
                color = MomoBlushPink.copy(alpha = 0.85f),
                topLeft = Offset(sx(68f) - 5f * scale, sy(62f) - 3f * scale),
                size = Size(10f * scale, 6f * scale)
            )
        }
        MascotMood.AWARE -> {
            drawOval(
                color = MomoBlushPink.copy(alpha = 0.60f),
                topLeft = Offset(sx(32f) - 4f * scale, sy(62f) - 2.5f * scale),
                size = Size(8f * scale, 5f * scale)
            )
            drawOval(
                color = MomoBlushPink.copy(alpha = 0.60f),
                topLeft = Offset(sx(68f) - 4f * scale, sy(62f) - 2.5f * scale),
                size = Size(8f * scale, 5f * scale)
            )
        }
        MascotMood.URGENT -> {
            drawOval(
                color = MomoUrgentBlushPink.copy(alpha = 0.90f),
                topLeft = Offset(sx(32f) - 6f * scale, sy(65f) - 3.5f * scale),
                size = Size(12f * scale, 7f * scale)
            )
            drawOval(
                color = MomoUrgentBlushPink.copy(alpha = 0.90f),
                topLeft = Offset(sx(68f) - 6f * scale, sy(65f) - 3.5f * scale),
                size = Size(12f * scale, 7f * scale)
            )
        }
        MascotMood.OVERDUE -> {
            // Faded away completely
        }
    }

    // 2. Whisker lines
    when (mood) {
        MascotMood.CALM -> {
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(58f)), Offset(sx(12f), sy(55f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(62f)), Offset(sx(11f), sy(63f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(58f)), Offset(sx(88f), sy(55f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(62f)), Offset(sx(89f), sy(63f)), strokeWhisker, StrokeCap.Round)
        }
        MascotMood.AWARE -> {
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(57f)), Offset(sx(10f), sy(52f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(62f)), Offset(sx(11f), sy(62f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(57f)), Offset(sx(90f), sy(52f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(62f)), Offset(sx(89f), sy(62f)), strokeWhisker, StrokeCap.Round)
        }
        MascotMood.URGENT -> {
            val qStroke = strokeWhisker * 1.15f
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(60f)), Offset(sx(10f), sy(58f)), qStroke, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(64f)), Offset(sx(11f), sy(67f)), qStroke, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(60f)), Offset(sx(90f), sy(58f)), qStroke, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(64f)), Offset(sx(89f), sy(67f)), qStroke, StrokeCap.Round)
        }
        MascotMood.OVERDUE -> {
            // Drooping whiskers
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(64f)), Offset(sx(12f), sy(70f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(24f), sy(69f)), Offset(sx(13f), sy(76f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(64f)), Offset(sx(88f), sy(70f)), strokeWhisker, StrokeCap.Round)
            drawLine(MomoOutlinePlum, Offset(sx(76f), sy(69f)), Offset(sx(87f), sy(76f)), strokeWhisker, StrokeCap.Round)
        }
    }
}

/**
 * Draws Momo's facial expressions (Eyes, Nose, Mouth) per mood state.
 */
private fun DrawScope.drawFace(
    mood: MascotMood,
    strokeFeature: Float,
    strokeMain: Float,
    sx: (Float) -> Float,
    sy: (Float) -> Float,
    scale: Float
) {
    // Nose (small inverted triangle at center)
    val nosePath = Path().apply {
        moveTo(sx(49f), sy(58f))
        lineTo(sx(51f), sy(58f))
        lineTo(sx(50f), sy(60f))
        close()
    }
    drawPath(path = nosePath, color = MomoOutlinePlum)

    when (mood) {
        MascotMood.CALM -> {
            // Eyes: Half-lidded content smirk arcs with white highlight
            val leftEye = Path().apply {
                moveTo(sx(34f), sy(52f))
                quadraticBezierTo(sx(39f), sy(47f), sx(44f), sy(53f))
            }
            val rightEye = Path().apply {
                moveTo(sx(66f), sy(52f))
                quadraticBezierTo(sx(61f), sy(47f), sx(56f), sy(53f))
            }
            drawPath(path = leftEye, color = MomoOutlinePlum, style = Stroke(width = strokeMain * 0.9f, cap = StrokeCap.Round))
            drawPath(path = rightEye, color = MomoOutlinePlum, style = Stroke(width = strokeMain * 0.9f, cap = StrokeCap.Round))
            drawCircle(color = Color.White, radius = 1.3f * scale, center = Offset(sx(39f), sy(50f)))
            drawCircle(color = Color.White, radius = 1.3f * scale, center = Offset(sx(61f), sy(50f)))

            // Mouth: Mischievous closed-mouth cat smirk (:3 style curl)
            val mouthPath = Path().apply {
                moveTo(sx(46f), sy(61f))
                quadraticBezierTo(sx(49f), sy(63.5f), sx(51f), sy(61.5f))
                quadraticBezierTo(sx(54f), sy(60.5f), sx(56f), sy(59f))
            }
            drawPath(path = mouthPath, color = MomoOutlinePlum, style = Stroke(width = strokeFeature, cap = StrokeCap.Round))
        }

        MascotMood.AWARE -> {
            // Eyes: Wide open circular/oval with vertical oval pupils and catchlight
            drawOval(
                color = MomoOutlinePlum,
                topLeft = Offset(sx(38f) - 5.5f * scale, sy(52f) - 6.5f * scale),
                size = Size(11f * scale, 13f * scale)
            )
            drawOval(
                color = Color(0xFF200B1D),
                topLeft = Offset(sx(37f) - 2.5f * scale, sy(51f) - 5f * scale),
                size = Size(5f * scale, 10f * scale)
            )
            drawCircle(color = Color.White, radius = 1.6f * scale, center = Offset(sx(36f), sy(49f)))

            drawOval(
                color = MomoOutlinePlum,
                topLeft = Offset(sx(62f) - 5.5f * scale, sy(52f) - 6.5f * scale),
                size = Size(11f * scale, 13f * scale)
            )
            drawOval(
                color = Color(0xFF200B1D),
                topLeft = Offset(sx(61f) - 2.5f * scale, sy(51f) - 5f * scale),
                size = Size(5f * scale, 10f * scale)
            )
            drawCircle(color = Color.White, radius = 1.6f * scale, center = Offset(sx(60f), sy(49f)))

            // Mouth: Small curious "o"
            drawCircle(
                color = MomoOutlinePlum,
                radius = 2.2f * scale,
                center = Offset(sx(50f), sy(63f)),
                style = Stroke(width = strokeFeature, cap = StrokeCap.Round)
            )
        }

        MascotMood.URGENT -> {
            // Eyes: Huge startled sclera with tiny shrunk pinpoint pupils
            drawCircle(
                color = Color.White,
                radius = 7f * scale,
                center = Offset(sx(37f), sy(54f))
            )
            drawCircle(
                color = MomoOutlinePlum,
                radius = 7f * scale,
                center = Offset(sx(37f), sy(54f)),
                style = Stroke(width = strokeFeature, cap = StrokeCap.Round)
            )
            drawCircle(
                color = MomoOutlinePlum,
                radius = 2.2f * scale,
                center = Offset(sx(37f), sy(54f))
            )

            drawCircle(
                color = Color.White,
                radius = 7f * scale,
                center = Offset(sx(63f), sy(54f))
            )
            drawCircle(
                color = MomoOutlinePlum,
                radius = 7f * scale,
                center = Offset(sx(63f), sy(54f)),
                style = Stroke(width = strokeFeature, cap = StrokeCap.Round)
            )
            drawCircle(
                color = MomoOutlinePlum,
                radius = 2.2f * scale,
                center = Offset(sx(63f), sy(54f))
            )

            // Mouth: Anxious quivering mouth
            val mouthPath = Path().apply {
                moveTo(sx(46f), sy(64f))
                quadraticBezierTo(sx(50f), sy(61f), sx(54f), sy(64f))
            }
            drawPath(path = mouthPath, color = MomoOutlinePlum, style = Stroke(width = strokeFeature * 1.2f, cap = StrokeCap.Round))
        }

        MascotMood.OVERDUE -> {
            // Eyes: Drooping half-closed weary slitted eyes
            val leftEye = Path().apply {
                moveTo(sx(33f), sy(58f))
                quadraticBezierTo(sx(39f), sy(63f), sx(45f), sy(59f))
            }
            val rightEye = Path().apply {
                moveTo(sx(67f), sy(58f))
                quadraticBezierTo(sx(61f), sy(63f), sx(55f), sy(59f))
            }
            drawPath(path = leftEye, color = MomoOutlinePlum, style = Stroke(width = strokeMain * 0.9f, cap = StrokeCap.Round))
            drawPath(path = rightEye, color = MomoOutlinePlum, style = Stroke(width = strokeMain * 0.9f, cap = StrokeCap.Round))
            drawCircle(color = MomoOutlinePlum, radius = 1.5f * scale, center = Offset(sx(39f), sy(62f)))
            drawCircle(color = MomoOutlinePlum, radius = 1.5f * scale, center = Offset(sx(61f), sy(62f)))

            // Mouth: Comically flat/frown ("given up")
            val mouthPath = Path().apply {
                moveTo(sx(46f), sy(68f))
                quadraticBezierTo(sx(50f), sy(65f), sx(54f), sy(68f))
            }
            drawPath(path = mouthPath, color = MomoOutlinePlum, style = Stroke(width = strokeFeature * 1.2f, cap = StrokeCap.Round))
        }
    }
}
