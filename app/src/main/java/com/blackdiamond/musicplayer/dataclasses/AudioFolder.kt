package com.blackdiamond.musicplayer.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_folder_table")
class AudioFolder(
    @PrimaryKey(autoGenerate = false)
    val folderName: String,
    val audioFileIds: MutableList<Long>
)