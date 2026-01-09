# SuperChart

A Jetpack Compose charting library for Android.

## Features

- Three chart types: Line, Bar, Pie
- Interactive touch callbacks
- Accessibility support (TalkBack)
- Smooth animations
- Export to PNG
- Material 3 design

## Requirements

- Android SDK 24+
- Jetpack Compose

## Installation

```gradle
dependencies {
    implementation("com.superchart:superchart:1.0.0")
}
```

## Quick Start

```kotlin
@Composable
fun Example() {
    val dataset = ChartDataset(
        entries = listOf(
            ChartEntry("Jan", 45f),
            ChartEntry("Feb", 67f),
            ChartEntry("Mar", 52f)
        ),
        color = Color.Blue
    )
    
    LineChart(
        datasets = listOf(dataset),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
```

## Charts

### LineChart
```kotlin
LineChart(
    datasets = listOf(dataset),
    showLegend = true,
    onPointClick = { datasetIndex, entryIndex, entry ->
        // Handle click
    }
)
```

### BarChart
```kotlin
BarChart(
    datasets = listOf(dataset),
    grouped = true,
    onBarClick = { datasetIndex, entryIndex, entry ->
        // Handle click
    }
)
```

### PieChart
```kotlin
PieChart(
    dataset = dataset,
    donutMode = true,
    onSliceClick = { entryIndex, entry ->
        // Handle click
    }
)
```

## License

MIT License

Copyright (c) 2026 robyrawat



