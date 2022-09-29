package com.blackdiamond.musicplayer.adapters

import android.graphics.Color
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder

class FolderAdapter(
    private val folders: MutableList<AudioFolder>,
    private val parent: ViewPagerAdapter,
    private val audioViewModel: AudioViewModel
    ) :
    RecyclerView.Adapter<FolderAdapter.FoldersViewHolder>() {

    init {
        folders.add(AudioFolder("", mutableListOf()))
    }

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

        val folderView = holder.itemView.findViewById<ConstraintLayout>(R.id.folderView)
        val name = holder.itemView.findViewById<TextView>(R.id.folderName)
        val songs = holder.itemView.findViewById<TextView>(R.id.folderSongs)
        val art = holder.itemView.findViewById<ImageView>(R.id.folderArt)

        if (folder.folderName.isBlank()){
            art.setImageBitmap(null)
            art.setColorFilter(Color.WHITE)
        }

        name.text = folder.folderName

        songs.text = if (folder.folderName.isNotBlank()) "${folder.audioFileIds.size} songs" else ""

        folderView.setOnClickListener {
            if (folder.folderName.isNotBlank()){
                audioViewModel.getSongs(folder.audioFileIds).observe(parent.parent){songs->
                    parent.changeFolderView(songs,folder.folderName)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }
}