package com.nqmgaming.audiorecorder.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gun0912.tedpermission.coroutine.TedPermission
import com.nqmgaming.audiorecorder.ui.view.ModalBottomSheet
import com.nqmgaming.audiorecorder.R
import com.nqmgaming.audiorecorder.databinding.ActivityMainBinding
import com.nqmgaming.audiorecorder.model.AudioRecord
import com.nqmgaming.audiorecorder.ui.viewmodel.AudioRecordViewModel
import com.nqmgaming.audiorecorder.util.OnNameChangedListener
import com.nqmgaming.audiorecorder.util.SharePreferencesUtil
import com.nqmgaming.audiorecorder.util.Timer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private lateinit var binding: ActivityMainBinding
    private var permissionGranted = false
    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var isRecording = false
    private var isPause = false
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private var duration = 0L

    @Inject
    lateinit var viewModel: AudioRecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        // get permission from shared preferences
        permissionGranted = SharePreferencesUtil.getBoolean(this, "permissionGranted", false)
        lifecycleScope.launch(Dispatchers.Main) {
            checkPermission()
        }

        timer = Timer(this)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        binding.btnDiscard.setOnClickListener {
            // Material Dialog
            MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Discard recording")
                .setMessage("Are you sure you want to discard this recording?")
                .setPositiveButton("Discard") { dialog, _ ->
                    stopRecording()

                    // Delete file
                    val file = File("$dirPath$fileName")
                    val result = file.delete()
                    if (result) {
                        Log.d("File", "File deleted successfully")
                    } else {
                        Log.d("File", "File not deleted")
                    }

                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }

        binding.btnSave.setOnClickListener {
            stopRecording()
            showRenameDialog()
        }

        binding.btnStartRecord.setOnClickListener {
            // Record audio
            when {
                isRecording -> {
                    // Pause recording
                    pauseRecording()
                }

                isPause -> {
                    // Resume recording
                    resumeRecording()
                }

                else -> {
                    // Start recording
                    startRecording()
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }

    }

    private fun stopRecording() {
        if (isRecording) {
            recorder.apply {
                stop()
                release()
            }
        }
        binding.apply {
            waveformView.clear()
            tvTimer.text = "00:00"
            tvNameRecord.text = ""
        }
        hideView()
        isRecording = false
        isPause = false
        timer.stop()
    }

    private fun showRenameDialog() {
        val modalBottomSheet = ModalBottomSheet().apply {
            arguments = Bundle().apply {
                putString("fileName", fileName)
                putString("dirPath", dirPath)
            }
            listener = object : OnNameChangedListener {
                override fun onNameChanged(newName: String) {
                    renameFile(newName)
                }
            }
        }
        modalBottomSheet.show(supportFragmentManager, "ModalBottomSheet")
        binding.btnStartRecord.setImageResource(R.drawable.ic_record)
    }

    private fun renameFile(newName: String) {
        var formattedName = newName
        if (!newName.endsWith(".mp3")) {
            formattedName += ".mp3"
        }

        val newFilePath = "$dirPath$formattedName"
        val oldFilePath = "$dirPath$fileName"
        val oldFile = File(oldFilePath)
        val newFile = File(newFilePath)
        if (oldFile.renameTo(newFile)) {
            Log.d("File", "File renamed successfully")
        } else {
            Log.d("File", "File not renamed")
        }

        binding.btnStartRecord.setImageResource(R.drawable.ic_record)

        val fileSize = newFile.length()
        val timestamp = System.currentTimeMillis()
        val ampsPath = "$dirPath$formattedName"
        Log.d("File", "File size: ${fileSize.toString()}")
        val audioRecord = AudioRecord(
            formattedName,
            newFilePath,
            duration.toString(),
            fileSize.toString(),
            timestamp,
            ampsPath
        )

        viewModel.insertRecord(audioRecord)

        Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        recorder.pause()
        isPause = true
        isRecording = false
        binding.btnStartRecord.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun resumeRecording() {
        recorder.resume()
        isPause = false
        isRecording = true
        binding.btnStartRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun startRecording() {
        if (permissionGranted) {
            // Start recording
            recorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
            dirPath = "${externalCacheDir?.absolutePath}/"
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            fileName = "recording${simpleDateFormat.format(System.currentTimeMillis())}.mp3"
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile("$dirPath$fileName")

                try {
                    prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                start()
            }

            isRecording = true
            isPause = false
            showView()
            binding.tvNameRecord.text = fileName
            timer.start()
            binding.btnStartRecord.setImageResource(R.drawable.ic_pause)

        } else {
            // Permission not granted
            lifecycleScope.launch(Dispatchers.Main) {
                checkPermission()
            }
        }
    }

    private suspend fun checkPermission() {
        if (permissionGranted) {
            // Permission granted
        } else {
            // Permission not granted
            val permissionResult = TedPermission.create()
                .setPermissions(Manifest.permission.RECORD_AUDIO)
                .check()
            permissionGranted = permissionResult.isGranted
            SharePreferencesUtil.saveBoolean(this, "permissionGranted", permissionGranted)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.text = duration
        binding.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
        this.duration = timer.getDurationInSeconds()
        val fileSize = getFileSize(this.duration)
        binding.tvFileProperties.text = "File size: $fileSize MB"
    }

    private fun showView() {
        binding.llSave.visibility = View.VISIBLE
        binding.llDiscard.visibility = View.VISIBLE
        binding.tvNameRecord.visibility = View.VISIBLE
        binding.ivSound.visibility = View.INVISIBLE
        binding.tvFileProperties.visibility = View.VISIBLE
    }

    private fun hideView() {
        binding.llSave.visibility = View.GONE
        binding.llDiscard.visibility = View.GONE
        binding.tvNameRecord.visibility = View.VISIBLE
        binding.ivSound.visibility = View.VISIBLE
        binding.tvFileProperties.visibility = View.GONE
    }

    private fun getFileSize(durationInSeconds: Long): Double {
        val bitRate = 128 // kbps
        val fileSizeInKb = durationInSeconds * bitRate
        val fileSizeInMb = fileSizeInKb.toDouble() / (1024 * 10)
        val roundedFileSizeInMb = String.format("%.1f", fileSizeInMb).toDouble()
        return roundedFileSizeInMb
    }
}