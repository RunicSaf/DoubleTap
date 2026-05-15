package com.saf.doubletap

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class DoubletapKeepAliveService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        if (BindingStore.isKeepActiveEnabled(this)) {
            val restartIntent = Intent(applicationContext, DoubletapKeepAliveService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(restartIntent)
            } else {
                applicationContext.startService(restartIntent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "doubletap_keep_active"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Doubletap active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps hardware shortcut bindings available."
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setContentTitle("Doubletap is active")
            .setContentText("Hardware shortcut bindings are available.")
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 44
    }
}
