package com.blackdiamond.musicplayer.activities

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.adapters.SongsAdapter
import com.blackdiamond.musicplayer.adapters.ViewPagerAdapter
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vpAdapter = ViewPagerAdapter(getAudio().first,getAudio().second,null)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = vpAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        val tabs = arrayOf("Songs","Folders","Playlists")

        TabLayoutMediator(tabLayout,viewPager){ tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun getAudio(): Pair<ArrayList<AudioFolder>,ArrayList<Audio>> {
        val audioFolders: ArrayList<AudioFolder> = ArrayList()
        val audioFiles: ArrayList<Audio> = ArrayList()

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
                    cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumID =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))

                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                val uri = ContentUris.withAppendedId(artworkUri, albumID)
                var art: Bitmap? = null

                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    art = BitmapFactory.decodeStream(inputStream)
                } catch (e: Exception) {//this file has no art !
//                    Log.d("gettingAudio", "exception at : $e")
                }
                val ext = data.split(".").last()
                if (arrayOf("mp3", "m4a", "wav").contains(ext)) {
                    val audioFile = Audio(name, duration, art, data)
                    Log.d("gettingAudio", "audio : $audioFile")
                    audioFiles.add(audioFile)
                }
            }
        }
        return Pair(audioFolders,audioFiles)
    }


}