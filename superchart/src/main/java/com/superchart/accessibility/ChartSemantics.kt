package com.superchart.accessibility

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

/**
 * Custom semantics properties for chart accessibility.
 */
object ChartSemantics {

    /**
     * Describes the type of chart (Line, Bar, Pie, etc.)
     */
    val ChartType = SemanticsPropertyKey<String>("ChartType")
    var SemanticsPropertyReceiver.chartType by ChartType

    /**
     * Provides a summary of the chart data.
     */
    val ChartSummary = SemanticsPropertyKey<String>("ChartSummary")
    var SemanticsPropertyReceiver.chartSummary by ChartSummary

    /**
     * Total number of data points in the chart.
     */
    val DataPointCount = SemanticsPropertyKey<Int>("DataPointCount")
    var SemanticsPropertyReceiver.dataPointCount by DataPointCount

    /**
     * Describes the trend of the data (increasing, decreasing, stable).
     */
    val DataTrend = SemanticsPropertyKey<String>("DataTrend")
    var SemanticsPropertyReceiver.dataTrend by DataTrend

    /**
     * Minimum value in the dataset.
     */
    val MinValue = SemanticsPropertyKey<String>("MinValue")
    var SemanticsPropertyReceiver.minValue by MinValue

    /**
     * Maximum value in the dataset.
     */
    val MaxValue = SemanticsPropertyKey<String>("MaxValue")
    var SemanticsPropertyReceiver.maxValue by MaxValue

    /**
     * Average value in the dataset.
     */
    val AverageValue = SemanticsPropertyKey<String>("AverageValue")
    var SemanticsPropertyReceiver.averageValue by AverageValue

    /**
     * Detailed data description for screen readers.
     */
    val DetailedDataDescription = SemanticsPropertyKey<String>("DetailedDataDescription")
    var SemanticsPropertyReceiver.detailedDataDescription by DetailedDataDescription

    /**
     * Whether the chart is interactive (has click handlers).
     */
    val IsInteractive = SemanticsPropertyKey<Boolean>("IsInteractive")
    var SemanticsPropertyReceiver.isInteractive by IsInteractive
}

/**
 * Enum for data trends.
 */
enum class DataTrend(val description: String) {
    INCREASING("increasing trend"),
    DECREASING("decreasing trend"),
    STABLE("stable trend"),
    FLUCTUATING("fluctuating trend"),
    UNKNOWN("no clear trend")
}

/**
 * Accessibility verbosity levels.
 */
enum class AccessibilityVerbosity {
    MINIMAL,    // Just type and count
    STANDARD,   // Type, count, min/max
    DETAILED,   // Everything including trends and descriptions
    CUSTOM      // User-provided descriptions
}

