package com.blackdiamond.musicplayer.services

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.view.KeyEvent.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.blackdiamond.musicplayer.App.Companion.CHANNEL_ID
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.activities.MainActivity
import com.blackdiamond.musicplayer.database.AudioDao
import com.blackdiamond.musicplayer.database.AudioDataBase
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.Songs

class MusicPlayerService : Service() {

    private val TAG = MusicPlayerService::class.java.simpleName

    private var player = MediaPlayer()
    private var audio: Audio? = null
    private var _audio: Audio? = null
    private var que: MutableList<Audio> = mutableListOf()
    private var last_pos: Int = -1
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var dao: AudioDao

    private var madeNotificationForPlaying = false

    companion object {
        val SERVICE_ID = 9
    }

    override fun onCreate() {
        super.onCreate()
        Thread {
            dao = AudioDataBase.getDataBase(this).dao()
        }.start()
        mediaSession = MediaSessionCompat(this, "MusicPlayerService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {

            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                val keyEvent =
                    mediaButtonEvent!!.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

                if (keyEvent != null) {
                    if (keyEvent.action == ACTION_UP) {
                        if (keyEvent.keyCode == KEYCODE_MEDIA_PREVIOUS) {
                            prev()
                        }
                        if (keyEvent.keyCode == KEYCODE_MEDIA_NEXT) {
                            skip()
                        }
                        if (keyEvent.keyCode == KEYCODE_MEDIA_PLAY_PAUSE) {
                            togglePlay()
                        }
                        if (keyEvent.keyCode == KEYCODE_MEDIA_PAUSE) {
                            togglePlay()
                        }
                        if (keyEvent.keyCode == KEYCODE_MEDIA_PLAY) {
                            togglePlay()
                        }
                    }
                }

                return true
            }

        })
        mediaSession.isActive = true
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val order = intent?.getStringExtra("order")
        if (order != null) {
            when (order) {
                "pause" -> {
                    togglePlay()
                }
                "lastSong" -> {
                    if (_audio != null) {
                        lastAudio()
                    } else {
                        noAudio()
                    }
                }
                "close" -> {
                    if (player.isPlaying) player.stop()
                    stopSelf()
                    onDestroy()
                }
                "skip" -> {
                    skip()
                }
                "prev" -> {
                    prev()
                }
                "fav" -> {
                    fav()
                }
            }
        }
        audio = intent?.getParcelableExtra("currentAudio") as? Audio
        if (audio != null) {
            val quee = intent?.getParcelableExtra<Songs>("quee")
            if (quee != null) {
                que = quee.songs
                last_pos = que.indexOf(que.filter { song -> song.songId == audio?.songId }[0])
            }
            changeAudio()
        }
        audio = intent?.getParcelableExtra("lastAudio") as? Audio
        if (audio != null) {
            _audio = audio
            val quee = intent?.getParcelableExtra<Songs>("quee")
            if (quee != null) {
                que = quee.songs
                last_pos = que.indexOf(que.filter { song -> song.songId == audio?.songId }[0])
            }
            lastAudio()
            player.reset()
            player.setDataSource(this, Uri.parse(audio!!.path))
            player.prepare()
        }
        return START_STICKY

    }

    private fun skip() {
        if (last_pos < que.size - 2) {
            audio = que[last_pos + 1]
            if (audio != null) {
                last_pos += 1
                _audio = audio
                changeAudio()
            }
        }
    }

    private fun prev() {
        if (last_pos > 0) {
            audio = que[last_pos - 1]
            if (audio != null) {
                last_pos -= 1
                _audio = audio
                changeAudio()
            }
        }
    }

    private fun fav() {
        _audio?.let {
            toggleFav(it)
        }
    }

    private fun changeAudio() {
        if (player.isPlaying) player.stop()
        songAdded()
        player.reset()
        player.setDataSource(this, Uri.parse(audio!!.path))
        player.prepare()
        player.start()
        Thread {
            while (player.isPlaying) {
                playing()
                Thread.sleep(1000)
            }
        }.start()
        player.setOnCompletionListener {
            stopped()
            skip()
        }
        _audio = audio
    }

    private fun togglePlay() {
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

    private fun playing() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "playing")
        })
        if (!madeNotificationForPlaying) {
            makeNotification(R.drawable.ic_pause)
            madeNotificationForPlaying = true
        }
    }

    private fun stopped() {
        sendBroadcast(Intent("playerStateChanged").also {
            it.putExtra("state", "paused")
        })
        makeNotification(R.drawable.ic_play)
        madeNotificationForPlaying = false
    }

    private fun songAdded() {
        sendBroadcast(Intent("songAdded").also {
            it.putExtra("addedAudio", audio)
            it.putExtra("que", que.map { audio -> audio.songId }.joinToString(separator = ","))
        })
        makeNotification()
    }

    private fun lastAudio() {
        sendBroadcast(Intent("lastAudio").also {
            it.putExtra("lastAudio", _audio)
        })
    }

    private fun noAudio() {
        sendBroadcast(Intent("noAudio"))
    }

    private fun toggleFav(audio: Audio) {
        audio.isFav = !audio.isFav
        sendBroadcast(Intent("toggleFav").also {
            it.putExtra("audio", audio)
        })
        makeNotification()
    }

    private fun makeNotification(playState: Int = R.drawable.ic_pause) {

        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), FLAG_MUTABLE
        )

        val togglePlay = PendingIntent.getService(
            this, 0,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "pause")
            }, FLAG_MUTABLE
        )

        val exit = PendingIntent.getService(
            this, 1,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "close")
            }, FLAG_MUTABLE
        )

        val skip = PendingIntent.getService(
            this, 2,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "skip")
            }, FLAG_MUTABLE
        )

        val prev = PendingIntent.getService(
            this, 3,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "prev")
            }, FLAG_MUTABLE
        )

        val fav = PendingIntent.getService(
            this, 4,
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "fav")
            }, FLAG_MUTABLE
        )

        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        var artUri = audio?.albumId?.let { ContentUris.withAppendedId(artworkUri, it) }

        val image = try {
            val inputStream = artUri?.let { contentResolver.openInputStream(it) }
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            BitmapFactory.decodeResource(resources, R.drawable.ic_music)
        }

        val favIcon = if (_audio?.isFav == true) {
            R.drawable.ic_fav
        } else {
            R.drawable.ic_fav_out_line
        }

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, _audio?.name)
                .putBitmap(MediaMetadata.METADATA_KEY_ART, image)
                .putLong(
                    MediaMetadata.METADATA_KEY_DURATION,
                    (_audio?.duration)?.toLong() ?: -1L
                )
                .build()
        )
        val state = if (playState == R.drawable.ic_pause) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, player.currentPosition.toLong(), 1f)
                .build()
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(_audio?.name)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setContentIntent(openApp)
            .setOnlyAlertOnce(true)
            .addAction(favIcon, "fav", fav)
            .addAction(R.drawable.ic_prev, "previous", prev)
            .addAction(playState, "play", togglePlay)
            .addAction(R.drawable.ic_skip, "skip", skip)
            .addAction(R.drawable.ic_close, "exit", exit)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
            .setVisibility(VISIBILITY_PUBLIC)
            .build()

        startForeground(SERVICE_ID, notification)
    }
}
