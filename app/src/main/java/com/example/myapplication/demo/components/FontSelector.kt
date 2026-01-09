package com.example.myapplication.demo.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FontSelector(
    selectedWeight: FontWeight,
    onWeightSelected: (FontWeight) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Font Weight",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val weights = listOf(
            FontWeight.Light to "Light",
            FontWeight.Normal to "Normal",
            FontWeight.Medium to "Medium",
            FontWeight.SemiBold to "SemiBold",
            FontWeight.Bold to "Bold"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weights.forEach { (weight, label) ->
                FilterChip(
                    selected = selectedWeight == weight,
                    onClick = { onWeightSelected(weight) },
                    label = { Text(label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

