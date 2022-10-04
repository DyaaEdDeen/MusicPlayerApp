package com.blackdiamond.musicplayer.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.activities.MainActivity
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewPagerAdapter(
    var folders: MutableList<AudioFolder>,
    var songs: MutableList<Audio>,
    var playlists: MutableList<PlayList>,
    var audioViewModel: AudioViewModel,
    var parent: MainActivity
) : RecyclerView.Adapter<ViewPagerAdapter.PagesViewHolder>() {

    var folderSongs = mutableListOf<Audio>()
    var plistSongs = mutableListOf<Audio>()
    var folderView = "folders"
    var plistView = "plists"
    var last_pos = 0
    val TAG = ViewPagerAdapter::class.java.simpleName

    var songsAdapter = SongsAdapter(songs, this)
    val foldersAdapter = FolderAdapter(folders, this)
    var playListAdapter = PlayListAdapter(playlists, this)

    class PagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagesViewHolder {
        return PagesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.page_view_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PagesViewHolder, position: Int) {
        val recyclerView = holder.itemView.findViewById<RecyclerView>(R.id.rvPages)
        when (position) {
            0 -> {
                songsAdapter = SongsAdapter(songs, this)
                recyclerView.adapter = songsAdapter
            }
            1 -> {
                if (folderView == "folderSongs") {
                    songsAdapter = SongsAdapter(folderSongs, this)
                    recyclerView.adapter = songsAdapter
                } else {
                    recyclerView.adapter = foldersAdapter
                }
            }
            2 -> {
                if (plistView == "plistSongs") {
                    songsAdapter = SongsAdapter(plistSongs, this)
                    recyclerView.adapter = songsAdapter
                } else {
                    recyclerView.adapter = playListAdapter
                }
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

    }

    fun notifySongsAdapterWithPos(audio: Audio,selected:Boolean = true) {
        CoroutineScope(Dispatchers.Main).launch {
            songsAdapter.songChanged(audio,selected)
        }
    }

    fun changeFolderView(
        _folderSongs: MutableList<Audio> = mutableListOf(),
        folderName: String = ""
    ) {
        folderSongs = _folderSongs
        folderView = if (folderSongs.isEmpty()) {
            "folders"
        } else {
            "folderSongs"
        }
        Log.e(TAG, "state changed : $folderView")
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(1)
            parent.setFolderTabView(folderView, folderName)
        }
    }

    fun changePlistView(_plistSongs: MutableList<Audio> = mutableListOf(), plistName: String = "") {
        plistSongs = _plistSongs
        plistView = if (plistView.isEmpty()) {
            "plist"
        } else {
            "plistSongs"
        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(2)
            parent.setPlistTabView(plistView, plistName)
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    fun updateSongs() {
        if (last_pos == 0 || last_pos == -1) {
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(0)
        }
    }
}