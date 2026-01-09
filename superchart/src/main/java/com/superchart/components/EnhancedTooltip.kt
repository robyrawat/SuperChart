package com.superchart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
import com.superchart.formatter.ValueFormatter
import com.superchart.formatter.DefaultFormatter

/**
 * Enhanced tooltip configuration with full customization
 */
data class EnhancedTooltipConfig(
    val backgroundColor: Color = Color(0xFF1E1E1E),
    val textColor: Color = Color.White,
    val borderColor: Color = Color.Transparent,
    val showDatasetLabel: Boolean = true,
    val showEntryLabel: Boolean = true,
    val showValue: Boolean = true,
    val showPercentage: Boolean = false,
    val valueFormatter: ValueFormatter = DefaultFormatter(),
    val fontSize: Float = 14f,
    val cornerRadius: Float = 8f,
    val elevation: Float = 4f,
    val padding: Float = 12f,
    val customContent: (@Composable (ChartEntry, ChartDataset) -> Unit)? = null
)

/**
 * Enhanced animated tooltip with full customization
 */
@Composable
fun EnhancedTooltip(
    visible: Boolean,
    position: Offset,
    entry: ChartEntry,
    dataset: ChartDataset,
    config: EnhancedTooltipConfig = EnhancedTooltipConfig(),
    percentage: Float? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Popup(
            offset = IntOffset(position.x.toInt(), position.y.toInt()),
            properties = PopupProperties(focusable = false)
        ) {
            Box(
                modifier = Modifier
                    .shadow(config.elevation.dp, RoundedCornerShape(config.cornerRadius.dp))
                    .background(config.backgroundColor, RoundedCornerShape(config.cornerRadius.dp))
                    .padding(config.padding.dp)
            ) {
                if (config.customContent != null) {
                    config.customContent.invoke(entry, dataset)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (config.showDatasetLabel && dataset.label != null) {
                            Text(
                                text = dataset.label!!,
                                color = config.textColor,
                                fontSize = (config.fontSize - 2).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (config.showEntryLabel) {
                            Text(
                                text = entry.label,
                                color = config.textColor,
                                fontSize = config.fontSize.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (config.showValue) {
                                Text(
                                    text = config.valueFormatter.format(entry.value),
                                    color = config.textColor,
                                    fontSize = config.fontSize.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (config.showPercentage && percentage != null) {
                                Text(
                                    text = "(${String.format("%.1f", percentage)}%)",
                                    color = config.textColor.copy(alpha = 0.8f),
                                    fontSize = (config.fontSize - 2).sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Value label configuration for showing values on charts
 */
data class ValueLabelStyle(
    val enabled: Boolean = true,
    val position: ValueLabelPosition = ValueLabelPosition.AUTO,
    val color: Color = Color.Black,
    val backgroundColor: Color? = null,
    val fontSize: Float = 12f,
    val fontWeight: FontWeight = FontWeight.Normal,
    val showIcon: Boolean = false,
    val formatter: ValueFormatter = DefaultFormatter(),
    val offset: Offset = Offset(0f, -10f)
)

enum class ValueLabelPosition {
    AUTO,      // Smart positioning based on space
    TOP,       // Always above
    BOTTOM,    // Always below
    CENTER,    // Inside the bar/point
    LEFT,      // To the left
    RIGHT      // To the right
}

/**
 * Percentage display configuration for pie/donut charts
 */
data class PercentageDisplayConfig(
    val enabled: Boolean = true,
    val showOnSlice: Boolean = true,
    val showInLegend: Boolean = true,
    val fontSize: Float = 14f,
    val color: Color = Color.White,
    val decimals: Int = 1,
    val showLabel: Boolean = true
)

