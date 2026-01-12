package com.superchart.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.superchart.components.ChartLegend
import com.superchart.components.EmptyChartState
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
import com.superchart.data.ChartStyle
import com.superchart.theme.ChartTheme
import com.superchart.theme.LegendOrientation
import com.superchart.accessibility.AccessibilityConfig
import com.superchart.accessibility.pieChartAccessibility
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * A pie chart that displays data as circular slices.
 *
 * @param dataset Single dataset to display as slices.
 * @param modifier Modifier for styling and positioning.
 * @param showLegend Whether to display the legend.
 * @param donutMode Whether to render as a donut chart with a center hole.
 * @param donutHoleRatio Size of the center hole (0.0 - 0.9) when in donut mode.
 * @param showLabels Whether to display percentage labels on slices.
 * @param animationDurationMs Duration of entry animation in milliseconds.
 * @param accessibilityConfig Accessibility settings for screen readers.
 * @param onSliceClick Callback invoked when a slice is clicked. Parameters: (entryIndex, entry).
 */
@Composable
fun PieChart(
    dataset: ChartDataset,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    donutMode: Boolean = false,
    donutHoleRatio: Float = 0.5f,
    showLabels: Boolean = true,
    animationDurationMs: Int = ChartTheme.animationConfig.durationMs,
    accessibilityConfig: AccessibilityConfig = AccessibilityConfig(),
    onSliceClick: ((entryIndex: Int, entry: ChartEntry) -> Unit)? = null
) {
    if (dataset.entries.isEmpty()) {
        EmptyChartState(modifier = modifier)
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(dataset) {
        animationProgress = 0f
        selectedIndex = -1
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDurationMs)
        ) { value, _ ->
            animationProgress = value
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .pieChartAccessibility(
                    dataset = dataset,
                    donutMode = donutMode,
                    config = accessibilityConfig,
                    onNavigate = if (onSliceClick != null) {
                        { entry -> onSliceClick(0, entry) }
                    } else null
                )
        ) {
            PieChartCanvas(
                dataset = dataset,
                animationProgress = animationProgress,
                donutMode = donutMode,
                donutHoleRatio = donutHoleRatio,
                showLabels = showLabels,
                selectedIndex = selectedIndex,
                onSliceClick = { index, entry ->
                    selectedIndex = index
                    onSliceClick?.invoke(index, entry)
                }
            )
        }

        if (showLegend) {
            ChartLegend(
                datasets = listOf(dataset),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                orientation = LegendOrientation.HORIZONTAL
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PieChartCanvas(
    dataset: ChartDataset,
    animationProgress: Float,
    donutMode: Boolean,
    donutHoleRatio: Float,
    showLabels: Boolean,
    selectedIndex: Int,
    onSliceClick: ((Int, ChartEntry) -> Unit)?
) {
    val total = remember(dataset) {
        dataset.entries.sumOf { it.value.toDouble() }.toFloat()
    }

    val pieStyle = (dataset.style as? ChartStyle.Pie) ?: ChartStyle.Pie(
        donutMode = donutMode,
        donutHoleRatio = donutHoleRatio,
        showLabels = showLabels
    )

    val sliceData = remember(dataset, showLabels) { mutableStateListOf<SliceData>() }

    key(dataset, showLabels, animationProgress) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dataset, onSliceClick, animationProgress) {
                    if (onSliceClick != null) {
                        detectTapGestures { offset ->
                            val clicked = findClickedSlice(offset, sliceData)
                            clicked?.let { (index, entry) ->
                                // Validate index bounds before callback
                                if (index >= 0 && index < dataset.entries.size) {
                                    onSliceClick(index, entry)
                                }
                            }
                        }
                    }
                }
        ) {
            val chartSize = minOf(size.width, size.height)
            val radius = chartSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            sliceData.clear()
            var startAngle = -90f

            dataset.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f * animationProgress

            if (sweepAngle > 0) {
                sliceData.add(
                    SliceData(
                        index = index,
                        entry = entry,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        center = center,
                        radius = radius
                    )
                )

                val color = dataset.getColorForEntry(entry)
                val isSelected = index == selectedIndex
                val sliceRadius = if (isSelected) radius * 1.05f else radius

                if (pieStyle.donutMode) {
                    val strokeWidth = sliceRadius * (1f - pieStyle.donutHoleRatio)
                    val arcRadius = sliceRadius - (strokeWidth / 2f)

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - arcRadius,
                            center.y - arcRadius
                        ),
                        size = Size(arcRadius * 2f, arcRadius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    // Highlight selected slice
                    if (isSelected) {
                        drawArc(
                            color = androidx.compose.ui.graphics.Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - arcRadius,
                                center.y - arcRadius
                            ),
                            size = Size(arcRadius * 2f, arcRadius * 2f),
                            style = Stroke(width = 4f, cap = StrokeCap.Butt)
                        )
                    }
                } else {
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(
                            center.x - sliceRadius,
                            center.y - sliceRadius
                        ),
                        size = Size(sliceRadius * 2f, sliceRadius * 2f)
                    )

                    // Highlight selected slice with white border
                    if (isSelected) {
                        drawArc(
                            color = androidx.compose.ui.graphics.Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(
                                center.x - sliceRadius,
                                center.y - sliceRadius
                            ),
                            size = Size(sliceRadius * 2f, sliceRadius * 2f),
                            style = Stroke(width = 4f)
                        )
                    }
                }

                startAngle += sweepAngle
            }
        }

        // Draw labels if enabled
        if (showLabels && animationProgress > 0.5f) {
            var labelStartAngle = -90f
            dataset.entries.forEach { entry ->
                val sweepAngle = (entry.value / total) * 360f * animationProgress
                if (sweepAngle > 15f) { // Only show label if slice is big enough
                    val midAngle = Math.toRadians((labelStartAngle + sweepAngle / 2f).toDouble())
                    val labelRadius = radius * 0.7f
                    val labelX = center.x + labelRadius * kotlin.math.cos(midAngle).toFloat()
                    val labelY = center.y + labelRadius * kotlin.math.sin(midAngle).toFloat()

                    val percentage = (entry.value / total * 100).toInt()
                    val labelText = "$percentage%"

                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = androidx.compose.ui.graphics.Color.White.toArgb()
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            isFakeBoldText = true
                            setShadowLayer(4f, 0f, 0f, androidx.compose.ui.graphics.Color.Black.toArgb())
                        }

                        canvas.nativeCanvas.drawText(
                            labelText,
                            labelX,
                            labelY + 12f,
                            paint
                        )
                    }
                }
                labelStartAngle += sweepAngle
            }
        }
    }
    }
}

private data class SliceData(
    val index: Int,
    val entry: ChartEntry,
    val startAngle: Float,
    val sweepAngle: Float,
    val center: Offset,
    val radius: Float
)

private fun findClickedSlice(
    tapOffset: Offset,
    sliceData: List<SliceData>
): Pair<Int, ChartEntry>? {
    sliceData.forEach { slice ->
        val center = slice.center
        val dx = tapOffset.x - center.x
        val dy = tapOffset.y - center.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance <= slice.radius) {
            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
            angle = (angle + 90f + 360f) % 360f

            val endAngle = (slice.startAngle + slice.sweepAngle + 90f + 360f) % 360f
            val startAngle = (slice.startAngle + 90f + 360f) % 360f

            val isInSlice = if (startAngle <= endAngle) {
                angle >= startAngle && angle <= endAngle
            } else {
                angle >= startAngle || angle <= endAngle
            }

            if (isInSlice) {
                return Pair(slice.index, slice.entry)
            }
        }
    }
    return null
}

