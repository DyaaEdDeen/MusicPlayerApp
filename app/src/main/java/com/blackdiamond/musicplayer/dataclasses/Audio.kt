package com.blackdiamond.musicplayer.dataclasses

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Audio(
    val name: String,
    val duration: Int,
    val art: Uri?,
    val path: String
): Parcelable {
    override fun toString(): String {
        return "[$name,$duration,${if (art != null) "has art" else "doesn't have Art"},$path]"
    }
}