package com.blackdiamond.musicplayer.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList
import com.blackdiamond.musicplayer.dataclasses.UserPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: AudioDao
    private val TAG = AudioViewModel::class.java.simpleName

    init {
        dao = AudioDataBase.getDataBase(application).dao()
    }

    fun addAudio(audio: Audio): Flow<Long> {
        return flow {
            emit(dao.addSong(audio))
        }
    }

    fun updateSong(audio: Audio) {
        viewModelScope.launch(Dispatchers.Default) {
            dao.updateSong(audio)
        }
    }

    fun updateFolder(folder: AudioFolder) {
        viewModelScope.launch(Dispatchers.Default) {
            dao.updateFolder(folder)
        }
    }

    fun addFolder(folder: AudioFolder) {
        viewModelScope.launch(Dispatchers.Default) {
            dao.addFolder(folder)
        }
    }

    fun addPlayList(playList: PlayList) {
        viewModelScope.launch(Dispatchers.Default) {
            dao.addPlaylist(playList)
        }
    }

    fun addUserPref(userPref: UserPref) {
        viewModelScope.launch(Dispatchers.Default) {
            dao.addUserPref(userPref)
        }
    }

    fun getUserPref(key: String = "userPref"): Flow<UserPref?> {
        return flow {
            emit(dao.getUserPref(key))
        }
    }

    fun getSongs(iDs: MutableList<Long>): Flow<MutableList<Audio>> {
        return flow {
            var songs = mutableListOf<Audio>()
            for (id in iDs) {
                runBlocking {
                    val song = dao.getSong(id)
                    songs.add(song)
                }
            }
            emit(songs)
        }
    }

    fun getSong(path: String): Flow<Audio?> {
        return flow {
            emit(dao.getSong(path))
        }
    }

    fun getSong(id: Long): Flow<Audio?> {
        return flow {
            emit(dao.getSong(id))
        }
    }

    fun getFolder(name: String): Flow<AudioFolder?> {
        return flow {
            emit(dao.getFolder(name))
        }
    }

    fun getAllSongs(): Flow<MutableList<Audio>> {
        return flow {
            emit(dao.getAllSongs())
        }
    }

    fun getAllFolders(): Flow<MutableList<AudioFolder>> {
        return flow {
            emit(dao.getAllFolders())
        }
    }

    fun getAllPlaylists(): Flow<MutableList<PlayList>> {
        return flow {
            emit(dao.getAllPlayLists())
        }
    }

    fun addFavsToFavPlayList(audio: Audio) : Flow<Boolean> {
        return flow {
            var favs: MutableList<Audio>
            runBlocking {
                dao.updateSong(audio)
            }
            runBlocking {
                favs = dao.getFavs()
            }
            Log.e(TAG, "favs : $favs")
            val favPlayList = PlayList(
                "Favourites",
                favs.map { audio -> audio.songId.toLong() } as MutableList<Long>)
            runBlocking {
                dao.updatePlayList(favPlayList)
            }
            emit(true)
        }
    }
}