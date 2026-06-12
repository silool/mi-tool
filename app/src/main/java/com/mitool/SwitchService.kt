package com.mitool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class SwitchService : Service() {

    companion object {
        const val CHANNEL_ID = "mi_switch"
        const val NOTIFY_ID = 1001
        const val ACTION_DAILY = "com.mitool.SWITCH_DAILY"
        const val ACTION_GAME = "com.mitool.SWITCH_GAME"
    }

    private val helper by lazy { SettingsHelper(this) }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DAILY -> {
                helper.setDailyMode()
                updateNotification("☀️ 日常模式")
            }
            ACTION_GAME -> {
                helper.setGameMode()
                updateNotification("🎮 游戏模式")
            }
        }

        startForeground(NOTIFY_ID, buildNotification("小米快捷"))
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "模式切换", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "日常/游戏模式快速切换"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(label: String): Notification {
        val piDaily = PendingIntent.getService(
            this, 0,
            Intent(this, SwitchService::class.java).apply { action = ACTION_DAILY },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val piGame = PendingIntent.getService(
            this, 1,
            Intent(this, SwitchService::class.java).apply { action = ACTION_GAME },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val piOpen = PendingIntent.getActivity(
            this, 2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(label)
                .setContentText("点击下方按钮切换模式")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true)
                .setContentIntent(piOpen)
                .addAction(android.R.drawable.ic_media_play, "☀️ 日常", piDaily)
                .addAction(android.R.drawable.ic_media_pause, "🎮 游戏", piGame)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle(label)
                .setContentText("点击下方按钮切换模式")
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setOngoing(true)
                .setContentIntent(piOpen)
                .addAction(android.R.drawable.ic_media_play, "☀️ 日常", piDaily)
                .addAction(android.R.drawable.ic_media_pause, "🎮 游戏", piGame)
                .build()
        }
    }

    private fun updateNotification(label: String) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFY_ID, buildNotification(label))
    }
}
