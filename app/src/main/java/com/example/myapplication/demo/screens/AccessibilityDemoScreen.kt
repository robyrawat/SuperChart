package com.example.myapplication.demo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.components.SectionHeader
import com.example.myapplication.demo.utils.DemoDataGenerator
import com.superchart.accessibility.AccessibilityConfig
import com.superchart.accessibility.AccessibilityVerbosity
import com.superchart.charts.LineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityDemoScreen(
    onNavigateBack: () -> Unit
) {
    var verbosity by remember { mutableStateOf(AccessibilityVerbosity.STANDARD) }

    var selectedValue by remember { mutableStateOf<String?>(null) }

    val sampleDataset = remember {
        DemoDataGenerator.generateTrendingDataset(
            count = 12,
            startValue = 30f,
            endValue = 80f,
            label = "Sales Trend",
            color = Color(0xFF4CAF50)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility Demo") },
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
                LineChart(
                    datasets = listOf(sampleDataset),
                    showLegend = true,
                    showValueLabels = true,
                    animationDurationMs = 800,
                    accessibilityConfig = AccessibilityConfig(
                        enabled = true,
                        verbosity = verbosity
                    ),
                    onPointClick = { _, _, entry ->
                        selectedValue = "${entry.label}: ${entry.value}"
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "How to Use",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enable TalkBack in System Settings → Accessibility to hear chart descriptions and data announcements.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (selectedValue != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Selected Value",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedValue ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                SectionHeader("Verbosity Level")

                Text(
                    text = "Controls how much information is announced by screen readers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccessibilityVerbosity.entries.forEach { level ->
                        FilterChip(
                            selected = verbosity == level,
                            onClick = { verbosity = level },
                            label = {
                                Column {
                                    Text(
                                        text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (level) {
                                            AccessibilityVerbosity.MINIMAL -> "Basic chart type and value count"
                                            AccessibilityVerbosity.STANDARD -> "Includes summary statistics"
                                            AccessibilityVerbosity.DETAILED -> "Full analysis with trends and patterns"
                                            AccessibilityVerbosity.CUSTOM -> "Custom accessibility descriptions"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider()

                SectionHeader("Accessibility Features")

                FeatureItem(
                    title = "Chart Summary",
                    description = "Announces chart type, data range, and overall trends"
                )

                FeatureItem(
                    title = "Value Navigation",
                    description = "Navigate through data points with swipe gestures"
                )

                FeatureItem(
                    title = "Statistical Analysis",
                    description = "Provides min, max, average, and trend information"
                )

                FeatureItem(
                    title = "Interactive Feedback",
                    description = "Announces selected values and percentages"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

