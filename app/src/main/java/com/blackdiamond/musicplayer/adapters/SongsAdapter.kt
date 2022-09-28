package com.blackdiamond.musicplayer.adapters

import android.content.ContentResolver
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.services.MusicPlayerService
import java.io.InputStream

class SongsAdapter(private val songs: ArrayList<Audio>) :
    RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

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
        if (song.art != null) {
            val inputStream = holder.itemView.context.contentResolver.openInputStream(song.art)
            art.setImageBitmap(BitmapFactory.decodeStream(inputStream))
            art.clearColorFilter()
        } else {
            art.setImageResource(R.drawable.ic_music)
            val color = holder.itemView.resources.getColor(R.color.purple_200)
            art.setColorFilter(color)
        }
        val hrs = String.format("%02d", (song.duration / 1000) / 60 / 60)
        val mins = String.format("%02d", (song.duration / 1000) / 60 % 60)
        val secs = String.format("%02d", (song.duration / 1000) % 60)
        duration.text = "${if (hrs != "00") "$hrs:" else ""}$mins:$secs"

        songView.setOnClickListener {
            val i = Intent(holder.itemView.context, MusicPlayerService::class.java)
            i.putExtra("currentAudio", song)
            holder.itemView.context.startService(i)
        }

    }

    override fun getItemCount(): Int {
        return songs.size
    }
}