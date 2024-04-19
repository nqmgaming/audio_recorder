package com.nqmgaming.audiorecorder.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nqmgaming.audiorecorder.repository.AudioRecordRepository

class AudioRecordViewModelFactory(
    private val repository: AudioRecordRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AudioRecordViewModel(repository) as T
    }
}