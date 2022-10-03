package com.blackdiamond.musicplayer.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_folder_table")
class AudioFolder(
    @PrimaryKey(autoGenerate = false)
    val folderName: String,
    var hasNew: Boolean = false,
    val audioFileIds: MutableList<Long>
){
    override fun toString(): String {
        return folderName
    }
}