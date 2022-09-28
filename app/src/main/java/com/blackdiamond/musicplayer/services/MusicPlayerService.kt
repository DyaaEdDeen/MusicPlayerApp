package com.blackdiamond.musicplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.Looper
import com.blackdiamond.musicplayer.dataclasses.Audio

class MusicPlayerService : Service() {

    private var player = MediaPlayer()
    private var audio: Audio? = null
    private var _audio: Audio? = null

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
                            }.start()
                        }
                    }
                    "lastSong" -> {
                        if (_audio != null) {
                            lastAudio()
                        }
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
            }
        }.start()
        return START_STICKY

    }

    private fun playing() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "playing")
        })
    }

    private fun stopped() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "paused")
        })
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
}