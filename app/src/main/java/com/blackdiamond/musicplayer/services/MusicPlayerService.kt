package com.blackdiamond.musicplayer.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.blackdiamond.musicplayer.App.Companion.CHANNEL_ID
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.activities.MainActivity
import com.blackdiamond.musicplayer.dataclasses.Audio

class MusicPlayerService : Service() {

    private var player = MediaPlayer()
    private var audio: Audio? = null
    private var _audio: Audio? = null
    lateinit var mediaSession: MediaSessionCompat

    companion object {
        val SERVICE_ID = 9
    }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "tag")
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Thread {
            Looper.prepare()
            val order = intent?.getStringExtra("order")
            if (order != null) {
                when (order) {
                    "pause" -> {
                        if (player.isPlaying) {
                            player.pause()
                            stopped()
                        } else {
                            player.start()
                            Thread {
                                while (player.isPlaying) {
                                    playing()
                                    Thread.sleep(1000)
                                }
                                stopped()
                            }.start()
                        }
                    }
                    "lastSong" -> {
                        if (_audio != null) {
                            lastAudio()
                        }
                    }
                    "close" -> {
                        stopSelf()
                        onDestroy()
                    }
                }
            }
            audio = intent?.getParcelableExtra("currentAudio") as? Audio
            if (audio != null) {
                if (player.isPlaying) player.stop()
                player = MediaPlayer.create(applicationContext, Uri.parse(audio!!.path))
                player.start()
                songAdded()
                playing()
                _audio = audio
                makeNotification()
            }
        }.start()
        return START_STICKY

    }

    private fun playing() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "playing")
        })
        makeNotification(R.drawable.ic_pause)
    }

    private fun stopped() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "paused")
        })
        makeNotification(R.drawable.ic_play)
    }

    private fun songAdded() {
        sendBroadcast(Intent("songAdded").also {
            it.putExtra("addedAudio", audio)
        })
    }

    private fun lastAudio() {
        sendBroadcast(Intent("lastAudio").also {
            it.putExtra("lastAudio", _audio)
        })
    }

    private fun makeNotification(playState: Int = R.drawable.ic_pause) {

        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), 0
        )

        val togglePlay = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "pause")
            }, 0
        )

        val exit = PendingIntent.getService(
            this, 1,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order","close")
            }, 0
        )

        val image = try {
            val inputStream = contentResolver.openInputStream(Uri.parse(_audio?.art))
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            BitmapFactory.decodeResource(resources, R.drawable.ic_music)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(_audio?.name)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setContentIntent(openApp)
            .addAction(R.drawable.ic_fav_out_line, "fav", null)
            .addAction(R.drawable.ic_prev, "previous", null)
            .addAction(playState, "play", togglePlay)
            .addAction(R.drawable.ic_skip, "skip", null)
            .addAction(R.drawable.ic_close, "exit", exit)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
            .build()

        startForeground(SERVICE_ID, notification)
    }
}