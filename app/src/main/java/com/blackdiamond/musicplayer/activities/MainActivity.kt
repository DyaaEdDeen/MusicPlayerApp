package com.blackdiamond.musicplayer.activities

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.adapters.ViewPagerAdapter
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.services.MusicPlayerService
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    var paused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val (folders, songs) = getAudio()
        val vpAdapter = ViewPagerAdapter(folders, songs, null)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = vpAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val tabs = arrayOf("Songs", "Folders", "Playlists")

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        val play = findViewById<ImageView>(R.id.playAudio)

        play.setOnClickListener {
            Intent(this, MusicPlayerService::class.java).also {
                it.putExtra("order", "pause")
                startService(it)
            }
        }
    }

    private fun getAudio(): Pair<ArrayList<AudioFolder>, ArrayList<Audio>> {
        val audioFolders: ArrayList<AudioFolder> = ArrayList()
        val audioFiles: ArrayList<Audio> = ArrayList()
        val folderMap = mutableMapOf<String, ArrayList<Audio>>()

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
                } catch (e: Exception) {//this file has no art !
//                    Log.d("gettingAudio", "exception at : $e")
                }
                val ext = data.split(".").last()
                val folderName = data.split("/")[data.split("/").size - 2]
                if (arrayOf("mp3", "m4a", "wav").contains(ext)) {
                    val audioFile = Audio(name, duration, art, data)
                    if (folderMap.keys.contains(folderName)) {
                        folderMap[folderName]?.add(audioFile)
                    } else {
                        folderMap[folderName] = ArrayList()
                        folderMap[folderName]?.add(audioFile)
                    }
                    audioFiles.add(audioFile)
                }
            }
        }
        for (key in folderMap.keys) {
            val folder = folderMap[key]?.let { AudioFolder(key, it) }
            if (folder != null) {
                audioFolders.add(folder)
            }
        }
        return Pair(audioFolders, audioFiles)
    }


}