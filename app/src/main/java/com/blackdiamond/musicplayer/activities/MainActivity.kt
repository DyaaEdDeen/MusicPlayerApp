package com.blackdiamond.musicplayer.activities

import android.content.*
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
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
import com.blackdiamond.musicplayer.services.MusicPlayerService
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var audioViewModel: AudioViewModel
    private lateinit var musicPlayerServiceIntent: Intent

    private lateinit var bottomControllerArt: ImageView
    private lateinit var bottomControllerTitle: TextView
    private lateinit var bottomControllerPlay: ImageView
    private lateinit var bottomControllerSkip: ImageView
    private lateinit var bottomControllerPrev: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        musicPlayerServiceIntent = Intent(applicationContext,MusicPlayerService::class.java)

        registerReceiver(songAddedToMusicPlayer, IntentFilter("songAdded"))
        registerReceiver(playerStateChanged, IntentFilter("playerStateChanged"))
        registerReceiver(getLastAudio, IntentFilter("lastAudio"))

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

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
                        val vpAdapter = ViewPagerAdapter(folders, songs, playlists)
                        viewPager.adapter = vpAdapter
                        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                            tab.text = tabs[position]
                        }.attach()
                        musicPlayerServiceIntent.putExtra("order","lastSong")
                        startService(musicPlayerServiceIntent)
                    }
                }
            }
        }

        bottomControllerArt.setColorFilter(resources.getColor(R.color.black))

        bottomControllerPlay.setOnClickListener {
            musicPlayerServiceIntent.putExtra("order","pause")
            startService(musicPlayerServiceIntent)
        }

    }

    private val songAddedToMusicPlayer : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            val audio = intent?.getParcelableExtra<Audio>("addedAudio")
            if (audio != null){
                try {
                    val inputStream = contentResolver.openInputStream(Uri.parse(audio.art))
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                }catch (e: Exception){
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerPlay.setImageResource(R.drawable.ic_pause)
                bottomControllerTitle.text = audio.name
            }
        }
    }

    private val playerStateChanged : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("state")
            if (state != null){
                if (state == "paused"){
                    bottomControllerPlay.setImageResource(R.drawable.ic_play)
                }else{
                    bottomControllerPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    private val getLastAudio : BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, intent: Intent?) {
            val audio = intent?.getParcelableExtra<Audio>("lastAudio")
            if (audio != null){
                try {
                    val inputStream = contentResolver.openInputStream(Uri.parse(audio.art))
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                }catch (e: Exception){
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerTitle.text = audio.name
            }
        }
    }

    private fun getAudio(): LiveData<Boolean> {
        var result = MutableLiveData<Boolean>()
        audioViewModel.getAllSongs().observe(this) { songs ->
            if (songs.isEmpty()) {
                loadSongsForTheFirstTime()
                createSongFolders()
                result.postValue(true)
            }
            result.postValue(true)
        }
        return result
    }

    private fun createSongFolders() {
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
        }
    }

    private fun loadSongsForTheFirstTime() {

        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val sort = "${MediaStore.Audio.Media.TITLE} ASC"

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