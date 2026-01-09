package com.superchart.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File

/**
 * Download notification manager for chart exports
 * Shows notifications when charts are exported with file location
 */
class DownloadNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "chart_export_channel"
    private val channelName = "Chart Exports"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for chart export operations"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show export progress notification
     */
    fun showExportProgress(fileName: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Exporting Chart")
            .setContentText("$fileName - $progress%")
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(PROGRESS_NOTIFICATION_ID, notification)
    }

    /**
     * Show export complete notification with file location and open action
     */
    fun showExportComplete(file: File, format: ExportFormat = ExportFormat.PNG) {
        // Cancel progress notification
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)

        // Create intent to open file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType(format))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Chart Exported Successfully")
            .setContentText("${file.name} saved")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("File saved to: ${file.absolutePath}\n\nTap to open"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Open",
                pendingIntent
            )
            .build()

        notificationManager.notify(SUCCESS_NOTIFICATION_ID, notification)
    }

    /**
     * Show export error notification
     */
    fun showExportError(fileName: String, error: String) {
        // Cancel progress notification
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Export Failed")
            .setContentText(fileName)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Failed to export $fileName\n\nError: $error"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    /**
     * Show export complete for Downloads folder with special message
     */
    fun showDownloadsExportComplete(file: File, format: ExportFormat = ExportFormat.PNG) {
        // Cancel progress notification
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID)

        // Create intent to open Downloads app
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            type = "resource/folder"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Chart Downloaded")
            .setContentText("${file.name} saved to Downloads")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("File: ${file.name}\nLocation: Downloads folder\n\nCheck your file manager or Downloads app"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(SUCCESS_NOTIFICATION_ID, notification)
    }

    private fun getMimeType(format: ExportFormat): String {
        return when (format) {
            ExportFormat.PNG -> "image/png"
            ExportFormat.PDF -> "application/pdf"
            is ExportFormat.Excel -> "application/vnd.ms-excel"
            is ExportFormat.CSV -> "text/csv"
            ExportFormat.SVG -> "image/svg+xml"
        }
    }

    companion object {
        private const val PROGRESS_NOTIFICATION_ID = 1001
        private const val SUCCESS_NOTIFICATION_ID = 1002
        private const val ERROR_NOTIFICATION_ID = 1003
    }
}

/**
 * Export format types
 */
sealed class ExportFormat {
    object PNG : ExportFormat()
    object PDF : ExportFormat()
    data class Excel(val includeData: Boolean = true) : ExportFormat()
    data class CSV(val separator: String = ",") : ExportFormat()
    object SVG : ExportFormat()
}

