package com.nqmgaming.audiorecorder.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.nqmgaming.audiorecorder.database.dao.AudioRecordDao
import com.nqmgaming.audiorecorder.model.AudioRecord

@Database(entities = [AudioRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): AudioRecordDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "audio_record_db"
            ).build()
    }
}