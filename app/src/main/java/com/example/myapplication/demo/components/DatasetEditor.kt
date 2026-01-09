package com.example.myapplication.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.demo.utils.DemoDataGenerator

@Composable
fun DatasetEditor(
    dataSize: Int,
    onDataSizeChange: (Int) -> Unit,
    onPresetSelected: (DemoDataGenerator.DataPreset) -> Unit,
    onAddPoint: () -> Unit,
    onRemovePoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Dataset Size: $dataSize",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRemovePoint,
                enabled = dataSize > 1
            ) {
                Icon(Icons.Default.Delete, "Remove Point")
            }
            
            Slider(
                value = dataSize.toFloat(),
                onValueChange = { onDataSizeChange(it.toInt()) },
                valueRange = 1f..100f,
                steps = 98,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onAddPoint,
                enabled = dataSize < 500
            ) {
                Icon(Icons.Default.Add, "Add Point")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Presets",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DemoDataGenerator.DataPreset.entries.forEach { preset ->
                val size = DemoDataGenerator.getPresetSize(preset)
                AssistChip(
                    onClick = { onPresetSelected(preset) },
                    label = { Text("${preset.name.lowercase().replaceFirstChar { it.uppercase() }} ($size)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

