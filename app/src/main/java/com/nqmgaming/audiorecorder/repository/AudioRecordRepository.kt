package com.nqmgaming.audiorecorder.repository

import android.util.Log
import com.nqmgaming.audiorecorder.database.AppDatabase
import com.nqmgaming.audiorecorder.model.AudioRecord
import javax.inject.Inject

class AudioRecordRepository @Inject constructor(
    val db: AppDatabase
) {
    // Get all records
    suspend fun getAllRecords() = db.audioRecordDao().getAllRecords()

    // Insert a record
    suspend fun insertRecord(audioRecord: AudioRecord) {
        val result = db.audioRecordDao().insertRecord(audioRecord)
        if (result == -1L) {
            throw Exception("Failed to insert record")
            Log.e("AudioRecordRepository", "Failed to insert record")
        }
    }

    // Delete a record
    suspend fun deleteRecord(audioRecord: AudioRecord) =
        db.audioRecordDao().deleteRecord(audioRecord)

    // Delete multiple records
    suspend fun deleteRecords(audioRecords: List<AudioRecord>) =
        db.audioRecordDao().deleteRecords(audioRecords)

    // Delete all records
    suspend fun deleteAllRecords() =
        db.audioRecordDao().deleteAllRecords()

    // Update a record
    suspend fun updateRecord(audioRecord: AudioRecord) =
        db.audioRecordDao().updateRecord(audioRecord)
}