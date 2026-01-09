package com.superchart.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.superchart.accessibility.barChartAccessibility

/**
 * A bar chart that displays data as vertical bars.
 *
 * @param datasets List of datasets to display. Multiple datasets can be grouped or stacked.
 * @param modifier Modifier for styling and positioning.
 * @param showLegend Whether to display the legend.
 * @param grouped Whether bars should be grouped (true) or stacked (false).
 * @param maxValue Maximum Y-axis value. If null, calculated from data.
 * @param minValue Minimum Y-axis value.
 * @param axisConfig Configuration for axes and grid.
 * @param animationDurationMs Duration of entry animation in milliseconds.
 * @param accessibilityConfig Accessibility settings for screen readers.
 * @param showValueLabels Whether to display value labels on top of bars.
 * @param onBarClick Callback invoked when a bar is clicked. Parameters: (datasetIndex, entryIndex, entry).
 */
@Composable
fun BarChart(
    datasets: List<ChartDataset>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    grouped: Boolean = true,
    maxValue: Float? = null,
    minValue: Float = 0f,
    axisConfig: AxisConfig = ChartTheme.axisConfig,
    animationDurationMs: Int = ChartTheme.animationConfig.durationMs,
    accessibilityConfig: AccessibilityConfig = AccessibilityConfig(),
    showValueLabels: Boolean = false,
    onBarClick: ((datasetIndex: Int, entryIndex: Int, entry: ChartEntry) -> Unit)? = null
) {
    if (datasets.isEmpty() || datasets.all { it.entries.isEmpty() }) {
        EmptyChartState(modifier = modifier)
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(datasets) {
        animationProgress = 0f
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
                .barChartAccessibility(
                    datasets = datasets,
                    grouped = grouped,
                    config = accessibilityConfig,
                    onNavigate = if (onBarClick != null) {
                        { entry -> onBarClick(0, 0, entry) }
                    } else null
                )
        ) {
            BarChartCanvas(
                datasets = datasets,
                animationProgress = animationProgress,
                grouped = grouped,
                maxValue = maxValue,
                minValue = minValue,
                axisConfig = axisConfig,
                showValueLabels = showValueLabels,
                onBarClick = onBarClick
            )
        }

        if (showLegend) {
            ChartLegend(
                datasets = datasets,
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
private fun BarChartCanvas(
    datasets: List<ChartDataset>,
    animationProgress: Float,
    grouped: Boolean,
    maxValue: Float?,
    minValue: Float,
    axisConfig: AxisConfig,
    showValueLabels: Boolean,
    onBarClick: ((datasetIndex: Int, entryIndex: Int, entry: ChartEntry) -> Unit)?
) {
    val calculatedMax = remember(datasets, grouped) {
        if (grouped) {
            maxValue ?: datasets.flatMap { it.entries }.maxOfOrNull { it.value } ?: 1f
        } else {
            maxValue ?: datasets.firstOrNull()?.entries?.indices?.maxOfOrNull { index ->
                datasets.sumOf { dataset ->
                    dataset.entries.getOrNull(index)?.value?.toDouble() ?: 0.0
                }.toFloat()
            } ?: 1f
        }
    }

    val calculatedMin = remember(datasets, minValue) {
        minValue.coerceAtMost(0f)
    }
    val valueRange = calculatedMax - calculatedMin

    val barPositions = remember { mutableStateMapOf<String, BarData>() }
    val maxEntries = remember(datasets) {
        datasets.maxOfOrNull { it.entries.size } ?: 0
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(datasets, onBarClick) {
                if (onBarClick != null) {
                    detectTapGestures { offset ->
                        val clickedBar = findClickedBar(offset, barPositions)
                        clickedBar?.let { (dsIndex, entryIndex, entry) ->
                            onBarClick(dsIndex, entryIndex, entry)
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

        barPositions.clear()

        if (maxEntries == 0) return@Canvas

        if (grouped) {
            // Proper grouped bar positioning
            val groupWidth = chartWidth / maxEntries
            val numDatasets = datasets.size
            val barStyle = (datasets.firstOrNull()?.style as? ChartStyle.Bar) ?: ChartStyle.Bar()
            val barSpacing = barStyle.spacing
            val availableWidth = groupWidth - (barSpacing * 2)
            val barWidth = (availableWidth / numDatasets).coerceAtLeast(1f)

            datasets.forEachIndexed { datasetIndex, dataset ->
                dataset.entries.forEachIndexed { entryIndex, entry ->
                    val groupStartX = entryIndex * groupWidth
                    val barX = groupStartX + barSpacing + (datasetIndex * barWidth)

                    val animatedValue = entry.value * animationProgress
                    val normalizedValue = ((animatedValue - calculatedMin) / valueRange).coerceIn(0f, 1f)
                    val barHeight = normalizedValue * chartHeight
                    val barY = chartHeight - barHeight

                    barPositions["$datasetIndex-$entryIndex"] = BarData(
                        datasetIndex = datasetIndex,
                        entryIndex = entryIndex,
                        entry = entry,
                        x = barX,
                        y = barY,
                        width = barWidth,
                        height = barHeight
                    )

                    drawRoundRect(
                        color = dataset.getColorForEntry(entry),
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barStyle.cornerRadius, barStyle.cornerRadius)
                    )

                    // Draw value label on top of bar
                    if (showValueLabels && barHeight > 20f) {
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = Color.White.toArgb()
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = true
                            }

                            // Format value (simple integer format)
                            val valueText = if (entry.value >= 1000) {
                                String.format("%.1fK", entry.value / 1000)
                            } else {
                                entry.value.toInt().toString()
                            }

                            // Draw black background for better visibility
                            val textBounds = android.graphics.Rect()
                            paint.getTextBounds(valueText, 0, valueText.length, textBounds)
                            val bgPaint = android.graphics.Paint().apply {
                                color = Color.Black.copy(alpha = 0.7f).toArgb()
                                style = android.graphics.Paint.Style.FILL
                            }
                            canvas.nativeCanvas.drawRoundRect(
                                barX + barWidth / 2 - textBounds.width() / 2 - 8f,
                                barY - 12f,
                                barX + barWidth / 2 + textBounds.width() / 2 + 8f,
                                barY + textBounds.height() + 4f,
                                8f, 8f,
                                bgPaint
                            )

                            // Draw text
                            canvas.nativeCanvas.drawText(
                                valueText,
                                barX + barWidth / 2,
                                barY + textBounds.height() / 2,
                                paint
                            )
                        }
                    }
                }
            }
        } else {
            // Stacked bars
            val barStyle = (datasets.firstOrNull()?.style as? ChartStyle.Bar) ?: ChartStyle.Bar()
            val groupWidth = chartWidth / maxEntries
            val barSpacing = barStyle.spacing
            val barWidth = (groupWidth - (barSpacing * 2)).coerceAtLeast(1f)

            for (entryIndex in 0 until maxEntries) {
                var stackY = chartHeight

                datasets.forEachIndexed { datasetIndex, dataset ->
                    val entry = dataset.entries.getOrNull(entryIndex) ?: return@forEachIndexed

                    val groupStartX = entryIndex * groupWidth
                    val barX = groupStartX + barSpacing

                    val animatedValue = entry.value * animationProgress
                    val normalizedValue = ((animatedValue - calculatedMin) / valueRange).coerceIn(0f, 1f)
                    val barHeight = normalizedValue * chartHeight
                    val barY = stackY - barHeight

                    barPositions["$datasetIndex-$entryIndex"] = BarData(
                        datasetIndex = datasetIndex,
                        entryIndex = entryIndex,
                        entry = entry,
                        x = barX,
                        y = barY,
                        width = barWidth,
                        height = barHeight
                    )

                    drawRoundRect(
                        color = dataset.getColorForEntry(entry),
                        topLeft = Offset(barX, barY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barStyle.cornerRadius, barStyle.cornerRadius)
                    )

                    // Draw value label for stacked bars
                    if (showValueLabels && barHeight > 20f) {
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = Color.White.toArgb()
                                textSize = 26f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = true
                            }

                            val valueText = if (entry.value >= 1000) {
                                String.format("%.1fK", entry.value / 1000)
                            } else {
                                entry.value.toInt().toString()
                            }

                            canvas.nativeCanvas.drawText(
                                valueText,
                                barX + barWidth / 2,
                                barY + barHeight / 2 + 10f,
                                paint
                            )
                        }
                    }

                    stackY = barY
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

private data class BarData(
    val datasetIndex: Int,
    val entryIndex: Int,
    val entry: ChartEntry,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

private fun findClickedBar(
    tapOffset: Offset,
    barPositions: Map<String, BarData>
): Triple<Int, Int, ChartEntry>? {
    barPositions.values.forEach { barData ->
        if (tapOffset.x >= barData.x &&
            tapOffset.x <= barData.x + barData.width &&
            tapOffset.y >= barData.y &&
            tapOffset.y <= barData.y + barData.height
        ) {
            return Triple(barData.datasetIndex, barData.entryIndex, barData.entry)
        }
    }
    return null
}

