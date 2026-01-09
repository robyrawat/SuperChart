package com.example.myapplication.demo.screens

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.components.ControlSlider
import com.example.myapplication.demo.components.SectionHeader
import com.example.myapplication.demo.utils.DemoDataGenerator
import com.superchart.charts.BarChart
import com.superchart.data.ChartDataset
import com.superchart.export.ChartExporter
import com.superchart.export.ExportConfig
import com.superchart.export.rememberChartCaptureState
import com.superchart.export.capturable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

enum class ExportFormat {
    PNG, CSV, JSON
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDemoScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val captureState = rememberChartCaptureState()

    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var isExporting by remember { mutableStateOf(false) }
    var exportWidth by remember { mutableIntStateOf(1200) }
    var exportHeight by remember { mutableIntStateOf(800) }
    var includeTitle by remember { mutableStateOf(true) }
    var exportTitle by remember { mutableStateOf("Chart Export") }

    val sampleDataset = remember {
        DemoDataGenerator.generateDataset(
            count = 12,
            minValue = 20f,
            maxValue = 100f,
            label = "Monthly Sales",
            color = Color(0xFF2196F3),
            useMonthLabels = true
        )
    }

    // Handle PNG export when bitmap is captured
    LaunchedEffect(captureState.capturedBitmap) {
        captureState.capturedBitmap?.let { bitmap ->
            exportPNG(
                context = context,
                bitmap = bitmap,
                width = exportWidth,
                height = exportHeight,
                includeTitle = includeTitle,
                title = if (includeTitle) exportTitle else null
            )
            captureState.reset()
            isExporting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Demo") },
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
                    .padding(16.dp)
            ) {
                BarChart(
                    datasets = listOf(sampleDataset),
                    showLegend = true,
                    showValueLabels = true,
                    animationDurationMs = 800,
                    modifier = Modifier.capturable(captureState)
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info card about file location
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "📁 File Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Files are saved to your Downloads folder.\nOpen Files app → Downloads to view them.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                SectionHeader("Export Format")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.entries.forEach { format ->
                        FilterChip(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            label = { Text(format.name) },
                            leadingIcon = {
                                Icon(
                                    when (format) {
                                        ExportFormat.PNG -> Icons.Default.Image
                                        ExportFormat.CSV -> Icons.Default.TableView
                                        ExportFormat.JSON -> Icons.Default.Code
                                    },
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                when (selectedFormat) {
                    ExportFormat.PNG -> {
                        SectionHeader("PNG Settings")

                        ControlSlider(
                            label = "Width",
                            value = exportWidth.toFloat(),
                            onValueChange = { exportWidth = it.toInt() },
                            valueRange = 400f..4096f,
                            valueDisplay = { "${it.toInt()}px" }
                        )

                        ControlSlider(
                            label = "Height",
                            value = exportHeight.toFloat(),
                            onValueChange = { exportHeight = it.toInt() },
                            valueRange = 300f..4096f,
                            valueDisplay = { "${it.toInt()}px" }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Include Title", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = includeTitle,
                                onCheckedChange = { includeTitle = it }
                            )
                        }

                        if (includeTitle) {
                            OutlinedTextField(
                                value = exportTitle,
                                onValueChange = { exportTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    ExportFormat.CSV -> {
                        SectionHeader("CSV Settings")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "CSV Format",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Exports data with headers:\nLabel, Value",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    ExportFormat.JSON -> {
                        SectionHeader("JSON Settings")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "JSON Structure",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = """
                                        {
                                          "chartType": "bar",
                                          "timestamp": "...",
                                          "dataset": {
                                            "label": "...",
                                            "entries": [...]
                                          }
                                        }
                                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Button(
                    onClick = {
                        scope.launch {
                            isExporting = true
                            try {
                                when (selectedFormat) {
                                    ExportFormat.PNG -> {
                                        // Trigger chart capture
                                        captureState.startCapture()
                                    }
                                    ExportFormat.CSV -> {
                                        exportCSV(
                                            context = context,
                                            dataset = sampleDataset
                                        )
                                        isExporting = false
                                    }
                                    ExportFormat.JSON -> {
                                        exportJSON(
                                            context = context,
                                            dataset = sampleDataset
                                        )
                                        isExporting = false
                                    }
                                }
                            } catch (e: Exception) {
                                isExporting = false
                                Toast.makeText(
                                    context,
                                    "Export error: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isExporting) "Exporting..." else "Export ${selectedFormat.name}")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private suspend fun exportPNG(
    context: Context,
    bitmap: Bitmap,
    width: Int,
    height: Int,
    includeTitle: Boolean,
    title: String?
) {
    withContext(Dispatchers.IO) {
        try {
            // Save to Downloads folder
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "SuperChart_${System.currentTimeMillis()}.png"
            val file = File(downloadsDir, fileName)

            // Save bitmap to file
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Notify media scanner
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(intent)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "✅ PNG saved to Downloads!\n$fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Export error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private suspend fun exportCSV(context: Context, dataset: ChartDataset) {
    withContext(Dispatchers.IO) {
        try {
            // Save to Downloads folder
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "SuperChart_Data_${System.currentTimeMillis()}.csv"
            val file = File(downloadsDir, fileName)

            FileWriter(file).use { writer ->
                writer.append("Label,Value\n")
                dataset.entries.forEach { entry ->
                    writer.append("${entry.label},${entry.value}\n")
                }
            }

            // Notify media scanner
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(intent)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "✅ CSV saved to Downloads!\n$fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "CSV export error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

private suspend fun exportJSON(context: Context, dataset: ChartDataset) {
    withContext(Dispatchers.IO) {
        try {
            // Save to Downloads folder
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val fileName = "SuperChart_Data_${System.currentTimeMillis()}.json"
            val file = File(downloadsDir, fileName)

            val jsonBuilder = StringBuilder()
            jsonBuilder.append("{\n")
            jsonBuilder.append("  \"chartType\": \"bar\",\n")
            jsonBuilder.append("  \"timestamp\": \"${System.currentTimeMillis()}\",\n")
            jsonBuilder.append("  \"dataset\": {\n")
            jsonBuilder.append("    \"label\": \"${dataset.label ?: "Data"}\",\n")
            jsonBuilder.append("    \"entries\": [\n")

            dataset.entries.forEachIndexed { index, entry ->
                jsonBuilder.append("      {\n")
                jsonBuilder.append("        \"label\": \"${entry.label}\",\n")
                jsonBuilder.append("        \"value\": ${entry.value}\n")
                jsonBuilder.append("      }")
                if (index < dataset.entries.size - 1) {
                    jsonBuilder.append(",")
                }
                jsonBuilder.append("\n")
            }

            jsonBuilder.append("    ]\n")
            jsonBuilder.append("  }\n")
            jsonBuilder.append("}\n")

            file.writeText(jsonBuilder.toString())

            // Notify media scanner
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(intent)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "✅ JSON saved to Downloads!\n$fileName",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "JSON export error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

