package com.example.myapplication.demo.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.superchart.data.ChartDataset
import com.superchart.data.ChartEntry

class ChartPlaygroundState {

    var datasets by mutableStateOf<List<ChartDataset>>(emptyList())
        private set

    var selectedDatasetIndex by mutableIntStateOf(-1)
        private set

    var selectedEntryIndex by mutableIntStateOf(-1)
        private set

    var chartColor by mutableStateOf(Color(0xFF2196F3))
        private set

    var backgroundColor by mutableStateOf(Color.White)
        private set

    var gridColor by mutableStateOf(Color.LightGray)
        private set

    var fontSize by mutableFloatStateOf(12f)
        private set

    var fontWeight by mutableStateOf(FontWeight.Normal)
        private set

    var showLabels by mutableStateOf(false)
        private set

    var animationEnabled by mutableStateOf(true)
        private set

    var animationDuration by mutableIntStateOf(800)
        private set

    var showAxis by mutableStateOf(true)
        private set

    var rotateLabels by mutableStateOf(false)
        private set

    var valueFormatter by mutableStateOf(ValueFormatterType.DECIMAL)
        private set

    var showLegend by mutableStateOf(true)
        private set

    var showValueLabels by mutableStateOf(false)
        private set

    var labelColor by mutableStateOf(Color(0xFF212121))
        private set

    var valueLabelBackgroundColor by mutableStateOf(Color(0xDD000000))
        private set

    var valueLabelTextColor by mutableStateOf(Color.White)
        private set

    var customOverlayText by mutableStateOf("")
        private set

    var showCustomOverlay by mutableStateOf(false)
        private set

    var overlayTextColor by mutableStateOf(Color(0xFF2196F3))
        private set

    fun updateDatasets(newDatasets: List<ChartDataset>) {
        datasets = newDatasets
        clearSelection()
    }

    fun updateSingleDataset(dataset: ChartDataset) {
        datasets = listOf(dataset)
        clearSelection()
    }

    fun selectValue(datasetIndex: Int, entryIndex: Int) {
        selectedDatasetIndex = datasetIndex
        selectedEntryIndex = entryIndex
    }

    fun clearSelection() {
        selectedDatasetIndex = -1
        selectedEntryIndex = -1
    }

    fun getSelectedEntry(): ChartEntry? {
        if (selectedDatasetIndex < 0 || selectedEntryIndex < 0) return null
        return datasets.getOrNull(selectedDatasetIndex)?.entries?.getOrNull(selectedEntryIndex)
    }

    fun updateChartColor(color: Color) {
        chartColor = color
        datasets = datasets.map { dataset ->
            dataset.copy(color = color)
        }
    }

    fun updateBackgroundColor(color: Color) {
        backgroundColor = color
    }

    fun updateGridColor(color: Color) {
        gridColor = color
    }

    fun updateFontSize(size: Float) {
        fontSize = size.coerceIn(8f, 24f)
    }

    fun updateFontWeight(weight: FontWeight) {
        fontWeight = weight
    }

    fun toggleLabels() {
        showLabels = !showLabels
    }

    fun toggleAnimation() {
        animationEnabled = !animationEnabled
    }

    fun updateAnimationDuration(duration: Int) {
        animationDuration = duration.coerceIn(0, 3000)
    }

    fun toggleAxis() {
        showAxis = !showAxis
    }

    fun toggleLabelRotation() {
        rotateLabels = !rotateLabels
    }

    fun updateValueFormatter(formatter: ValueFormatterType) {
        valueFormatter = formatter
    }

    fun toggleLegend() {
        showLegend = !showLegend
    }

    fun toggleValueLabels() {
        showValueLabels = !showValueLabels
    }

    fun updateLabelColor(color: Color) {
        labelColor = color
    }

    fun updateValueLabelBackgroundColor(color: Color) {
        valueLabelBackgroundColor = color
    }

    fun updateValueLabelTextColor(color: Color) {
        valueLabelTextColor = color
    }

    fun updateCustomOverlayText(text: String) {
        customOverlayText = text
    }

    fun toggleCustomOverlay() {
        showCustomOverlay = !showCustomOverlay
    }

    fun updateOverlayTextColor(color: Color) {
        overlayTextColor = color
    }

    fun addDataPoint(value: Float, label: String = "New") {
        if (datasets.isEmpty()) return

        val colorPalette = listOf(
            Color(0xFF2196F3), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFFFFC107),
            Color(0xFF9C27B0), Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF00BCD4),
            Color(0xFF8BC34A), Color(0xFFFF5252), Color(0xFF536DFE), Color(0xFFFFEB3B),
            Color(0xFF009688), Color(0xFFFF4081), Color(0xFF7C4DFF), Color(0xFF26C6DA),
            Color(0xFFAB47BC), Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFFFFCA28)
        )

        val updatedDatasets = datasets.mapIndexed { index, dataset ->
            if (index == 0) {
                val newIndex = dataset.entries.size
                val newColor = colorPalette[newIndex % colorPalette.size]
                dataset.copy(
                    entries = dataset.entries + ChartEntry(label, value, newColor)
                )
            } else {
                dataset
            }
        }
        datasets = updatedDatasets
    }

    fun removeLastDataPoint() {
        if (datasets.isEmpty()) return

        val updatedDatasets = datasets.mapIndexed { index, dataset ->
            if (index == 0 && dataset.entries.isNotEmpty()) {
                dataset.copy(entries = dataset.entries.dropLast(1))
            } else {
                dataset
            }
        }
        datasets = updatedDatasets
        clearSelection()
    }

    fun updateDataPointValue(datasetIndex: Int, entryIndex: Int, newValue: Float) {
        if (datasetIndex < 0 || datasetIndex >= datasets.size) return

        val dataset = datasets[datasetIndex]
        if (entryIndex < 0 || entryIndex >= dataset.entries.size) return

        val updatedEntries = dataset.entries.toMutableList()
        val entry = updatedEntries[entryIndex]
        updatedEntries[entryIndex] = entry.copy(value = newValue.coerceAtLeast(0f))

        val updatedDatasets = datasets.toMutableList()
        updatedDatasets[datasetIndex] = dataset.copy(entries = updatedEntries)
        datasets = updatedDatasets
    }

    fun reset() {
        datasets = emptyList()
        clearSelection()
        chartColor = Color(0xFF2196F3)
        backgroundColor = Color.White
        gridColor = Color.LightGray
        fontSize = 12f
        fontWeight = FontWeight.Normal
        showLabels = true
        animationEnabled = true
        animationDuration = 800
        showAxis = true
        rotateLabels = false
        valueFormatter = ValueFormatterType.DECIMAL
        showLegend = true
        showValueLabels = false
    }
}

enum class ValueFormatterType {
    DECIMAL,
    PERCENTAGE,
    CURRENCY,
    COMPACT
}

