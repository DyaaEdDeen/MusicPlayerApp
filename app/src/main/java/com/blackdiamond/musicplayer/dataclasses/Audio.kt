package com.blackdiamond.musicplayer.dataclasses

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "songs_table")
@Parcelize
class Audio(
    @PrimaryKey(autoGenerate = true)
    val songId: Int,
    val name: String,
    val duration: Int,
    val albumId: Long,
    val path: String
) : Parcelable {
    override fun toString(): String {
        return "[$name,$duration,$albumId,$path]"
    }
}