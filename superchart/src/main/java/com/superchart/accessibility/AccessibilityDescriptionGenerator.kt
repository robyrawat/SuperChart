package com.superchart.accessibility

import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
import com.superchart.formatter.ValueFormatter
import com.superchart.formatter.DefaultFormatter
import kotlin.math.abs

/**
 * Accessibility configuration for charts.
 */
data class AccessibilityConfig(
    val enabled: Boolean = true,
    val verbosity: AccessibilityVerbosity = AccessibilityVerbosity.STANDARD,
    val valueFormatter: ValueFormatter = DefaultFormatter(),
    val customDescription: String? = null,
    val announceDataPoints: Boolean = true,
    val announceTrends: Boolean = true,
    val announceStatistics: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val enableAudioCues: Boolean = false
)

/**
 * Generates accessibility descriptions for charts.
 */
class AccessibilityDescriptionGenerator(
    private val config: AccessibilityConfig = AccessibilityConfig()
) {

    /**
     * Generate complete chart description for screen readers.
     */
    fun generateChartDescription(
        chartType: String,
        datasets: List<ChartDataset>
    ): String {
        if (!config.enabled) return ""

        // Use custom description if provided
        config.customDescription?.let { return it }

        return when (config.verbosity) {
            AccessibilityVerbosity.MINIMAL -> generateMinimalDescription(chartType, datasets)
            AccessibilityVerbosity.STANDARD -> generateStandardDescription(chartType, datasets)
            AccessibilityVerbosity.DETAILED -> generateDetailedDescription(chartType, datasets)
            AccessibilityVerbosity.CUSTOM -> config.customDescription ?: generateStandardDescription(chartType, datasets)
        }
    }

    /**
     * Generate minimal description (type + count).
     */
    private fun generateMinimalDescription(
        chartType: String,
        datasets: List<ChartDataset>
    ): String {
        val totalPoints = datasets.sumOf { it.entries.size }
        return "$chartType chart with $totalPoints data points"
    }

    /**
     * Generate standard description (type + count + range).
     */
    private fun generateStandardDescription(
        chartType: String,
        datasets: List<ChartDataset>
    ): String {
        val totalPoints = datasets.sumOf { it.entries.size }
        val allValues = datasets.flatMap { it.entries.map { entry -> entry.value } }

        if (allValues.isEmpty()) {
            return "$chartType chart with no data"
        }

        val min = allValues.minOrNull() ?: 0f
        val max = allValues.maxOrNull() ?: 0f

        val minFormatted = config.valueFormatter.format(min)
        val maxFormatted = config.valueFormatter.format(max)

        return buildString {
            append("$chartType chart with $totalPoints data points. ")
            append("Values range from $minFormatted to $maxFormatted.")
        }
    }

    /**
     * Generate detailed description (everything).
     */
    private fun generateDetailedDescription(
        chartType: String,
        datasets: List<ChartDataset>
    ): String {
        val totalPoints = datasets.sumOf { it.entries.size }
        val allValues = datasets.flatMap { it.entries.map { entry -> entry.value } }

        if (allValues.isEmpty()) {
            return "$chartType chart with no data"
        }

        val min = allValues.minOrNull() ?: 0f
        val max = allValues.maxOrNull() ?: 0f
        val avg = allValues.average().toFloat()

        val minFormatted = config.valueFormatter.format(min)
        val maxFormatted = config.valueFormatter.format(max)
        val avgFormatted = config.valueFormatter.format(avg)

        val trend = if (config.announceTrends) {
            detectTrend(allValues)
        } else {
            null
        }

        return buildString {
            append("$chartType chart with $totalPoints data points")

            if (datasets.size > 1) {
                append(" across ${datasets.size} datasets")
            }
            append(". ")

            if (config.announceStatistics) {
                append("Minimum value: $minFormatted. ")
                append("Maximum value: $maxFormatted. ")
                append("Average value: $avgFormatted. ")
            }

            trend?.let {
                append("Data shows ${it.description}. ")
            }

            if (config.announceDataPoints && datasets.size == 1) {
                val dataDescription = generateDataPointsDescription(datasets[0])
                append(dataDescription)
            }
        }
    }

    /**
     * Generate description for individual data points.
     */
    fun generateDataPointsDescription(dataset: ChartDataset): String {
        if (dataset.entries.isEmpty()) return "No data points"

        return buildString {
            append("Data points: ")
            dataset.entries.forEachIndexed { index, entry ->
                append(entry.label)
                append(": ")
                append(config.valueFormatter.format(entry.value))

                if (index < dataset.entries.size - 1) {
                    append(", ")
                }
            }
        }
    }

    /**
     * Generate description for a single data point.
     */
    fun generateDataPointDescription(
        entry: ChartEntry,
        dataset: ChartDataset
    ): String {
        val formattedValue = config.valueFormatter.format(entry.value)

        return buildString {
            if (dataset.label != null) {
                append("${dataset.label}, ")
            }
            append("${entry.label}: $formattedValue")
        }
    }

    /**
     * Generate description for pie chart slice with percentage.
     */
    fun generatePieSliceDescription(
        entry: ChartEntry,
        percentage: Float,
        dataset: ChartDataset
    ): String {
        val formattedValue = config.valueFormatter.format(entry.value)
        val formattedPercentage = String.format("%.1f", percentage)

        return buildString {
            append("${entry.label}: ")
            append("$formattedValue, ")
            append("$formattedPercentage percent of total")
        }
    }

    /**
     * Detect data trend.
     */
    private fun detectTrend(values: List<Float>): DataTrend {
        if (values.size < 2) return DataTrend.UNKNOWN

        var increasing = 0
        var decreasing = 0
        val threshold = 0.01f // 1% threshold for stability

        for (i in 1 until values.size) {
            val prev = values[i - 1]
            val curr = values[i]

            // Avoid division by zero
            if (prev == 0f) {
                // If previous is 0, check absolute change
                when {
                    curr > threshold -> increasing++
                    curr < -threshold -> decreasing++
                }
                continue
            }

            val change = (curr - prev) / prev

            when {
                change > threshold -> increasing++
                change < -threshold -> decreasing++
            }
        }

        val totalChanges = increasing + decreasing
        if (totalChanges == 0) return DataTrend.STABLE

        val increasingRatio = increasing.toFloat() / totalChanges
        val decreasingRatio = decreasing.toFloat() / totalChanges

        return when {
            increasingRatio > 0.7f -> DataTrend.INCREASING
            decreasingRatio > 0.7f -> DataTrend.DECREASING
            abs(increasingRatio - decreasingRatio) < 0.2f -> DataTrend.FLUCTUATING
            else -> DataTrend.STABLE
        }
    }

    /**
     * Generate summary statistics.
     */
    fun generateStatisticsSummary(datasets: List<ChartDataset>): String {
        val allValues = datasets.flatMap { it.entries.map { entry -> entry.value } }

        if (allValues.isEmpty()) return "No data available"

        val min = allValues.minOrNull() ?: 0f
        val max = allValues.maxOrNull() ?: 0f
        val avg = allValues.average().toFloat()
        val count = allValues.size

        return buildString {
            append("Statistics: ")
            append("$count values, ")
            append("minimum ${config.valueFormatter.format(min)}, ")
            append("maximum ${config.valueFormatter.format(max)}, ")
            append("average ${config.valueFormatter.format(avg)}")
        }
    }

    /**
     * Generate trend description.
     */
    fun generateTrendDescription(datasets: List<ChartDataset>): String {
        val allValues = datasets.flatMap { it.entries.map { entry -> entry.value } }

        if (allValues.isEmpty()) return "No trend data"

        val trend = detectTrend(allValues)
        return "Data trend: ${trend.description}"
    }
}

/**
 * Accessibility action descriptions.
 */
object AccessibilityActions {
    const val TAP_TO_HEAR_VALUE = "Double tap to hear value"
    const val TAP_TO_HEAR_DETAILS = "Double tap to hear details"
    const val TAP_TO_EXPLORE = "Double tap to explore chart"
    const val SWIPE_TO_NAVIGATE = "Swipe right or left to navigate data points"
    const val TAP_TO_HEAR_STATISTICS = "Double tap to hear statistics"
    const val TAP_TO_EXPORT = "Double tap to export chart"
}

/**
 * Haptic feedback patterns for charts.
 */
enum class HapticPattern {
    LIGHT,      // Small value or decrease
    MEDIUM,     // Medium value
    HEAVY,      // Large value or increase
    SUCCESS,    // Action success
    ERROR       // Action error
}

