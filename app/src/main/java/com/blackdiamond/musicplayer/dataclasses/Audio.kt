package com.blackdiamond.musicplayer.dataclasses

import android.graphics.Bitmap

class Audio (
    private val name: String,
    private val duration: Float,
    private val art: Bitmap?,
    private val path: String
){
    override fun toString(): String {
        return "[$name,$duration,$art,$path]"
    }
}