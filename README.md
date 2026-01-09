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

![Line Chart](https://github.com/user-attachments/assets/2ee37486-6086-46c6-997c-adf3c80b400d)
![Bar Chart](https://github.com/user-attachments/assets/e652d545-d46a-4c4e-a6b2-8a67da2ef3cc)
![Pie Chart](https://github.com/user-attachments/assets/a954031b-d1e9-46fc-a0de-98d3e6a57015)
![Demo Screen](https://github.com/user-attachments/assets/03b645e5-87be-4823-817f-e9b03f2654e0)





## License

MIT License

Copyright (c) 2026 robyrawat



