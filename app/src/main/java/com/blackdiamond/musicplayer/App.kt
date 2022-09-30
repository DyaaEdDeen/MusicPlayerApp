package com.blackdiamond.musicplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class App : Application() {


    companion object {
        val CHANNEL_ID = "musicPlayerServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Service",
                NotificationManager.IMPORTANCE_NONE
            )
            channel.setShowBadge(true)
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}