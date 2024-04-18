package com.nqmgaming.audiorecorder.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "audio_record")
data class AudioRecord(
    val fileName: String,
    val filePath: String,
    val duration: String,
    val size: String,
    val timestamp: Long,
    val ampsPath: String,
){
    @PrimaryKey(autoGenerate = true)
    var recordId: Int = 0
    @Ignore
    var isChecked: Boolean = false
}
