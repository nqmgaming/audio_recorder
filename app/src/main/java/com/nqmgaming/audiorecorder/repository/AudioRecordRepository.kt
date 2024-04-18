package com.nqmgaming.audiorecorder.repository

import com.nqmgaming.audiorecorder.database.AppDatabase
import com.nqmgaming.audiorecorder.model.AudioRecord

class AudioRecordRepository(
    val db: AppDatabase
) {
    // Get all records
    suspend fun getAllRecords() = db.audioRecordDao().getAllRecords()

    // Insert a record
    suspend fun insertRecord(vararg audioRecord: AudioRecord) =
        db.audioRecordDao().insertRecord(*audioRecord)

    // Delete a record
    suspend fun deleteRecord(audioRecord: AudioRecord) =
        db.audioRecordDao().deleteRecord(audioRecord)

    // Delete multiple records
    suspend fun deleteRecords(audioRecords: List<AudioRecord>) =
        db.audioRecordDao().deleteRecords(audioRecords)

    // Update a record
    suspend fun updateRecord(audioRecord: AudioRecord) =
        db.audioRecordDao().updateRecord(audioRecord)
}