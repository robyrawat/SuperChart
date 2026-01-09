package com.example.myapplication.demo.utils

import androidx.compose.ui.graphics.Color
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry
import kotlin.math.sin
import kotlin.random.Random

object DemoDataGenerator {

    private val monthLabels = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    private val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun generateDataset(
        count: Int,
        minValue: Float = 0f,
        maxValue: Float = 100f,
        label: String = "Dataset",
        color: Color = Color.Blue,
        useMonthLabels: Boolean = false
    ): ChartDataset {
        require(count > 0) { "Count must be positive" }
        require(maxValue > minValue) { "Max value must be greater than min value" }

        val entries = (0 until count).map { index ->
            val itemLabel = when {
                useMonthLabels && count <= 12 -> monthLabels.getOrNull(index) ?: "Item $index"
                count <= 7 -> dayLabels.getOrNull(index) ?: "Item $index"
                else -> "Item $index"
            }

            val value = Random.nextFloat() * (maxValue - minValue) + minValue
            ChartEntry(
                label = itemLabel,
                value = value
            )
        }

        return ChartDataset(
            entries = entries,
            label = label,
            color = color
        )
    }

    fun generateSineWaveDataset(
        count: Int,
        amplitude: Float = 50f,
        offset: Float = 50f,
        label: String = "Sine Wave",
        color: Color = Color.Blue
    ): ChartDataset {
        require(count > 0) { "Count must be positive" }

        val entries = (0 until count).map { index ->
            val x = (index.toFloat() / count) * 4 * Math.PI
            val value = (sin(x) * amplitude + offset).toFloat()
            ChartEntry(
                label = "Point $index",
                value = value.coerceAtLeast(0f)
            )
        }

        return ChartDataset(
            entries = entries,
            label = label,
            color = color
        )
    }

    fun generateTrendingDataset(
        count: Int,
        startValue: Float = 20f,
        endValue: Float = 80f,
        noiseAmount: Float = 10f,
        label: String = "Trending",
        color: Color = Color.Blue
    ): ChartDataset {
        require(count > 0) { "Count must be positive" }

        val step = (endValue - startValue) / count
        val entries = (0 until count).map { index ->
            val baseValue = startValue + (step * index)
            val noise = (Random.nextFloat() - 0.5f) * 2 * noiseAmount
            val value = (baseValue + noise).coerceAtLeast(0f)

            ChartEntry(
                label = "Item $index",
                value = value
            )
        }

        return ChartDataset(
            entries = entries,
            label = label,
            color = color
        )
    }

    fun generateMultipleDatasets(
        datasetCount: Int,
        entriesPerDataset: Int,
        minValue: Float = 0f,
        maxValue: Float = 100f,
        useMonthLabels: Boolean = false
    ): List<ChartDataset> {
        require(datasetCount > 0) { "Dataset count must be positive" }

        val colors = listOf(
            Color(0xFF2196F3), // Blue
            Color(0xFF4CAF50), // Green
            Color(0xFFF44336), // Red
            Color(0xFFFF9800), // Orange
            Color(0xFF9C27B0), // Purple
            Color(0xFFFFEB3B), // Yellow
        )

        return (0 until datasetCount).map { index ->
            generateDataset(
                count = entriesPerDataset,
                minValue = minValue,
                maxValue = maxValue,
                label = "Dataset ${index + 1}",
                color = colors[index % colors.size],
                useMonthLabels = useMonthLabels
            )
        }
    }

    enum class DataPreset {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }

    fun getPresetSize(preset: DataPreset): Int = when (preset) {
        DataPreset.SMALL -> 5
        DataPreset.MEDIUM -> 30
        DataPreset.LARGE -> 100
        DataPreset.EXTRA_LARGE -> 500
    }
}

