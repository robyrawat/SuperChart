package com.example.myapplication.demo.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.components.*
import com.example.myapplication.demo.state.ChartPlaygroundState
import com.example.myapplication.demo.utils.DemoDataGenerator
import com.superchart.charts.BarChart
import com.superchart.charts.LineChart
import com.superchart.charts.PieChart
import com.superchart.theme.AxisConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPlaygroundScreen(
    chartType: String,
    onNavigateBack: () -> Unit
) {
    val state = remember { ChartPlaygroundState() }
    var dataSize by remember { mutableIntStateOf(12) }

    LaunchedEffect(chartType) {
        val dataset = DemoDataGenerator.generateDataset(
            count = dataSize,
            minValue = 10f,
            maxValue = 100f,
            label = "Sample Data",
            color = state.chartColor,
            useMonthLabels = true
        )

        when (chartType) {
            "pie" -> state.updateSingleDataset(dataset)
            else -> state.updateDatasets(listOf(dataset))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (chartType) {
                            "line" -> "Line Chart Playground"
                            "bar" -> "Bar Chart Playground"
                            "pie" -> "Pie Chart Playground"
                            else -> "Chart Playground"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(state.backgroundColor)
                    .padding(16.dp)
            ) {
                when (chartType) {
                    "line" -> {
                        if (state.datasets.isNotEmpty()) {
                            LineChart(
                                datasets = state.datasets,
                                showLegend = state.showLegend,
                                showValueLabels = state.showValueLabels,
                                animationDurationMs = if (state.animationEnabled) state.animationDuration else 0,
                                axisConfig = AxisConfig(
                                    gridColor = state.gridColor,
                                    labelColor = state.labelColor
                                ),
                                onPointClick = { dsIndex, entryIndex, _ ->
                                    state.selectValue(dsIndex, entryIndex)
                                }
                            )
                        }
                    }
                    "bar" -> {
                        if (state.datasets.isNotEmpty()) {
                            BarChart(
                                datasets = state.datasets,
                                showLegend = state.showLegend,
                                showValueLabels = state.showValueLabels,
                                animationDurationMs = if (state.animationEnabled) state.animationDuration else 0,
                                axisConfig = AxisConfig(
                                    gridColor = state.gridColor,
                                    labelColor = state.labelColor
                                ),
                                onBarClick = { dsIndex, entryIndex, _ ->
                                    state.selectValue(dsIndex, entryIndex)
                                }
                            )
                        }
                    }
                    "pie" -> {
                        if (state.datasets.isNotEmpty()) {
                            PieChart(
                                dataset = state.datasets.first(),
                                showLegend = state.showLegend,
                                showLabels = state.showLabels,
                                animationDurationMs = if (state.animationEnabled) state.animationDuration else 0,
                                onSliceClick = { entryIndex, _ ->
                                    state.selectValue(0, entryIndex)
                                }
                            )
                        }
                    }
                }
            }

            // Custom text overlay on chart
            if (state.showCustomOverlay && state.customOverlayText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = state.customOverlayText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = state.overlayTextColor,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = state.getSelectedEntry() != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    state.getSelectedEntry()?.let { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📊 Selected Value",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = entry.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = String.format("%.1f", entry.value),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeader("Dataset Controls")

                DatasetEditor(
                    dataSize = dataSize,
                    onDataSizeChange = { newSize ->
                        dataSize = newSize
                        val dataset = DemoDataGenerator.generateDataset(
                            count = newSize,
                            minValue = 10f,
                            maxValue = 100f,
                            label = "Sample Data",
                            color = state.chartColor,
                            useMonthLabels = newSize <= 12
                        )
                        if (chartType == "pie") {
                            state.updateSingleDataset(dataset)
                        } else {
                            state.updateDatasets(listOf(dataset))
                        }
                    },
                    onPresetSelected = { preset ->
                        val size = DemoDataGenerator.getPresetSize(preset)
                        dataSize = size
                        val dataset = DemoDataGenerator.generateDataset(
                            count = size,
                            minValue = 10f,
                            maxValue = 100f,
                            label = "Sample Data",
                            color = state.chartColor,
                            useMonthLabels = size <= 12
                        )
                        if (chartType == "pie") {
                            state.updateSingleDataset(dataset)
                        } else {
                            state.updateDatasets(listOf(dataset))
                        }
                    },
                    onAddPoint = {
                        state.addDataPoint(
                            value = (10..100).random().toFloat(),
                            label = "Point ${dataSize + 1}"
                        )
                        dataSize++
                    },
                    onRemovePoint = {
                        if (dataSize > 1) {
                            state.removeLastDataPoint()
                            dataSize--
                        }
                    }
                )

                HorizontalDivider()

                SectionHeader("Colors")

                ColorPicker(
                    label = "Chart Color",
                    selectedColor = state.chartColor,
                    onColorSelected = { state.updateChartColor(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorPicker(
                    label = "Background Color",
                    selectedColor = state.backgroundColor,
                    onColorSelected = { state.updateBackgroundColor(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorPicker(
                    label = "Grid Color",
                    selectedColor = state.gridColor,
                    onColorSelected = { state.updateGridColor(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorPicker(
                    label = "Label Color",
                    selectedColor = state.labelColor,
                    onColorSelected = { state.updateLabelColor(it) }
                )

                HorizontalDivider()

                SectionHeader("Fonts")

                ControlSlider(
                    label = "Font Size",
                    value = state.fontSize,
                    onValueChange = { state.updateFontSize(it) },
                    valueRange = 8f..24f,
                    valueDisplay = { "${it.toInt()}sp" }
                )

                FontSelector(
                    selectedWeight = state.fontWeight,
                    onWeightSelected = { state.updateFontWeight(it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show Labels", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.showLabels,
                        onCheckedChange = { state.toggleLabels() }
                    )
                }

                HorizontalDivider()

                SectionHeader("Animation")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Animation", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.animationEnabled,
                        onCheckedChange = { state.toggleAnimation() }
                    )
                }

                AnimatedVisibility(visible = state.animationEnabled) {
                    ControlSlider(
                        label = "Animation Duration",
                        value = state.animationDuration.toFloat(),
                        onValueChange = { state.updateAnimationDuration(it.toInt()) },
                        valueRange = 0f..3000f,
                        valueDisplay = { "${it.toInt()}ms" }
                    )
                }

                HorizontalDivider()

                SectionHeader("Display Options")

                if (chartType != "pie") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Show Axis", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = state.showAxis,
                            onCheckedChange = { state.toggleAxis() }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show Legend", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.showLegend,
                        onCheckedChange = { state.toggleLegend() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show Value Labels", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.showValueLabels,
                        onCheckedChange = { state.toggleValueLabels() }
                    )
                }

                HorizontalDivider()

                SectionHeader("Custom Text Overlay")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Show Custom Text", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.showCustomOverlay,
                        onCheckedChange = { state.toggleCustomOverlay() }
                    )
                }

                AnimatedVisibility(visible = state.showCustomOverlay) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.customOverlayText,
                            onValueChange = { state.updateCustomOverlayText(it) },
                            label = { Text("Overlay Text") },
                            placeholder = { Text("e.g., 'Q4 Target Achieved'") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )

                        ColorPicker(
                            label = "Overlay Text Color",
                            selectedColor = state.overlayTextColor,
                            onColorSelected = { state.updateOverlayTextColor(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

