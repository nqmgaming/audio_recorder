package com.nqmgaming.audiorecorder.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nqmgaming.audiorecorder.model.AudioRecord

@Dao
interface AudioRecordDao {

    // Get all records
    @Query("SELECT * FROM audio_record")
    fun getAllRecords(): LiveData<List<AudioRecord>>

    // Insert a record
    @Insert
    suspend fun insertRecord(audioRecord: AudioRecord): Long

    // Delete a record
    @Delete
    suspend fun deleteRecord(audioRecord: AudioRecord): Int

    // Delete multiple records
    @Delete
    suspend fun deleteRecords(audioRecords: List<AudioRecord>): Int

    // Delete all records
    @Query("DELETE FROM audio_record")
    suspend fun deleteAllRecords(): Int

    // Update a record
    @Update
    suspend fun updateRecord(audioRecord: AudioRecord): Int
}