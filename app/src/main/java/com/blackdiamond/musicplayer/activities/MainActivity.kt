package com.blackdiamond.musicplayer.activities

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder


class MainActivity : AppCompatActivity() {

    var paused = false
    lateinit var audioViewModel: AudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        getAudio()
//        val (folders, songs) = getAudio()
//
//        val vpAdapter = ViewPagerAdapter(folders, songs, null)
//        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
//        viewPager.adapter = vpAdapter
//
//        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
//
//        val tabs = arrayOf("Songs", "Folders", "Playlists")
//
//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            tab.text = tabs[position]
//        }.attach()
//
//        val play = findViewById<ImageView>(R.id.playAudio)
//
//        play.setOnClickListener {
//            Intent(this, MusicPlayerService::class.java).also {
//                it.putExtra("order", "pause")
//                startService(it)
//            }
//        }
    }

    private fun getAudio(){
        audioViewModel.getAllSongs().observe(this) { songs ->
            if (songs.isEmpty()) {
                loadSongsForTheFirstTime()
                createSongFolders()
            }
        }
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
                if (songFolders[key] != null){
                    val folder = AudioFolder(key,songFolders[key]!!)
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
                val uri = ContentUris.withAppendedId(artworkUri, albumID)
                var art: Uri? = null

                try {
                    contentResolver.openInputStream(uri)
                    art = uri
                } catch (e: Exception) {
                }//this file has no art !

                val ext = data.split(".").last()
                if (arrayOf("mp3", "m4a", "wav").contains(ext)) {
                    val audioFile = Audio(0, name, duration, art.toString(), data)
                    audioViewModel.addAudio(audioFile)
                }
            }
        }
    }


}