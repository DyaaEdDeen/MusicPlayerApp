package com.blackdiamond.musicplayer.activities

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getAudioFolders()
    }

    private fun getAudioFolders(): ArrayList<AudioFolder> {
        var audioFolders: ArrayList<AudioFolder> = ArrayList()
        var audioFiles: ArrayList<Audio> = ArrayList()

        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )


        applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
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
        return audioFolders
    }


}