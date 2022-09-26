package com.blackdiamond.musicplayer.dataclasses

import android.graphics.Bitmap

class Audio (
    val name: String,
    val duration: Float,
    val art: Bitmap?,
    val path: String
){
    override fun toString(): String {
        return "[$name,$duration,$art,$path]"
    }
}