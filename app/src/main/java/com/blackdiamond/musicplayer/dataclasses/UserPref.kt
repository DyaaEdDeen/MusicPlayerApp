package com.blackdiamond.musicplayer.dataclasses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_pref")
class UserPref(
    @PrimaryKey(autoGenerate = false)
    val key:String = "userPref",
    val last_played: Long?,
    val last_quee: String = ""
){
    override fun toString(): String {
        return "[$key,$last_played,$last_quee]"
    }
}