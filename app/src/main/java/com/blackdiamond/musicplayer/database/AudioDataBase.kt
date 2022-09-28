package com.blackdiamond.musicplayer.database

import android.content.Context
import androidx.room.*
import com.blackdiamond.musicplayer.dataclasses.Audio
import com.blackdiamond.musicplayer.dataclasses.AudioFolder
import com.blackdiamond.musicplayer.dataclasses.PlayList

@Database(entities = [Audio::class, AudioFolder::class, PlayList::class], version = 1)
@TypeConverters(Converters::class)
abstract class AudioDataBase : RoomDatabase() {

    abstract fun dao(): AudioDao

    companion object {
        @Volatile
        private var INSTANCE: AudioDataBase? = null

        fun getDataBase(context: Context): AudioDataBase {
            val temp = INSTANCE
            if (temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioDataBase::class.java,
                    "audio_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }


}

class Converters {

    @TypeConverter
    fun fromList(value: MutableList<Long>) = value.joinToString(separator = "|")

    @TypeConverter
    fun toList(value: String): MutableList<Long> {
        var result = mutableListOf<Long>()
        for (l in value.split("|")) {
            result.add(l.toLong())
        }
        return result
    }
}

