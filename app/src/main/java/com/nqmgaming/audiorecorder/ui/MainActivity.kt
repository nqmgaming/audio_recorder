package com.nqmgaming.audiorecorder.ui

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.coroutine.TedPermission
import com.nqmgaming.audiorecorder.R
import com.nqmgaming.audiorecorder.databinding.ActivityMainBinding
import com.nqmgaming.audiorecorder.util.SharePreferencesUtil
import com.nqmgaming.audiorecorder.util.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener {
    private lateinit var binding: ActivityMainBinding
    private var permissionGranted = false
    private lateinit var recoder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""
    private var isRecording = false
    private var isPause = false
    private val TAG = "MainActivityRecorder"
    private var duration = 0
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator

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
            // Alert dialog
            val dialog = AlertDialog.Builder(this)
                .setTitle("Discard recording")
                .setMessage("Are you sure you want to discard this recording?")
                .setPositiveButton("Yes") { _, _ ->
                    // Discard recording
                    recoder.stop()
                    recoder.release()
                    binding.waveformView.clear()
                    binding.tvTimer.text = "00:00"
                    binding.tvNameRecord.text = ""
                    binding.llSave.visibility = View.GONE
                    binding.llDiscard.visibility = View.GONE
                    binding.tvNameRecord.visibility = View.GONE
                    isRecording = false
                    isPause = false
                    timer.stop()
                    binding.btnStartRecord.setImageResource(R.drawable.ic_record)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }

        binding.btnSave.setOnClickListener {
            // Save recording
            recoder.stop()
            recoder.release()
            binding.waveformView.clear()
            binding.tvTimer.text = "00:00"
            binding.tvNameRecord.text = ""
            binding.llSave.visibility = View.GONE
            binding.llDiscard.visibility = View.GONE
            binding.tvNameRecord.visibility = View.GONE
            isRecording = false
            isPause = false
            timer.stop()
            binding.btnStartRecord.setImageResource(R.drawable.ic_record)
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show()
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

    // Stop recording
    private fun stopRecording() {
        recoder.stop()
        recoder.release()
        binding.btnStartRecord.setImageResource(R.drawable.ic_record)
    }

    private fun pauseRecording() {
        recoder.pause()
        isPause = true
        isRecording = false
        binding.btnStartRecord.setImageResource(R.drawable.ic_record)
        timer.pause()
    }

    private fun resumeRecording() {
        recoder.resume()
        isPause = false
        isRecording = true
        binding.btnStartRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun startRecording() {
        if (permissionGranted) {
            // Start recording
            recoder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
            dirPath = "${externalCacheDir?.absolutePath}/"
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            fileName = "recording${simpleDateFormat.format(System.currentTimeMillis())}.mp3"
            recoder.apply {
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
        binding.waveformView.addAmplitude(recoder.maxAmplitude.toFloat())
    }

    private fun showView() {
        binding.llSave.visibility = View.VISIBLE
        binding.llDiscard.visibility = View.VISIBLE
        binding.tvNameRecord.visibility = View.VISIBLE
    }

    private fun hideView() {
        binding.llSave.visibility = View.GONE
        binding.llDiscard.visibility = View.GONE
        binding.tvNameRecord.visibility = View.VISIBLE
    }
}