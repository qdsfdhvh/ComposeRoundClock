package com.seiko.composeclock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.seiko.composeclock.ui.theme.ComposeClockTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun ComposeRoundClock(
    clockState: ClockState,
    modifier: Modifier = Modifier,
    hourCount: Int = 12,
    minuteCount: Int = 60,
    secondCount: Int = 60,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = CircleShape,
) {
    // 转动 -90 度，使 12 点对应 0 度
    val clockRotation = -90f

    fun Int.toAngleCompat(count: Int): Float {
        return toAngle(count) + clockRotation
    }

    suspend fun scrollTo(
        animatable: Animatable<Float, AnimationVector1D>,
        value: Int,
        count: Int,
    ) {
        val targetValue = value.toAngleCompat(count)
        val unitAngle = 360f / count
        when {
            animatable.value == targetValue -> Unit
            abs(animatable.value - targetValue) > unitAngle -> {
                animatable.snapTo(targetValue)
            }

            else -> {
                animatable.animateTo(targetValue, tween(950, easing = LinearEasing))
                // 12 点位置，360 度重置为 0 度
                if ((360f + clockRotation - unitAngle).compareTo(animatable.value) == 0) {
                    animatable.snapTo(clockRotation - unitAngle)
                }
            }
        }
    }

    // 用于显示的动画
    val animateHourAngle = remember {
        Animatable(clockState.time.hour.toAngleCompat(hourCount))
    }
    val animateMinuteAngle = remember {
        Animatable(clockState.time.minute.toAngleCompat(minuteCount))
    }
    val animateSecondAngle = remember {
        Animatable(clockState.time.second.toAngleCompat(secondCount))
    }

    LaunchedEffect(clockState) {
        snapshotFlow { clockState.time }.collect { time ->
            launch {
                scrollTo(animateHourAngle, time.hour, hourCount)
            }
            launch {
                scrollTo(animateMinuteAngle, time.minute, minuteCount)
            }
            launch {
                scrollTo(animateSecondAngle, time.second, secondCount)
            }
        }
    }

    Surface(
        color = containerColor,
        modifier = modifier,
        shape = shape,
    ) {
        Canvas(
            modifier = Modifier.aspectRatio(1f),
        ) {
            val boldStrokeLength = 4.dp.toPx()
            val normalStrokeLength = 2.dp.toPx()

            val targetOuterRadius = (size.width / 2 * 0.9).toFloat()
            val outerRadius = (size.width / 2 * 0.86).toFloat()
            val innerRadius = (size.width / 2 * 0.8).toFloat()

            val hourCircleRadius = (size.width / 2 * 0.3).toFloat()
            val minuteCircleRadius = (size.width / 2 * 0.6).toFloat()
            val secondCircleRadius = (size.width / 2 * 0.65).toFloat()

            val centerOutRadius = 6.dp.toPx()
            val centerRadius = 4.dp.toPx()

            for (index in 0..330 step 30) {
                val indexDegrees = index * PI / 180

                val isTarget = index % 90 == 0
                if (isTarget) {
                    drawCircle(
                        color = contentColor,
                        radius = boldStrokeLength / 2f,
                        center = center + Offset(
                            (innerRadius * cos(indexDegrees)).toFloat(),
                            (innerRadius * sin(indexDegrees)).toFloat(),
                        ),
                    )
                    drawLine(
                        contentColor,
                        start = center + Offset(
                            (targetOuterRadius * cos(indexDegrees)).toFloat(),
                            (targetOuterRadius * sin(indexDegrees)).toFloat(),
                        ),
                        end = center + Offset(
                            ((innerRadius + boldStrokeLength + 2.dp.toPx()) * cos(indexDegrees)).toFloat(),
                            ((innerRadius + boldStrokeLength + 2.dp.toPx()) * sin(indexDegrees)).toFloat(),
                        ),
                        strokeWidth = boldStrokeLength,
                        cap = StrokeCap.Round,
                    )
                } else {
                    drawLine(
                        contentColor.copy(0.6f),
                        start = center + Offset(
                            (outerRadius * cos(indexDegrees)).toFloat(),
                            (outerRadius * sin(indexDegrees)).toFloat(),
                        ),
                        end = center + Offset(
                            (innerRadius * cos(indexDegrees)).toFloat(),
                            (innerRadius * sin(indexDegrees)).toFloat(),
                        ),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            // 时
            drawTimeLine(
                angle = animateHourAngle.value,
                innerRadius = centerRadius,
                outerRadius = hourCircleRadius,
                strokeWidth = boldStrokeLength,
                color = contentColor,
            )

            // 分
            drawTimeLine(
                angle = animateMinuteAngle.value,
                innerRadius = centerRadius,
                outerRadius = minuteCircleRadius,
                strokeWidth = normalStrokeLength,
                color = contentColor,
            )

            drawCircle(
                color = contentColor,
                center = center,
                radius = centerOutRadius,
            )

            drawCircle(
                color = primaryColor,
                center = center,
                radius = centerRadius,
            )

            // 秒
            drawTimeLine(
                angle = animateSecondAngle.value,
                innerRadius = centerRadius,
                outerRadius = secondCircleRadius,
                strokeWidth = normalStrokeLength,
                color = primaryColor,
            )
        }
    }
}

private fun DrawScope.drawTimeLine(
    angle: Float,
    innerRadius: Float,
    outerRadius: Float,
    strokeWidth: Float,
    color: Color,
) {
    val degrees = angle * PI / 180
    drawLine(
        color,
        start = center + Offset(
            (innerRadius * cos(degrees)).toFloat(),
            (innerRadius * sin(degrees)).toFloat(),
        ),
        end = center + Offset(
            (outerRadius * cos(degrees)).toFloat(),
            (outerRadius * sin(degrees)).toFloat(),
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

private fun Int.toAngle(count: Int): Int {
    return (this.toFloat() / count * 360).roundToInt()
}

@Preview
@Composable
private fun AmiaoRoundClockPreview() {
    ComposeClockTheme {
        ComposeRoundClock(
            clockState = ClockState(
                initialTime = LocalTime(12, 36, 10),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}