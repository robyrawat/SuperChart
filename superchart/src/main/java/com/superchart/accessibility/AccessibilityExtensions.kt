package com.superchart.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
// Import ChartSemantics extension properties
import com.superchart.accessibility.ChartSemantics.chartType
import com.superchart.accessibility.ChartSemantics.dataPointCount
import com.superchart.accessibility.ChartSemantics.isInteractive
import com.superchart.accessibility.ChartSemantics.minValue
import com.superchart.accessibility.ChartSemantics.maxValue
import com.superchart.accessibility.ChartSemantics.averageValue
import com.superchart.accessibility.ChartSemantics.detailedDataDescription
import com.superchart.accessibility.ChartSemantics.dataTrend

/**
 * Extension functions to add accessibility to charts.
 */

/**
 * Add accessibility semantics to LineChart.
 */
fun Modifier.lineChartAccessibility(
    datasets: List<ChartDataset>,
    config: AccessibilityConfig = AccessibilityConfig(),
    onNavigate: ((ChartEntry) -> Unit)? = null
): Modifier {
    if (!config.enabled) return this

    val generator = AccessibilityDescriptionGenerator(config)
    val description = generator.generateChartDescription("Line", datasets)

    return this.semantics {
        contentDescription = description

        // Custom semantics using extension properties
        chartType = "Line Chart"
        dataPointCount = datasets.sumOf { it.entries.size }
        isInteractive = onNavigate != null

        // Add statistics
        val allValues = datasets.flatMap { it.entries.map { it.value } }
        if (allValues.isNotEmpty()) {
            minValue = config.valueFormatter.format(allValues.minOrNull() ?: 0f)
            maxValue = config.valueFormatter.format(allValues.maxOrNull() ?: 0f)
            averageValue = config.valueFormatter.format(allValues.average().toFloat())
        }

        // Add detailed description
        if (config.verbosity == AccessibilityVerbosity.DETAILED) {
            detailedDataDescription = generator.generateDataPointsDescription(datasets.firstOrNull() ?: return@semantics)
            dataTrend = generator.generateTrendDescription(datasets)
        }

        // Mark as clickable if interactive
        if (onNavigate != null) {
            onClick {
                // Handled by chart's own click handler
                true
            }
        }
    }
}

/**
 * Add accessibility semantics to BarChart.
 */
fun Modifier.barChartAccessibility(
    datasets: List<ChartDataset>,
    grouped: Boolean,
    config: AccessibilityConfig = AccessibilityConfig(),
    onNavigate: ((ChartEntry) -> Unit)? = null
): Modifier {
    if (!config.enabled) return this

    val generator = AccessibilityDescriptionGenerator(config)
    val chartType = if (grouped) "Grouped Bar" else "Stacked Bar"
    val description = generator.generateChartDescription(chartType, datasets)

    return this.semantics {
        contentDescription = description

        this.chartType = "$chartType Chart"
        dataPointCount = datasets.sumOf { it.entries.size }
        isInteractive = onNavigate != null

        val allValues = datasets.flatMap { it.entries.map { it.value } }
        if (allValues.isNotEmpty()) {
            minValue = config.valueFormatter.format(allValues.minOrNull() ?: 0f)
            maxValue = config.valueFormatter.format(allValues.maxOrNull() ?: 0f)
            averageValue = config.valueFormatter.format(allValues.average().toFloat())
        }

        if (onNavigate != null) {
            onClick {
                true
            }
        }
    }
}

/**
 * Add accessibility semantics to PieChart.
 */
fun Modifier.pieChartAccessibility(
    dataset: ChartDataset,
    donutMode: Boolean,
    config: AccessibilityConfig = AccessibilityConfig(),
    onNavigate: ((ChartEntry) -> Unit)? = null
): Modifier {
    if (!config.enabled) return this

    val generator = AccessibilityDescriptionGenerator(config)
    val chartType = if (donutMode) "Donut" else "Pie"

    // Calculate percentages
    val total = dataset.entries.sumOf { it.value.toDouble() }.toFloat()
    val descriptions = dataset.entries.map { entry ->
        val percentage = (entry.value / total * 100)
        generator.generatePieSliceDescription(entry, percentage, dataset)
    }

    val description = buildString {
        append("$chartType chart with ${dataset.entries.size} slices. ")
        descriptions.forEachIndexed { index, desc ->
            append(desc)
            if (index < descriptions.size - 1) {
                append(". ")
            }
        }
    }

    return this.semantics {
        contentDescription = description

        this.chartType = "$chartType Chart"
        dataPointCount = dataset.entries.size
        isInteractive = onNavigate != null

        if (onNavigate != null) {
            onClick {
                true
            }
        }
    }
}

/**
 * Add accessibility semantics to individual data points.
 */
fun Modifier.dataPointAccessibility(
    entry: ChartEntry,
    dataset: ChartDataset,
    config: AccessibilityConfig = AccessibilityConfig(),
    onClick: () -> Unit
): Modifier {
    if (!config.enabled) return this

    val generator = AccessibilityDescriptionGenerator(config)
    val description = generator.generateDataPointDescription(entry, dataset)

    return this.semantics {
        contentDescription = description
        stateDescription = "Data point"

        onClick(label = AccessibilityActions.TAP_TO_HEAR_VALUE) {
            onClick()
            true
        }
    }
}

/**
 * Add accessibility semantics to chart legend.
 */
fun Modifier.legendAccessibility(
    datasets: List<ChartDataset>,
    config: AccessibilityConfig = AccessibilityConfig()
): Modifier {
    if (!config.enabled) return this

    val description = buildString {
        append("Chart legend with ${datasets.size} items. ")
        datasets.forEach { dataset ->
            if (dataset.label != null) {
                append("${dataset.label}, ")
            }
        }
    }

    return this.semantics {
        contentDescription = description
        heading()
    }
}

/**
 * Add accessibility semantics to export button.
 */
fun Modifier.exportButtonAccessibility(
    chartType: String,
    config: AccessibilityConfig = AccessibilityConfig()
): Modifier {
    if (!config.enabled) return this

    return this.semantics {
        contentDescription = "Export $chartType chart as PNG image"
        stateDescription = "Button"

        onClick(label = AccessibilityActions.TAP_TO_EXPORT) {
            // Handled by button's own click
            true
        }
    }
}

/**
 * Add accessibility semantics to tooltip.
 */
fun Modifier.tooltipAccessibility(
    entry: ChartEntry,
    dataset: ChartDataset,
    config: AccessibilityConfig = AccessibilityConfig()
): Modifier {
    if (!config.enabled) return this

    val generator = AccessibilityDescriptionGenerator(config)
    val description = try {
        generator.generateDataPointDescription(entry, dataset)
    } catch (e: Exception) {
        // Graceful fallback if description generation fails
        "Data point: ${entry.label}"
    }

    return this.semantics {
        liveRegion = LiveRegionMode.Polite
        contentDescription = "Data tooltip: $description"
    }
}

/**
 * Mark chart as navigable for TalkBack explore by touch.
 */
fun Modifier.chartNavigable(
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this

    return this.semantics {
        traversalIndex = 0f

        // Custom action for statistics
        customActions = listOf(
            CustomAccessibilityAction(
                label = "Hear statistics",
                action = { true }
            ),
            CustomAccessibilityAction(
                label = "Hear data points",
                action = { true }
            )
        )
    }
}

