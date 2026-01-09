package com.superchart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.superchart.data.ChartDataset
import com.superchart.theme.LegendOrientation

@Composable
fun ChartLegend(
    datasets: List<ChartDataset>,
    modifier: Modifier = Modifier,
    orientation: LegendOrientation = LegendOrientation.HORIZONTAL
) {
    val datasetsWithLabels = datasets.filter { !it.label.isNullOrBlank() }
    if (datasetsWithLabels.isEmpty()) return

    when (orientation) {
        LegendOrientation.HORIZONTAL -> {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                datasetsWithLabels.forEach { dataset ->
                    LegendItem(label = dataset.label ?: "", color = dataset.color)
                }
            }
        }
        LegendOrientation.VERTICAL -> {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                datasetsWithLabels.forEach { dataset ->
                    LegendItem(label = dataset.label ?: "", color = dataset.color)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(12.dp).background(color))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
fun EmptyChartState(
    modifier: Modifier = Modifier,
    message: String = "No data available"
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray)
    }
}

