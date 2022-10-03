package com.blackdiamond.musicplayer.adapters

import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blackdiamond.musicplayer.R
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.Songs
import com.blackdiamond.musicplayer.services.MusicPlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongsAdapter(private val songs: MutableList<Audio>, val parent: ViewPagerAdapter) :
    RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    val TAG = SongsAdapter::class.java.simpleName
    private var lastClicked = -1

    init {
        val dummy = Audio(-1, "", 0, 0, "", false)
        if (songs.none { audio -> audio.songId == -1 }) {
            songs.add(dummy)
        }
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
        val fav = holder.itemView.findViewById<ImageView>(R.id.audioFav)
        val more = holder.itemView.findViewById<ImageView>(R.id.audioMore)

        if (songs[holder.adapterPosition].songId == -1) {
            fav.visibility = GONE
            more.visibility = GONE
        }

        title.text = songs[holder.adapterPosition].name


        CoroutineScope(Dispatchers.Default).launch {

            var isFav = songs[holder.adapterPosition].isFav
            parent.audioViewModel.getSong(songs[holder.adapterPosition].songId.toLong()).collect {
                isFav = it?.isFav ?: false
            }

            val fav_icon = if (isFav) {
                R.drawable.ic_fav
            } else {
                R.drawable.ic_fav_out_line
            }
            fav.setImageResource(fav_icon)
            parent.updateSongs()
        }


        fav.setOnClickListener {
            if (songs[holder.adapterPosition].songId != -1) {
                songs[holder.adapterPosition].isFav = !songs[holder.adapterPosition].isFav
                val fav_icon = if (songs[holder.adapterPosition].isFav) {
                    R.drawable.ic_fav
                } else {
                    R.drawable.ic_fav_out_line
                }
                fav.setImageResource(fav_icon)
                holder.itemView.context.startService(
                    Intent(holder.itemView.context,MusicPlayerService::class.java).also {
                    it.putExtra("order","fav")
                })
            }

        }

        more.setOnClickListener {
            if (songs[holder.adapterPosition].songId != -1) {
                Toast.makeText(holder.itemView.context, "Clicked On More", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        if (lastClicked == holder.adapterPosition) {
            title.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_700))
            duration.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_200))
        } else {
            title.setTextColor(holder.itemView.context.resources.getColor(R.color.purple_500))
            duration.setTextColor(holder.itemView.context.resources.getColor(R.color.purple_700))
        }

        val artworkUri = Uri.parse("content://media/external/audio/albumart")
        var artUri = ContentUris.withAppendedId(artworkUri, songs[holder.adapterPosition].albumId)

        //check if this audio have album art !
        try {
            val inputStream = holder.itemView.context.contentResolver.openInputStream(artUri)
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
                val list = Songs(songs.filter { audio -> audio.songId != -1 } as MutableList<Audio>)
                i.putExtra("quee", list)
                i.putExtra("pos", holder.adapterPosition)
                holder.itemView.context.startService(i)
                title.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_700))
                duration.setTextColor(holder.itemView.context.resources.getColor(R.color.teal_200))
                if (lastClicked != holder.adapterPosition) {
                    notifyItemChanged(lastClicked)
                    lastClicked = holder.adapterPosition
                }
            }
        }

    }

    fun songChanged(audio: Audio) {
        if (songs.isNotEmpty() && songs.any { song -> song.songId == audio.songId }) {
            val last = lastClicked
            val pos = songs.indexOf(songs.filter { song -> song.songId == audio.songId }[0])
            lastClicked = pos
            notifyItemChanged(pos)
            notifyItemChanged(last)
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