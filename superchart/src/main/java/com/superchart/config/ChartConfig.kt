package com.superchart.config

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.superchart.formatter.ValueFormatter
import com.superchart.formatter.DefaultFormatter

/**
 * Configuration for showing value labels on charts
 * This allows displaying values directly on bars, points, or slices
 */
data class ValueLabelConfig(
    val enabled: Boolean = true,
    val position: ValueLabelPosition = ValueLabelPosition.AUTO,
    val color: Color = Color.Black,
    val backgroundColor: Color? = Color.White.copy(alpha = 0.8f),
    val fontSize: Float = 12f,
    val fontWeight: FontWeight = FontWeight.Bold,
    val formatter: ValueFormatter = DefaultFormatter(),
    val offsetX: Float = 0f,
    val offsetY: Float = -10f,
    val showBackground: Boolean = true,
    val backgroundPadding: Float = 4f,
    val cornerRadius: Float = 4f
)

/**
 * Position options for value labels
 */
enum class ValueLabelPosition {
    AUTO,       // Smart positioning based on available space
    TOP,        // Always above the bar/point
    BOTTOM,     // Always below the bar/point
    CENTER,     // Inside the bar/point
    LEFT,       // To the left
    RIGHT,      // To the right
    INSIDE_TOP, // Inside bar, at top
    INSIDE_BOTTOM // Inside bar, at bottom
}

/**
 * Dynamic style configuration - allows changing colors, sizes via callbacks
 */
data class DynamicStyleConfig(
    // Color customization callback
    val colorProvider: ((datasetIndex: Int, entryIndex: Int, value: Float) -> Color)? = null,

    // Size customization callback
    val sizeProvider: ((datasetIndex: Int, entryIndex: Int, value: Float) -> Float)? = null,

    // Label customization callback
    val labelProvider: ((datasetIndex: Int, entryIndex: Int, value: Float) -> String)? = null,

    // Alpha (transparency) callback
    val alphaProvider: ((datasetIndex: Int, entryIndex: Int, value: Float) -> Float)? = null
)

/**
 * Configuration for chart interactions
 */
data class InteractionConfig(
    val highlightOnClick: Boolean = true,
    val highlightColor: Color = Color.Yellow.copy(alpha = 0.3f),
    val highlightScale: Float = 1.1f,
    val animateSelection: Boolean = true,
    val selectionDuration: Int = 300,
    val enableHapticFeedback: Boolean = true
)

/**
 * Complete chart configuration combining all options
 */
data class ChartConfig(
    val valueLabelConfig: ValueLabelConfig = ValueLabelConfig(),
    val dynamicStyleConfig: DynamicStyleConfig = DynamicStyleConfig(),
    val interactionConfig: InteractionConfig = InteractionConfig(),
    val showGrid: Boolean = true,
    val showAxis: Boolean = true,
    val animationEnabled: Boolean = true,
    val animationDuration: Int = 600
)

