package com.blackdiamond.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList

class ViewPagerAdapter(
    var folders: ArrayList<AudioFolder>,
    var songs: ArrayList<Audio>,
    var playlists: ArrayList<PlayList>?
) : RecyclerView.Adapter<ViewPagerAdapter.PagesViewHolder>() {

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
        val songsAdapter = SongsAdapter(songs)

        when (position) {
            0 -> {
                recyclerView.adapter = songsAdapter
                recyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
            }
            1 -> {
                Toast.makeText(holder.itemView.context, "We Are in Folders Tab!", Toast.LENGTH_SHORT)
                    .show()
            }
            2 -> {
                Toast.makeText(holder.itemView.context, "We Are in Playlist Tab!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}