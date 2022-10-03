package com.blackdiamond.musicplayer.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.database.AudioViewModel
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderAdapter(
    private val folders: MutableList<AudioFolder>,
    private val parent: ViewPagerAdapter
) :
    RecyclerView.Adapter<FolderAdapter.FoldersViewHolder>() {

    init {
        val dummy = AudioFolder("", false,mutableListOf())
        if (folders.none { audio -> audio.folderName.isBlank()}){
            folders.add(dummy)
        }
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
        val new = holder.itemView.findViewById<ImageView>(R.id.folderNewTag)

        if (folder.hasNew) {
            new.visibility = VISIBLE
        } else {
            new.visibility = INVISIBLE
        }

        if (folder.folderName.isBlank()) {
            art.setImageBitmap(null)
            art.setColorFilter(Color.WHITE)
        }

        name.text = folder.folderName

        songs.text = if (folder.folderName.isNotBlank()) "${folder.audioFileIds.size} songs" else ""

        folderView.setOnClickListener {
            if (folder.folderName.isNotBlank()) {
                CoroutineScope(Dispatchers.Default).launch {
                    parent.audioViewModel.getSongs(folder.audioFileIds).collect { songs ->
                        parent.changeFolderView(songs, folder.folderName)
                    }
                    folder.hasNew = false
                    parent.audioViewModel.updateFolder(folder)
                    new.visibility = INVISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return folders.size
    }
}