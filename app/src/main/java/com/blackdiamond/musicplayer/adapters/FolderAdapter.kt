package com.blackdiamond.musicplayer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.AudioFolder

class FolderAdapter(private val folders: ArrayList<AudioFolder>) :
    RecyclerView.Adapter<FolderAdapter.FoldersViewHolder>() {

    class FoldersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersViewHolder {
        return FoldersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.folder_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FoldersViewHolder, position: Int) {
        val folder = folders[position]

        val name = holder.itemView.findViewById<TextView>(R.id.folderName)
        val songs = holder.itemView.findViewById<TextView>(R.id.folderSongs)

        name.text = folder.folderName


        songs.text = "${folder.audioFileIds.size} songs"
    }

    override fun getItemCount(): Int {
        return folders.size
    }
}