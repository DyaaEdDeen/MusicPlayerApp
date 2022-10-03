package com.blackdiamond.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.PlayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayListAdapter(private val playlists: MutableList<PlayList>,val parent: ViewPagerAdapter) :
    RecyclerView.Adapter<PlayListAdapter.PlaylistViewHolder>() {

    init {
        if (playlists.none { playList -> playList.name == "Favourites" }) {
            playlists.add(0, PlayList("Favourites", mutableListOf()))
        }
        val dummy = PlayList("", mutableListOf())
        if (playlists.none { playlist -> playlist.name.isBlank() }) {
            playlists.add(dummy)
        }
    }

    class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.playlist_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playList = playlists[position]

        val playListView = holder.itemView.findViewById<ConstraintLayout>(R.id.playListView)
        val name = holder.itemView.findViewById<TextView>(R.id.playListName)
        val songs = holder.itemView.findViewById<TextView>(R.id.playListSongs)
        val art = holder.itemView.findViewById<ImageView>(R.id.playListArt)

        if (playList.name.isBlank()) {
            art.setImageBitmap(null)
        }

        if (playList.name == "Favourites") {
            art.setImageResource(R.drawable.ic_fav)
        } else {
            if (playList.name.isBlank()) {
                art.setImageBitmap(null)
            } else {
                art.setImageResource(R.drawable.ic_play_list)
            }
        }

        name.text = playList.name

        songs.text = if (playList.name.isNotBlank()) "${playList.audioFileIds.size} songs" else ""

        playListView.setOnClickListener {
            if (playList.name.isNotBlank()) {
                CoroutineScope(Dispatchers.Default).launch {
                    parent.audioViewModel.getSongs(playList.audioFileIds).collect { songs ->
                        parent.changePlistView(songs, playList.name)
                    }
                    parent.audioViewModel.addPlayList(playList)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }
}