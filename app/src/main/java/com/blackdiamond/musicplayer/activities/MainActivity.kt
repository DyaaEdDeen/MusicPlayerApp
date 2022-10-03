package com.blackdiamond.musicplayer.activities

import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.adapters.ViewPagerAdapter
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.*
import com.blackdiamond.musicplayer.services.MusicPlayerService
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {

    lateinit var audioViewModel: AudioViewModel
    lateinit var vpAdapter: ViewPagerAdapter
    val TAG = MainActivity::class.java.simpleName

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomControllerArt: ImageView
    private lateinit var bottomControllerTitle: TextView
    private lateinit var bottomControllerPlay: ImageView
    private lateinit var bottomControllerSkip: ImageView
    private lateinit var bottomControllerPrev: ImageView

    val tabs = arrayOf("Songs", "Folders", "Playlists")

    var folderView: String = "folders"
    var plistView: String = "plists"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioViewModel = ViewModelProvider(this)[AudioViewModel::class.java]


        registerReceiver(songAddedToMusicPlayer, IntentFilter("songAdded"))
        registerReceiver(playerStateChanged, IntentFilter("playerStateChanged"))
        registerReceiver(getLastAudio, IntentFilter("lastAudio"))
        registerReceiver(noAudio, IntentFilter("noAudio"))
        registerReceiver(toggleFav, IntentFilter("toggleFav"))

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        bottomControllerArt = findViewById(R.id.currentAudioArt)
        bottomControllerTitle = findViewById(R.id.currentAudioTitle)
        bottomControllerPlay = findViewById(R.id.playAudio)
        bottomControllerSkip = findViewById(R.id.skipAudio)
        bottomControllerPrev = findViewById(R.id.prevAudio)

        //Getting Audio from device:
        getAudio(viewPager, tabs)

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
            folderView = folderName
        }
    }

    fun setPlistTabView(state: String, plistName: String) {
        plistView = state
        if (plistView == "plist") {
            tabLayout.getTabAt(2)?.text = "PLAYLISTS"
        } else {
            tabLayout.getTabAt(2)?.text = plistName
            plistView = plistName
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
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                var artUri = ContentUris.withAppendedId(artworkUri, audio.albumId)
                try {
                    val inputStream = contentResolver.openInputStream(artUri)
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                } catch (e: Exception) {
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerTitle.text = audio.name
                val que = intent.getStringExtra("que") ?: ""
                audioViewModel.addUserPref(UserPref("userPref", audio.songId.toLong(), que))
                vpAdapter.notifySongsAdapterWithPos(audio)
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
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                var artUri = ContentUris.withAppendedId(artworkUri, audio.albumId)
                try {
                    val inputStream = contentResolver.openInputStream(artUri)
                    bottomControllerArt.setImageBitmap(BitmapFactory.decodeStream(inputStream))
                    bottomControllerArt.clearColorFilter()
                } catch (e: Exception) {
                    bottomControllerArt.setImageResource(R.drawable.ic_music)
                    bottomControllerArt.setColorFilter(resources.getColor(R.color.black))
                }
                bottomControllerTitle.text = audio.name
                vpAdapter.notifySongsAdapterWithPos(audio)
            }
        }
    }

    private val noAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            CoroutineScope(Dispatchers.Default).launch {
                audioViewModel.getUserPref().collect { userPref ->
                    if (userPref != null) {
                        audioViewModel.getSong(userPref.last_played!!)
                            .collect { song ->
                                if (song != null) {
                                    startService(
                                        Intent(
                                            applicationContext,
                                            MusicPlayerService::class.java
                                        ).also {
                                            it.putExtra("lastAudio", song)
                                            runBlocking {
                                                audioViewModel.getSongs(getIdsFromString(userPref.last_quee))
                                                    .collect { songs ->
                                                        if (songs.isNotEmpty()) {
                                                            it.putExtra("quee", Songs(songs))
                                                        }
                                                    }
                                            }
                                        })
                                }
                            }
                    } else {
                        audioViewModel.getAllSongs().collect { songs ->
                            if (songs.isNotEmpty()) {
                                startService(
                                    Intent(
                                        applicationContext,
                                        MusicPlayerService::class.java
                                    ).also {
                                        it.putExtra("lastAudio", songs[0])
                                        it.putExtra("quee", Songs(songs))
                                    })
                            }
                        }
                    }
                }
            }
        }
    }

    private val toggleFav: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val audio = intent?.getParcelableExtra<Audio>("audio")
            if (audio != null) {
                audioViewModel.updateSong(audio)
                audioViewModel.addFavsToFavPlayList()
                vpAdapter.notifySongsAdapterWithPos(audio)
                vpAdapter.notifyItemChanged(2)
            }
        }
    }

    private fun getIdsFromString(string: String) =
        string.split(",").map { s -> s.toLong() } as MutableList<Long>


    private fun getAudio(viewPager: ViewPager2, tabs: Array<String>) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.e(TAG, "getting audios...")
            var songs = mutableListOf<Audio>()
            var folders = mutableListOf<AudioFolder>()
            var playlists = mutableListOf<PlayList>()


            runBlocking {
                loadSongs()
            }

            audioViewModel.getAllSongs().collect {
                songs = it
            }
            audioViewModel.getAllFolders().collect {
                folders = it
            }
            audioViewModel.getAllPlaylists().collect {
                playlists = it
            }
            Log.e(TAG, "finished getting audios")
            Log.e(TAG, "got list of audios with size of ${songs.size}")
            Log.e(TAG, "got list of folders with size of ${folders.size}")
            Log.e(TAG, "got list of playlists with size of ${playlists.size}")

            vpAdapter =
                ViewPagerAdapter(folders, songs, playlists, audioViewModel, this@MainActivity)

            runOnUiThread {
                viewPager.adapter = vpAdapter
                TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                    if (position != 1 || (position == 1 && folderView == "folders")) {
                        tab.text = tabs[position]
                    } else {
                        tab.text = folderView
                    }
                    if (position != 2 || (position == 2 && plistView == "plists")) {
                        tab.text = tabs[position]
                    } else {
                        tab.text = plistView
                    }
                }.attach()

                tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        vpAdapter.last_pos = tab?.position ?: -1
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                    }
                })
            }
            startService(
                Intent(
                    applicationContext,
                    MusicPlayerService::class.java
                ).also {
                    it.putExtra("order", "lastSong")
                })
        }
    }

    private fun loadSongs() {

        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val sort = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

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

                val ext = data.split(".").last()
                val folderName = getFolderName(data)
                if (arrayOf("mp3", "m4a", "wav").contains(ext)) {
                    val audioFile = Audio(0, name, duration, albumID, data, false)
                    runBlocking {
                        var audioFileId: Long = 0
                        audioViewModel.getSong(data).collect { audio ->
                            if (audio == null) {
                                runBlocking {
                                    audioViewModel.addAudio(audioFile).collect { id ->
                                        audioFileId = id
                                    }
                                }
                                runBlocking {
                                    audioViewModel.getFolder(folderName).collect { folder ->
                                        if (folder == null) {
                                            val newFolder =
                                                AudioFolder(
                                                    folderName,
                                                    true,
                                                    mutableListOf(audioFileId)
                                                )
                                            audioViewModel.addFolder(newFolder)
                                        } else {
                                            folder.audioFileIds.add(audioFileId)
                                            folder.hasNew = true
                                            audioViewModel.addFolder(folder)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getFolderName(path: String) = path.split("/")[path.split("/").size - 2]


}