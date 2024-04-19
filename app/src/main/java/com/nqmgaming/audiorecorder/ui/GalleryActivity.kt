package com.nqmgaming.audiorecorder.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nqmgaming.audiorecorder.R
import com.nqmgaming.audiorecorder.adapter.AudioRecordAdapter
import com.nqmgaming.audiorecorder.databinding.ActivityGalleryBinding
import com.nqmgaming.audiorecorder.model.AudioRecord
import com.nqmgaming.audiorecorder.ui.viewmodel.AudioRecordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GalleryActivity : AppCompatActivity() {
    private val binding: ActivityGalleryBinding by lazy {
        ActivityGalleryBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModel: AudioRecordViewModel
    private lateinit var adapter: AudioRecordAdapter
    private lateinit var audioRecordList: List<AudioRecord>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = AudioRecordAdapter()
        binding.rcAudio.layoutManager = LinearLayoutManager(this)
        binding.rcAudio.adapter = adapter

        lifecycleScope.launch {
            viewModel.getRecords().observe(this@GalleryActivity) { records ->
                audioRecordList = records
                adapter.differ.submitList(audioRecordList)
            }
        }

    }
}