package com.blackdiamond.musicplayer.database

import android.app.Application
import android.content.Context
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

    fun getSongs(iDs: MutableList<Long>): LiveData<MutableList<Audio>>{
        var result = MutableLiveData<MutableList<Audio>>()
        viewModelScope.launch(Dispatchers.IO) {
            var audios = mutableListOf<Audio>()
            for (id in iDs){
                var audio = dao.getSong(id)
                audios.add(audio)
            }
            result.postValue(audios)
        }
        return result
    }

    fun getAllSongs(): LiveData<MutableList<Audio>>{
        var result = MutableLiveData<MutableList<Audio>>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(dao.getAllSongs())
        }
        return result
    }

    fun getAllFolders(): LiveData<MutableList<AudioFolder>>{
        var result = MutableLiveData<MutableList<AudioFolder>>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(dao.getAllFolders())
        }
        return result
    }

    fun getAllPlaylists(): LiveData<MutableList<PlayList>>{
        var result = MutableLiveData<MutableList<PlayList>>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(dao.getAllPlayLists())
        }
        return result
    }

    fun getContext(): Context {
        return getContext()
    }
}