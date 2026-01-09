package com.superchart.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import java.io.File
import java.io.FileOutputStream

/**
 * Configuration for chart export.
 */
data class ExportConfig(
    val width: Int = 1200,
    val height: Int = 800,
    val backgroundColor: Color = Color.White,
    val quality: Int = 100, // 0-100, PNG compression quality
    val includeTitle: Boolean = true,
    val title: String? = null,
    val titleTextSize: Float = 48f,
    val titleColor: Color = Color.Black
) {
    init {
        // Validate dimensions to prevent OutOfMemoryError
        require(width in 1..4096) {
            "Width must be between 1 and 4096 pixels (got $width)"
        }
        require(height in 1..4096) {
            "Height must be between 1 and 4096 pixels (got $height)"
        }
        require(quality in 0..100) {
            "Quality must be between 0 and 100 (got $quality)"
        }
    }
}

/**
 * Result of chart export operation.
 */
sealed class ExportResult {
    data class Success(val file: File) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * Chart exporter for saving charts as images.
 */
class ChartExporter(context: Context) {
    // Use applicationContext to prevent Activity memory leaks
    private val appContext: Context = context.applicationContext
    private val notificationManager = DownloadNotificationManager(appContext)

    /**
     * Export chart to PNG file with notification.
     *
     * @param bitmap The chart bitmap to export
     * @param fileName Output filename (without extension)
     * @param config Export configuration
     * @param showNotification Show download notification (default: true)
     * @return ExportResult with success or error
     */
    fun exportToPNG(
        bitmap: Bitmap,
        fileName: String,
        config: ExportConfig = ExportConfig(),
        showNotification: Boolean = true
    ): ExportResult {
        return try {
            val file = File(appContext.filesDir, "$fileName.png")

            val finalBitmap = if (config.includeTitle && config.title != null) {
                addTitleToBitmap(bitmap, config)
            } else {
                bitmap
            }

            // Use 'use' extension for automatic stream closing
            FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, config.quality, outputStream)
            }

            // Recycle bitmap only if we created a new one
            if (finalBitmap != bitmap) {
                finalBitmap.recycle()
            }

            // Show notification with file location
            if (showNotification) {
                notificationManager.showExportComplete(file, ExportFormat.PNG)
            }

            ExportResult.Success(file)
        } catch (e: Exception) {
            if (showNotification) {
                notificationManager.showExportError(fileName, e.message ?: "Unknown error")
            }
            ExportResult.Error("Failed to export PNG: ${e.message}")
        }
    }

    /**
     * Export chart to external storage (Downloads folder).
     *
     * @param bitmap The chart bitmap to export
     * @param fileName Output filename (without extension)
     * @param config Export configuration
     * @return ExportResult with success or error
     */
    fun exportToDownloads(
        bitmap: Bitmap,
        fileName: String,
        config: ExportConfig = ExportConfig()
    ): ExportResult {
        return try {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, "$fileName.png")

            val finalBitmap = if (config.includeTitle && config.title != null) {
                addTitleToBitmap(bitmap, config)
            } else {
                bitmap
            }

            // Use 'use' extension for automatic stream closing
            FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, config.quality, outputStream)
            }

            // Recycle bitmap only if we created a new one
            if (finalBitmap != bitmap) {
                finalBitmap.recycle()
            }

            // Notify media scanner
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(file)
            appContext.sendBroadcast(intent)

            ExportResult.Success(file)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export to Downloads: ${e.message}")
        }
    }

    /**
     * Share chart image via system share dialog.
     *
     * @param bitmap The chart bitmap to share
     * @param fileName Temporary filename
     * @param config Export configuration
     */
    fun shareChart(
        bitmap: Bitmap,
        fileName: String = "chart",
        config: ExportConfig = ExportConfig()
    ) {
        try {
            val file = File(appContext.cacheDir, "$fileName.png")

            val finalBitmap = if (config.includeTitle && config.title != null) {
                addTitleToBitmap(bitmap, config)
            } else {
                bitmap
            }

            // Use 'use' extension for automatic stream closing
            FileOutputStream(file).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, config.quality, outputStream)
            }

            // Recycle bitmap only if we created a new one
            if (finalBitmap != bitmap) {
                finalBitmap.recycle()
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )

            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "image/png"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            appContext.startActivity(
                android.content.Intent.createChooser(shareIntent, "Share Chart")
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Add title to bitmap.
     */
    private fun addTitleToBitmap(bitmap: Bitmap, config: ExportConfig): Bitmap {
        val titleHeight = 100
        val newBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height + titleHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(newBitmap)

        // Draw background
        canvas.drawColor(config.backgroundColor.toArgb())

        // Draw title (with null safety)
        config.title?.let { title ->
            val paint = Paint().apply {
                color = config.titleColor.toArgb()
                textSize = config.titleTextSize
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER // Fully qualified to avoid ambiguity
            }

            val textBounds = Rect()
            paint.getTextBounds(title, 0, title.length, textBounds)

            canvas.drawText(
                title,
                bitmap.width / 2f,
                titleHeight / 2f + textBounds.height() / 2f,
                paint
            )
        }

        // Draw chart
        canvas.drawBitmap(bitmap, 0f, titleHeight.toFloat(), null)

        // Recycle original bitmap since we created a copy
        bitmap.recycle()

        return newBitmap
    }
}

/**
 * Create a bitmap from a Composable chart.
 *
 * @param size Size of the bitmap
 * @param backgroundColor Background color
 * @param content The composable chart to capture
 */
@Composable
fun rememberChartBitmap(
    size: IntSize,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
): Bitmap? {
    val context = LocalContext.current

    return remember(size, content) {
        try {
            val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // This is a simplified version
            // In production, you'd use androidx.compose.ui.graphics.Canvas
            canvas.drawColor(backgroundColor.toArgb())

            bitmap
        } catch (e: Exception) {
            null
        }
    }
}

