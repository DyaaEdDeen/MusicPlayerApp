package com.blackdiamond.musicplayer.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_table")
class PlayList (
    @PrimaryKey(autoGenerate = false)
    val name: String,
    var audioFileIds: MutableList<Long>
    ){

    fun addAudio(audioId: Long){
        audioFileIds.add(audioId)
    }

    fun removeAudio(audioId:Long){
        if (audioFileIds.contains(audioId)){
            audioFileIds.remove(audioId)
        }
    }
}