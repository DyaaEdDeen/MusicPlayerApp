package com.blackdiamond.musicplayer.activities

import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.adapters.ViewPagerAdapter
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.UserPref
import com.blackdiamond.musicplayer.services.MusicPlayerService
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    lateinit var audioViewModel: AudioViewModel
    lateinit var vpAdapter: ViewPagerAdapter

    private lateinit var tabLayout: TabLayout
    private lateinit var bottomControllerArt: ImageView
    private lateinit var bottomControllerTitle: TextView
    private lateinit var bottomControllerPlay: ImageView
    private lateinit var bottomControllerSkip: ImageView
    private lateinit var bottomControllerPrev: ImageView

    var folderView: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]

        registerReceiver(songAddedToMusicPlayer, IntentFilter("songAdded"))
        registerReceiver(playerStateChanged, IntentFilter("playerStateChanged"))
        registerReceiver(getLastAudio, IntentFilter("lastAudio"))
        registerReceiver(noAudio, IntentFilter("noAudio"))

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        bottomControllerArt = findViewById(R.id.currentAudioArt)
        bottomControllerTitle = findViewById(R.id.currentAudioTitle)
        bottomControllerPlay = findViewById(R.id.playAudio)
        bottomControllerSkip = findViewById(R.id.skipAudio)
        bottomControllerPrev = findViewById(R.id.prevAudio)


        val tabs = arrayOf("Songs", "Folders", "Playlists")

        getAudio().observe(this) {
            audioViewModel.getAllSongs().observe(this) { songs ->
                audioViewModel.getAllFolders().observe(this) { folders ->
                    audioViewModel.getAllPlaylists().observe(this) { playlists ->
                        vpAdapter =
                            ViewPagerAdapter(folders, songs, playlists, audioViewModel, this)
                        viewPager.adapter = vpAdapter
                        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                            tab.text = tabs[position]
                        }.attach()

                        startService(
                            Intent(
                                applicationContext,
                                MusicPlayerService::class.java
                            ).also {
                                it.putExtra("order", "lastSong")
                            })
                    }
                }
            }
        }

        bottomControllerArt.setColorFilter(resources.getColor(R.color.black))

        bottomControllerPlay.setOnClickListener {
            startService(Intent(applicationContext, MusicPlayerService::class.java).also {
                it.putExtra("order", "pause")
            })
        }

        bottomControllerSkip.setOnClickListener {
            startService(Intent(applicationContext, MusicPlayerService::class.java).also {
                it.putExtra("order", "skip")
            })
        }

        bottomControllerPrev.setOnClickListener {
            startService(Intent(applicationContext, MusicPlayerService::class.java).also {
                it.putExtra("order", "prev")
            })
        }

    }

    fun setFolderTabView(state: String, folderName: String = "") {
        folderView = state
        if (folderView == "folders") {
            tabLayout.getTabAt(1)?.text = "FOLDERS"
        } else {
            tabLayout.getTabAt(1)?.text = folderName
        }
    }

    override fun onBackPressed() {
        if (folderView.isBlank() || folderView == "folders") {
            super.onBackPressed()
        } else {
            vpAdapter.changeFolderView()
            tabLayout.selectTab(tabLayout.getTabAt(1))
        }
    }

    private val songAddedToMusicPlayer: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val audio = intent?.getParcelableExtra<Audio>("addedAudio")
            if (audio != null) {
                try {
                    val inputStream = contentResolver.openInputStream(Uri.parse(audio.art))
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                } catch (e: Exception) {
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerTitle.text = audio.name
                audioViewModel.addUserPref(UserPref("userPref", audio.songId.toLong(), ""))
            }
            val pos = intent?.getIntExtra("pos", -1)
            if (pos != null && pos != -1) {
                vpAdapter.notifySongsAdapterWithPos(pos)
            }
        }
    }

    private val playerStateChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state")
            if (state != null) {
                if (state == "paused") {
                    bottomControllerPlay.setImageResource(R.drawable.ic_play)
                } else {
                    bottomControllerPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    private val getLastAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val audio = intent?.getParcelableExtra<Audio>("lastAudio")
            if (audio != null) {
                try {
                    val inputStream = contentResolver.openInputStream(Uri.parse(audio.art))
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                } catch (e: Exception) {
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerTitle.text = audio.name
            }
            val pos = intent?.getIntExtra("pos", -1)
            if (pos != null && pos != -1) {
                vpAdapter.notifySongsAdapterWithPos(pos)
            }
        }
    }

    private val noAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            audioViewModel.getUserPref().observe(this@MainActivity) { userPref ->
                if (userPref != null) {
                    audioViewModel.getSongs(mutableListOf(userPref.last_played!!))
                        .observe(this@MainActivity) { songs ->
                            if (songs.isNotEmpty()) {
                                Toast.makeText(
                                    applicationContext,
                                    "${songs[0]}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startService(
                                    Intent(
                                        applicationContext,
                                        MusicPlayerService::class.java
                                    ).also {
                                        it.putExtra("lastAudio", songs[0])
                                    })
                            }
                        }
                }
            }
        }
    }

    private fun getAudio(): LiveData<Boolean> {
        var result = MutableLiveData<Boolean>()
        audioViewModel.getAllSongs().observe(this) { songs ->
            if (songs.isEmpty()) {
                loadSongs()
                createSongFolders().observe(this) {
                    result.postValue(true)
                }
            }
            result.postValue(true)
        }
        return result
    }

    private fun createSongFolders(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        audioViewModel.getAllSongs().observe(this) { songs ->
            val songFolders = mutableMapOf<String, MutableList<Long>>()
            for (song in songs) {
                val folder = song.path.split("/")[song.path.split("/").size - 2]
                if (songFolders.keys.contains(folder)) {
                    songFolders[folder]!!.add(song.songId.toLong())
                } else {
                    songFolders[folder] = mutableListOf()
                    songFolders[folder]!!.add(song.songId.toLong())
                }
            }
            for (key in songFolders.keys) {
                if (songFolders[key] != null) {
                    val folder = AudioFolder(key, songFolders[key]!!)
                    audioViewModel.addFolder(folder)
                }
            }
            result.postValue(true)
        }
        return result
    }

    private fun loadSongs() {

        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val sort = "${MediaStore.Audio.Media.DATE_MODIFIED} ASC"

        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sort
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumID =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                var art = ContentUris.withAppendedId(artworkUri, albumID)

                val ext = data.split(".").last()
                if (arrayOf("mp3", "m4a", "wav").contains(ext)) {
                    val audioFile = Audio(0, name, duration, art.toString(), data)
                    audioViewModel.addAudio(audioFile)
                }
            }
        }
    }


}