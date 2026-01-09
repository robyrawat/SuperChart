package com.superchart.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.superchart.theme.AxisConfig
import com.superchart.theme.ChartTheme
import com.superchart.theme.LegendOrientation
import com.superchart.accessibility.AccessibilityConfig
import com.superchart.accessibility.lineChartAccessibility
import kotlin.math.abs

/**
 * A line chart that displays data as connected points on a 2D plane.
 *
 * @param datasets List of datasets to display. Each dataset will be rendered as a separate line.
 * @param modifier Modifier for styling and positioning.
 * @param showLegend Whether to display the legend.
 * @param showValueLabels Whether to display value labels on data points.
 * @param maxValue Maximum Y-axis value. If null, calculated from data.
 * @param minValue Minimum Y-axis value.
 * @param axisConfig Configuration for axes and grid.
 * @param animationDurationMs Duration of entry animation in milliseconds.
 * @param accessibilityConfig Accessibility settings for screen readers.
 * @param onPointClick Callback invoked when a data point is clicked. Parameters: (datasetIndex, entryIndex, entry).
 */
@Composable
fun LineChart(
    datasets: List<ChartDataset>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    showValueLabels: Boolean = false,
    maxValue: Float? = null,
    minValue: Float = 0f,
    axisConfig: AxisConfig = ChartTheme.axisConfig,
    animationDurationMs: Int = ChartTheme.animationConfig.durationMs,
    accessibilityConfig: AccessibilityConfig = AccessibilityConfig(),
    onPointClick: ((datasetIndex: Int, entryIndex: Int, entry: ChartEntry) -> Unit)? = null
) {
    // Create immutable snapshot to prevent ConcurrentModificationException
    val stableDatasets = remember(datasets) { datasets.toList() }

    if (stableDatasets.isEmpty() || stableDatasets.all { it.entries.isEmpty() }) {
        EmptyChartState(modifier = modifier)
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    var selectedPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) } // datasetIndex, entryIndex

    LaunchedEffect(stableDatasets) {
        animationProgress = 0f
        selectedPoint = null
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
                .lineChartAccessibility(
                    datasets = stableDatasets,
                    config = accessibilityConfig,
                    onNavigate = if (onPointClick != null) {
                        { entry -> onPointClick(0, 0, entry) }
                    } else null
                )
        ) {
            LineChartCanvas(
                datasets = stableDatasets,
                animationProgress = animationProgress,
                maxValue = maxValue,
                minValue = minValue,
                axisConfig = axisConfig,
                showValueLabels = showValueLabels,
                selectedPoint = selectedPoint,
                onPointClick = { dsIdx, entryIdx, entry ->
                    selectedPoint = Pair(dsIdx, entryIdx)
                    onPointClick?.invoke(dsIdx, entryIdx, entry)
                }
            )
        }

        if (showLegend) {
            ChartLegend(
                datasets = stableDatasets,
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
private fun LineChartCanvas(
    datasets: List<ChartDataset>,
    animationProgress: Float,
    maxValue: Float?,
    minValue: Float,
    axisConfig: AxisConfig,
    showValueLabels: Boolean,
    selectedPoint: Pair<Int, Int>?,
    onPointClick: ((Int, Int, ChartEntry) -> Unit)?
) {
    val calculatedMax = remember(datasets) {
        maxValue ?: datasets.flatMap { it.entries }.maxOfOrNull { it.value } ?: 1f
    }
    val calculatedMin = remember(datasets, minValue) {
        minValue.coerceAtMost(datasets.flatMap { it.entries }.minOfOrNull { it.value } ?: 0f)
    }
    val valueRange = calculatedMax - calculatedMin

    val pointPositions = remember { mutableStateMapOf<String, PointData>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(datasets, onPointClick) {
                if (onPointClick != null) {
                    detectTapGestures { offset ->
                        val nearestPoint = findNearestPoint(offset, pointPositions)
                        nearestPoint?.let { (dsIndex, entryIndex, entry) ->
                            onPointClick(dsIndex, entryIndex, entry)
                        }
                    }
                }
            }
    ) {
        val chartWidth = size.width
        val chartHeight = size.height

        if (axisConfig.showGridLines) {
            val gridSteps = axisConfig.yAxisSteps
            for (i in 0..gridSteps) {
                val y = chartHeight - (chartHeight * i / gridSteps)
                drawLine(
                    color = axisConfig.gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }
        }

        pointPositions.clear()
        datasets.forEachIndexed { datasetIndex, dataset ->
            if (dataset.entries.isEmpty()) return@forEachIndexed

            val lineStyle = (dataset.style as? ChartStyle.Line) ?: ChartStyle.Line()
            val path = Path()

            dataset.entries.forEachIndexed { index, entry ->
                // Handle single data point case
                val x = if (dataset.entries.size <= 1) {
                    chartWidth / 2f
                } else {
                    index * (chartWidth / (dataset.entries.size - 1))
                }

                val animatedValue = entry.value * animationProgress
                val normalizedValue = ((animatedValue - calculatedMin) / valueRange).coerceIn(0f, 1f)
                val y = chartHeight - (normalizedValue * chartHeight)

                pointPositions["$datasetIndex-$index"] = PointData(
                    datasetIndex = datasetIndex,
                    entryIndex = index,
                    entry = entry,
                    position = Offset(x, y)
                )

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            // Use Stroke style instead of fill
            drawPath(
                path = path,
                color = dataset.color,
                style = Stroke(
                    width = lineStyle.width,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            if (lineStyle.showPoints) {
                dataset.entries.forEachIndexed { index, entry ->
                    val pointData = pointPositions["$datasetIndex-$index"]
                    pointData?.let {
                        val isSelected = selectedPoint?.first == datasetIndex && selectedPoint?.second == index
                        val pointRadius = if (isSelected) lineStyle.pointRadius * 1.5f else lineStyle.pointRadius

                        // Draw main point
                        drawCircle(
                            color = dataset.getColorForEntry(entry),
                            radius = pointRadius * animationProgress,
                            center = it.position
                        )

                        // Draw white border for selected point
                        if (isSelected) {
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color.White,
                                radius = pointRadius * animationProgress,
                                center = it.position,
                                style = Stroke(width = 3f)
                            )
                        }

                        // Draw value label if enabled
                        if (showValueLabels && animationProgress > 0.5f) {
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    color = androidx.compose.ui.graphics.Color.White.toArgb()
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    isFakeBoldText = true
                                }

                                val valueText = if (entry.value >= 1000) {
                                    String.format("%.1fK", entry.value / 1000)
                                } else {
                                    entry.value.toInt().toString()
                                }

                                // Draw background
                                val textBounds = android.graphics.Rect()
                                paint.getTextBounds(valueText, 0, valueText.length, textBounds)
                                val bgPaint = android.graphics.Paint().apply {
                                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f).toArgb()
                                    style = android.graphics.Paint.Style.FILL
                                }
                                canvas.nativeCanvas.drawRoundRect(
                                    it.position.x - textBounds.width() / 2 - 8f,
                                    it.position.y - textBounds.height() - 20f,
                                    it.position.x + textBounds.width() / 2 + 8f,
                                    it.position.y - 8f,
                                    8f, 8f,
                                    bgPaint
                                )

                                // Draw text
                                canvas.nativeCanvas.drawText(
                                    valueText,
                                    it.position.x,
                                    it.position.y - 14f,
                                    paint
                                )
                            }
                        }
                    }
                }
            }
        }

        if (axisConfig.showXAxis) {
            drawLine(
                color = axisConfig.axisColor,
                start = Offset(0f, chartHeight),
                end = Offset(chartWidth, chartHeight),
                strokeWidth = 2f
            )
        }

        if (axisConfig.showYAxis) {
            drawLine(
                color = axisConfig.axisColor,
                start = Offset(0f, 0f),
                end = Offset(0f, chartHeight),
                strokeWidth = 2f
            )
        }
    }
}

private data class PointData(
    val datasetIndex: Int,
    val entryIndex: Int,
    val entry: ChartEntry,
    val position: Offset
)

private fun findNearestPoint(
    tapOffset: Offset,
    pointPositions: Map<String, PointData>,
    threshold: Float = 50f
): Triple<Int, Int, ChartEntry>? {
    var nearest: Triple<Int, Int, ChartEntry>? = null
    var minDistance = threshold

    pointPositions.values.forEach { pointData ->
        val distance = abs(tapOffset.x - pointData.position.x) + abs(tapOffset.y - pointData.position.y)
        if (distance < minDistance) {
            minDistance = distance
            nearest = Triple(pointData.datasetIndex, pointData.entryIndex, pointData.entry)
        }
    }

    return nearest
}

