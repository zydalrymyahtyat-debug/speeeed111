package com.example.internet_speed_meter

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.*
import androidx.core.app.NotificationCompat
import java.util.Locale

class SpeedMeterService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastRx = 0L
    private var lastTx = 0L
    private var lastTime = 0L
    private val channelId = "speed_meter_channel"
    private val notificationId = 1001

    private val updater = object : Runnable {
        override fun run() {
            updateSpeed()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(notificationId, buildNotification("جارٍ القياس...", "0 KB/s"))
        lastRx = TrafficStats.getTotalRxBytes()
        lastTx = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
        handler.post(updater)
    }

    private fun formatSpeed(speedKbps: Double): String {
        return if (speedKbps >= 1024) {
            String.format(Locale.US, "%.1f MB/s", speedKbps / 1024.0)
        } else {
            String.format(Locale.US, "%.1f KB/s", speedKbps)
        }
    }

    private fun updateSpeed() {
        val now = System.currentTimeMillis()
        val rx = TrafficStats.getTotalRxBytes()
        val tx = TrafficStats.getTotalTxBytes()
        val seconds = ((now - lastTime).coerceAtLeast(1L)).toDouble() / 1000.0
        val down = if (rx >= 0 && lastRx >= 0) ((rx - lastRx) / 1024.0) / seconds else 0.0
        val up = if (tx >= 0 && lastTx >= 0) ((tx - lastTx) / 1024.0) / seconds else 0.0
        lastRx = rx; lastTx = tx; lastTime = now

        val totalSpeed = down + up
        val speedText = formatSpeed(totalSpeed)
        val text = String.format(Locale.US, "↓ %s   ↑ %s", formatSpeed(down), formatSpeed(up))

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, buildNotification(text, speedText))

        // Send data back to flutter
        val intent = Intent("speedUpdate")
        intent.putExtra("download", down)
        intent.putExtra("upload", up)
        sendBroadcast(intent)
    }

    private fun buildNotification(text: String, speedText: String): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(speedText)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Max priority to keep it on top
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pending)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId, "عداد سرعة الإنترنت",
                NotificationManager.IMPORTANCE_HIGH // High importance
            )
            channel.description = "عرض سرعة نقل البيانات في شريط الإشعارات"
            channel.setShowBadge(false)
            channel.setSound(null, null)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        handler.removeCallbacks(updater)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
