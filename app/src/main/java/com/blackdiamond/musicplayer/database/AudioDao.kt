package com.blackdiamond.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList
import com.blackdiamond.musicplayer.dataclasses.UserPref

@Dao
interface AudioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addSong(song: Audio) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFolder(folder: AudioFolder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlaylist(playList: PlayList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserPref(userPref: UserPref)

    @Delete
    fun deleteSong(song: Audio)


    @Query("select * from audio_folder_table where folderName like :name")
    fun getFolder(name:String) : AudioFolder

    @Query("select * from songs_table order by songId asc")
    fun getAllSongs() : MutableList<Audio>

    @Query("select * from audio_folder_table order by folderName desc")
    fun getAllFolders() : MutableList<AudioFolder>

    @Query("select * from playlist_table")
    fun getAllPlayLists() : MutableList<PlayList>

    @Query("select * from songs_table where songId like :id")
    fun getSong(id : Long) : Audio

    @Query("select * from songs_table where path like :path")
    fun getSong(path : String) : Audio

    @Query("select * from user_pref where `key` like :key")
    fun getUserPref(key : String = "userPref") : UserPref


}