package com.blackdiamond.musicplayer.adapters

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
    var playlists: MutableList<PlayList>?,
    var audioViewModel: AudioViewModel,
    var parent: MainActivity
) : RecyclerView.Adapter<ViewPagerAdapter.PagesViewHolder>() {

    var folderSongs = mutableListOf<Audio>()
    var folderView  = "folders"

    var songsAdapter = SongsAdapter(songs,this)
    val foldersAdapter = FolderAdapter(folders, this, audioViewModel)
    var playListAdapter = PlayListAdapter(playlists!!)

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
                songsAdapter = SongsAdapter(songs,this)
                recyclerView.adapter = songsAdapter
            }
            1 -> {
                if (folderView == "folderSongs") {
                    songsAdapter = SongsAdapter(folderSongs,this)
                    recyclerView.adapter = songsAdapter
                } else {
                    recyclerView.adapter = foldersAdapter
                }
            }
            2 -> {
                recyclerView.adapter = playListAdapter
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

    }

    fun notifySongsAdapterWithPos(audio: Audio){
        songsAdapter.songChanged(audio)
    }

    fun changeFolderView(_folderSongs: MutableList<Audio> = mutableListOf(),folderName:String = "") {
        folderSongs = _folderSongs
        folderView = if (folderSongs.isEmpty()){
            "folders"
        }else{
            "folderSongs"
        }
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(1)
        }
        parent.setFolderTabView(folderView,folderName)
    }

    override fun getItemCount(): Int {
        return 3
    }
}