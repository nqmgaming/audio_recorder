package com.nqmgaming.audiorecorder.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nqmgaming.audiorecorder.model.AudioRecord
import com.nqmgaming.audiorecorder.repository.AudioRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioRecordViewModel @Inject constructor(
    private val repository: AudioRecordRepository
) : ViewModel() {

    private val records: MutableLiveData<List<AudioRecord>> = MutableLiveData()

    init {
        viewModelScope.launch {
            getAllRecords()
        }
    }

    private suspend fun getAllRecords() {
        repository.getAllRecords().observeForever { recordsList ->
            records.postValue(recordsList)
        }
    }

    fun insertRecord(audioRecord: AudioRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRecord(audioRecord)
        }
    }

    fun deleteRecord(audioRecord: AudioRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(audioRecord)
        }
    }

    fun deleteRecords(audioRecords: List<AudioRecord>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecords(audioRecords)
        }
    }

    // Delete all records
    fun deleteAllRecords() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllRecords()
        }
    }

    fun updateRecord(audioRecord: AudioRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateRecord(audioRecord)
        }
    }

    fun getRecords(): LiveData<List<AudioRecord>> {
        return records
    }
}