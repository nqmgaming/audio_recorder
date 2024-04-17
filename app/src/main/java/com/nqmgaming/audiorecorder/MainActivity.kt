package com.nqmgaming.audiorecorder

import android.Manifest
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.coroutine.TedPermission
import com.nqmgaming.audiorecorder.databinding.ActivityMainBinding
import com.nqmgaming.audiorecorder.util.SharePreferencesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var permissionGranted = false
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
}