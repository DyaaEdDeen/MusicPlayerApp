package com.blackdiamond.musicplayer.dataclasses

class PlayList (
    val name: String,
    var audioFiles: ArrayList<Audio>
    ){

    fun addAudio(audio: Audio){
        audioFiles.add(audio)
    }

    fun removeAudio(audio:Audio){
        if (audioFiles.contains(audio)){
            audioFiles.remove(audio)
        }
    }
}