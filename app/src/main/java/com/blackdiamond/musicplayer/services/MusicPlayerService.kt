package com.blackdiamond.musicplayer.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import com.blackdiamond.musicplayer.dataclasses.Audio

class MusicPlayerService : Service() {

    private var player = MediaPlayer()

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val order = intent?.getStringExtra("order")
        val audio = intent?.getParcelableExtra("currentAudio") as? Audio
        if (audio != null) {
            if (player.isPlaying) player.stop()
            player = MediaPlayer.create(applicationContext,Uri.parse(audio.path))
            player.start()
        }
        if (order != null){
            when(order){
                "pause" ->{
                    if (player.isPlaying){
                        player.pause()
                    }else{
                        player.start()
                    }
                }
            }
        }
        return START_STICKY
    }
}