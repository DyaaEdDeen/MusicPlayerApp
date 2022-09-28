package com.blackdiamond.musicplayer.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: AudioDao

    init {
        dao = AudioDataBase.getDataBase(application).dao()
    }

    fun addAudio(audio: Audio): LiveData<Long> {
        var result = MutableLiveData<Long>()
        viewModelScope.launch(Dispatchers.IO) {
            val id = dao.addSong(audio)
            result.postValue(id)
        }
        return result
    }

    fun addFolder (folder: AudioFolder){
        viewModelScope.launch(Dispatchers.IO) {
            dao.addFolder(folder)
        }
    }

    fun addPlayList(playList: PlayList){
        viewModelScope.launch(Dispatchers.IO) {
            dao.addPlaylist(playList)
        }
    }

    fun getAllSongs(): LiveData<MutableList<Audio>>{
        var result = MutableLiveData<MutableList<Audio>>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(dao.getAllSongs())
        }
        return result
    }
}