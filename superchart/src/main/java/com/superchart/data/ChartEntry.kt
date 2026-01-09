package com.superchart.data

import androidx.compose.ui.graphics.Color

/**
 * Represents a single data point in a chart.
 */
data class ChartEntry(
    val label: String,
    val value: Float,
    val color: Color? = null
) {
    init {
        require(value.isFinite()) { "Chart value must be finite (not NaN or Infinity)" }
    }
}

/**
 * Represents a dataset containing multiple chart entries.
 */
data class ChartDataset(
    val entries: List<ChartEntry>,
    val label: String? = null,
    val color: Color = Color.Blue,
    val style: ChartStyle? = null
) {
    fun getColorForEntry(entry: ChartEntry): Color = entry.color ?: color

    fun getColorAtIndex(index: Int): Color {
        if (index < 0 || index >= entries.size) return color
        return entries[index].color ?: color
    }

    fun maxValue(): Float = entries.maxOfOrNull { it.value } ?: 0f
    fun minValue(): Float = entries.minOfOrNull { it.value } ?: 0f
}

/**
 * Styling options for different chart types.
 */
sealed class ChartStyle {
    data class Line(
        val width: Float = 4f,
        val dashed: Boolean = false,
        val smooth: Boolean = false,
        val showPoints: Boolean = false,
        val pointRadius: Float = 6f
    ) : ChartStyle()

    data class Bar(
        val width: Float = 30f,
        val cornerRadius: Float = 0f,
        val spacing: Float = 8f
    ) : ChartStyle()

    data class Pie(
        val donutMode: Boolean = false,
        val donutHoleRatio: Float = 0.5f,
        val showLabels: Boolean = true,
        val sliceSpacing: Float = 2f
    ) : ChartStyle()
}

