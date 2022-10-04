package com.blackdiamond.musicplayer.database

import androidx.room.*
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList
import com.blackdiamond.musicplayer.dataclasses.UserPref

@Dao
interface AudioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addSong(song: Audio): Long

    @Update
    fun updateSong(song: Audio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFolder(folder: AudioFolder)

    @Update
    fun updateFolder(folder: AudioFolder)

    @Update
    fun updatePlayList(playList: PlayList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlaylist(playList: PlayList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserPref(userPref: UserPref)

    @Delete
    fun deleteSong(song: Audio)


    @Query("select * from audio_folder_table where folderName like :name")
    fun getFolder(name: String): AudioFolder

    @Query("select * from songs_table order by songId asc")
    fun getAllSongs(): MutableList<Audio>

    @Query("select * from audio_folder_table order by folderName desc")
    fun getAllFolders(): MutableList<AudioFolder>

    @Query("select * from playlist_table")
    fun getAllPlayLists(): MutableList<PlayList>

    @Query("select * from songs_table where songId like :id")
    fun getSong(id: Long): Audio

    @Query("select * from songs_table where path like :path")
    fun getSong(path: String): Audio

    @Query("select * from songs_table where isFav like :fav")
    fun getFavs(fav: Boolean = true) : MutableList<Audio>

    @Query("select * from user_pref where `key` like :key")
    fun getUserPref(key: String = "userPref"): UserPref


}