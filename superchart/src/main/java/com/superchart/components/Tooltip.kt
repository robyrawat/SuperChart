package com.superchart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
 * Configuration for chart tooltips.
 */
data class TooltipConfig(
    val enabled: Boolean = true,
    val showDatasetLabel: Boolean = true,
    val showEntryLabel: Boolean = true,
    val showValue: Boolean = true,
    val showPercentage: Boolean = false,
    val valueFormatter: ValueFormatter = DefaultFormatter(),
    val backgroundColor: Color = Color(0xEE000000),
    val textColor: Color = Color.White,
    val borderColor: Color = Color(0xFF666666),
    val customContent: ((ChartEntry, ChartDataset, Float?) -> String)? = null
)

/**
 * Interactive tooltip that appears when a data point is clicked/hovered.
 */
@Composable
fun ChartTooltip(
    visible: Boolean,
    position: Offset,
    entry: ChartEntry,
    dataset: ChartDataset,
    config: TooltipConfig = TooltipConfig(),
    percentage: Float? = null,
    modifier: Modifier = Modifier
) {
    if (!visible || !config.enabled) return

    val density = LocalDensity.current

    Popup(
        offset = IntOffset(
            x = with(density) { position.x.toDp().roundToPx() },
            y = with(density) { (position.y - 80.dp.toPx()).toDp().roundToPx() }
        ),
        properties = PopupProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false
        )
    ) {
        Box(
            modifier = modifier
                .background(
                    color = config.backgroundColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = config.borderColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Column(spacing = 4.dp) {
                if (config.customContent != null) {
                    Text(
                        text = config.customContent.invoke(entry, dataset, percentage),
                        color = config.textColor,
                        fontSize = 12.sp
                    )
                } else {
                    if (config.showDatasetLabel && dataset.label != null) {
                        Text(
                            text = dataset.label,
                            color = config.textColor,
                            fontSize = 13.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }

                    if (config.showEntryLabel) {
                        Text(
                            text = entry.label,
                            color = config.textColor.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }

                    if (config.showValue) {
                        Text(
                            text = "Value: ${config.valueFormatter.format(entry.value)}",
                            color = config.textColor,
                            fontSize = 12.sp
                        )
                    }

                    if (config.showPercentage && percentage != null) {
                        Text(
                            text = "Percentage: ${percentage.toInt()}%",
                            color = config.textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Column(spacing: androidx.compose.ui.unit.Dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

