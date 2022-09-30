package com.blackdiamond.musicplayer.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.services.MusicPlayerService

class SongsAdapter(private val songs: MutableList<Audio>) :
    RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    var lastClicked = -1

    init {
        songs.add(Audio(-1, "", 0, null, ""))
    }

    class SongsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.song_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val song = songs[position]

        val songView = holder.itemView.findViewById<ConstraintLayout>(R.id.songView)
        val title = holder.itemView.findViewById<TextView>(R.id.audioTitle)
        val art = holder.itemView.findViewById<ImageView>(R.id.audioArt)
        val duration = holder.itemView.findViewById<TextView>(R.id.audioDuration)

        title.text = song.name

        if (lastClicked == position) {
            title.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_700))
            duration.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_200))
        } else {
            title.setTextColor(holder.itemView.context.resources.getColor(R.color.purple_500))
            duration.setTextColor(holder.itemView.context.resources.getColor(R.color.purple_700))
        }

        try {
            val inputStream =
                holder.itemView.context.contentResolver.openInputStream(Uri.parse(song.art))
            art.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            art.clearColorFilter()
        } catch (e: Exception) {
            if (song.songId == -1) {
                art.setImageBitmap(null)
                art.setColorFilter(Color.WHITE)
            } else {
                art.setImageResource(R.drawable.ic_music)
                val color = holder.itemView.resources.getColor(R.color.purple_700)
                art.setColorFilter(color)
            }
        }

        duration.text = if (song.songId != -1) formateDuration(song.duration) else ""

        songView.setOnClickListener {
            if (song.songId != -1) {
                val i = Intent(holder.itemView.context, MusicPlayerService::class.java)
                i.putExtra("currentAudio", song)
                holder.itemView.context.startService(i)
                title.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_700))
                duration.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_200))
                if (lastClicked != position) {
                    notifyItemChanged(lastClicked)
                    lastClicked = position
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return songs.size
    }

    private fun formateDuration(duration: Int): String {
        val hrs = String.format("%02d", (duration / 1000) / 60 / 60)
        val mins = String.format("%02d", (duration / 1000) / 60 % 60)
        val secs = String.format("%02d", (duration / 1000) % 60)
        return "${if (hrs != "00") "$hrs:" else ""}$mins:$secs"
    }
}