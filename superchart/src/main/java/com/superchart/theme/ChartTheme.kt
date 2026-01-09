package com.superchart.theme

import androidx.compose.ui.graphics.Color

data class ChartColors(
    val primary: Color = Color(0xFF2196F3),
    val secondary: Color = Color(0xFF4CAF50),
    val tertiary: Color = Color(0xFFF44336),
    val background: Color = Color.White,
    val grid: Color = Color(0xFFE0E0E0),
    val text: Color = Color(0xFF212121),
    val axis: Color = Color(0xFF757575),
    val tooltip: Color = Color(0xDD000000)
)

data class AxisConfig(
    val showXAxis: Boolean = true,
    val showYAxis: Boolean = true,
    val showGridLines: Boolean = true,
    val showXAxisLabels: Boolean = true,
    val showYAxisLabels: Boolean = true,
    val xAxisLabels: List<String> = emptyList(),
    val yAxisSteps: Int = 5,
    val axisColor: Color = Color(0xFF757575),
    val gridColor: Color = Color(0xFFE0E0E0),
    val labelColor: Color = Color(0xFF212121)
)

data class AnimationConfig(
    val enabled: Boolean = true,
    val durationMs: Int = 600,
    val delayMs: Int = 0,
    val staggerDelayMs: Int = 50
)

object ChartTheme {
    val colors = ChartColors()
    val axisConfig = AxisConfig()
    val animationConfig = AnimationConfig()

    val defaultPalette = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFF44336),
        Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFFEB3B), Color(0xFF795548), Color(0xFF607D8B),
        Color(0xFFE91E63)
    )

    fun getColorForIndex(index: Int): Color {
        return defaultPalette[index % defaultPalette.size]
    }
}

enum class LegendOrientation {
    HORIZONTAL,
    VERTICAL
}

