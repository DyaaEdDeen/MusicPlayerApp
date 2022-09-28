package com.blackdiamond.musicplayer.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList

class ViewPagerAdapter(
    folders: MutableList<AudioFolder>,
    songs: MutableList<Audio>,
    playlists: MutableList<PlayList>?
) : RecyclerView.Adapter<ViewPagerAdapter.PagesViewHolder>() {

    val songsAdapter = SongsAdapter(songs)
    val foldersAdapter = FolderAdapter(folders)
    val playListAdapter = PlayListAdapter(playlists!!)

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
                recyclerView.adapter = songsAdapter
            }
            1 -> {
                recyclerView.adapter = foldersAdapter
            }
            2 -> {
                recyclerView.adapter = playListAdapter
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)

    }

    override fun getItemCount(): Int {
        return 3
    }
}