package com.superchart.export

import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.core.view.drawToBitmap

/**
 * State holder for chart capture operations.
 */
class ChartCaptureState {
    var capturedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var isCapturing by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    internal fun onBitmapCaptured(bitmap: Bitmap) {
        capturedBitmap = bitmap
        isCapturing = false
        error = null
    }

    internal fun onCaptureError(message: String) {
        error = message
        isCapturing = false
        capturedBitmap = null
    }

    fun startCapture() {
        isCapturing = true
        error = null
    }

    fun reset() {
        capturedBitmap = null
        error = null
        isCapturing = false
    }
}

/**
 * Remember a ChartCaptureState.
 */
@Composable
fun rememberChartCaptureState(): ChartCaptureState {
    return remember { ChartCaptureState() }
}

/**
 * Modifier to make a composable capturable as bitmap.
 */
@Composable
fun Modifier.capturable(
    captureState: ChartCaptureState,
    onCaptured: (Bitmap) -> Unit = {}
): Modifier {
    val view = LocalView.current
    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(captureState.isCapturing) {
        if (captureState.isCapturing && size.width > 0 && size.height > 0) {
            try {
                // Find the ComposeView and capture it
                val composeView = findComposeView(view)
                if (composeView != null) {
                    val bitmap = composeView.drawToBitmap()
                    captureState.onBitmapCaptured(bitmap)
                    onCaptured(bitmap)
                } else {
                    captureState.onCaptureError("Could not find compose view")
                }
            } catch (e: Exception) {
                captureState.onCaptureError("Capture failed: ${e.message}")
            }
        }
    }

    return this.onGloballyPositioned { coordinates ->
        size = IntSize(coordinates.size.width, coordinates.size.height)
    }
}

/**
 * Capture a chart as bitmap.
 *
 * @param captureState State holder for capture operation
 * @param onCaptured Callback when bitmap is captured
 */
fun captureChart(
    captureState: ChartCaptureState,
    onCaptured: (Bitmap) -> Unit = {}
) {
    captureState.startCapture()
}

/**
 * Find parent ComposeView.
 */
private fun findComposeView(view: View): ComposeView? {
    var currentView: View? = view
    while (currentView != null) {
        if (currentView is ComposeView) {
            return currentView
        }
        currentView = currentView.parent as? View
    }
    return null
}

/**
 * Extension function to export chart directly.
 */
@Composable
fun ChartCaptureState.exportToPNG(
    fileName: String,
    config: ExportConfig = ExportConfig(),
    onResult: (ExportResult) -> Unit = {}
) {
    val context = LocalContext.current
    val exporter = remember { ChartExporter(context) }

    LaunchedEffect(capturedBitmap) {
        capturedBitmap?.let { bitmap ->
            val result = exporter.exportToPNG(bitmap, fileName, config)
            onResult(result)
        }
    }
}

/**
 * Extension function to export chart to downloads.
 */
@Composable
fun ChartCaptureState.exportToDownloads(
    fileName: String,
    config: ExportConfig = ExportConfig(),
    onResult: (ExportResult) -> Unit = {}
) {
    val context = LocalContext.current
    val exporter = remember { ChartExporter(context) }

    LaunchedEffect(capturedBitmap) {
        capturedBitmap?.let { bitmap ->
            val result = exporter.exportToDownloads(bitmap, fileName, config)
            onResult(result)
        }
    }
}

/**
 * Extension function to share chart.
 */
@Composable
fun ChartCaptureState.shareChart(
    fileName: String = "chart",
    config: ExportConfig = ExportConfig()
) {
    val context = LocalContext.current
    val exporter = remember { ChartExporter(context) }

    LaunchedEffect(capturedBitmap) {
        capturedBitmap?.let { bitmap ->
            exporter.shareChart(bitmap, fileName, config)
        }
    }
}

