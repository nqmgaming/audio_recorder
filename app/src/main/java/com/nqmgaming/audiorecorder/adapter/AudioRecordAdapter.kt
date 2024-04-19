package com.nqmgaming.audiorecorder.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nqmgaming.audiorecorder.databinding.ItemAudioRecordBinding
import com.nqmgaming.audiorecorder.model.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordAdapter : RecyclerView.Adapter<AudioRecordAdapter.AudioRecordViewHolder>() {
    inner class AudioRecordViewHolder(private val binding: ItemAudioRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            audioRecord: AudioRecord
        ) {
            Log.d("AudioRecordAdapter", "bind: $audioRecord")
            binding.tvFileName.text = audioRecord.fileName
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val date = Date(audioRecord.timestamp)
            val formattedDate = dateFormat.format(date)
            binding.tvMeta.text = "{${audioRecord.duration} - ${formattedDate}}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioRecordViewHolder {
        val binding =
            ItemAudioRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioRecordViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: AudioRecordViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            val audioRecord = differ.currentList[position]
            holder.bind(audioRecord)
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<AudioRecord>() {
        override fun areItemsTheSame(oldItem: AudioRecord, newItem: AudioRecord): Boolean {
            return oldItem.recordId == newItem.recordId
        }

        override fun areContentsTheSame(oldItem: AudioRecord, newItem: AudioRecord): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)
}