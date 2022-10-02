package com.blackdiamond.musicplayer.dataclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Songs(val songs: MutableList<Audio>) : Parcelable{
    override fun toString(): String {
        return songs.joinToString(separator = ",", prefix = "[", postfix = "]")
    }
}