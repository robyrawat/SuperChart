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
Here are some screenshots of SuperChart in action:

![Line Chart](https://github.com/user-attachments/assets/eec143c9-f0f1-4049-90e6-1c120f4fcba6)
![Bar Chart](https://github.com/user-attachments/assets/5da1034a-f102-4f2e-bd13-126542cd4fdc)
![Pie Chart](https://github.com/user-attachments/assets/78ca1241-70c5-4548-a9eb-bf7e688c850c)
![Demo Screen](https://github.com/user-attachments/assets/31c4a8ef-2992-450d-a2d3-1d63288b6382)


## License

MIT License

Copyright (c) 2026 robyrawat



