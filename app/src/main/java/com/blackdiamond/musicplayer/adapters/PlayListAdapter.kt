package com.blackdiamond.musicplayer.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.PlayList

class PlayListAdapter(private val playlists: MutableList<PlayList>) :
    RecyclerView.Adapter<PlayListAdapter.PlaylistViewHolder>() {

    init {
        if (playlists.isEmpty()) {
            playlists.add(PlayList("no playlists...", mutableListOf()))
        } else {
            playlists.add(PlayList("", mutableListOf()))
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

        val name = holder.itemView.findViewById<TextView>(R.id.playListName)
        val songs = holder.itemView.findViewById<TextView>(R.id.playListSongs)
        val art = holder.itemView.findViewById<ImageView>(R.id.playListArt)

        if (playList.name.isBlank()) {
            art.setImageBitmap(null)
            art.setColorFilter(Color.WHITE)
        }

        name.text = playList.name

        songs.text = if (playList.name.isNotBlank()) "${playList.audioFileIds.size} songs" else ""
    }

    override fun getItemCount(): Int {
        return playlists.size
    }
}