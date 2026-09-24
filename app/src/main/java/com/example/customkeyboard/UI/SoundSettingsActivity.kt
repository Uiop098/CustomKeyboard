package com.example.customkeyboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.customkeyboard.R
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.util.SoundManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SoundSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager
    private lateinit var tvVolume: TextView
    private lateinit var seekVolume: SeekBar
    private lateinit var btnTestSound: Button
    private lateinit var btnChooseSound: Button

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permission denied to read audio files", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            handleSelectedAudioFile(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_settings)

        prefs = Prefs(this)
        soundManager = SoundManager(this)

        setupViews()
    }

    private fun setupViews() {
        val switchSoundEnabled = findViewById<SwitchMaterial>(R.id.switch_sound_enabled)
        seekVolume = findViewById(R.id.seek_volume)
        tvVolume = findViewById(R.id.tv_volume_value)
        btnTestSound = findViewById(R.id.btn_test_sound)
        btnChooseSound = findViewById(R.id.btn_choose_sound)

        // Sound enabled switch
        switchSoundEnabled.isChecked = prefs.isSoundEnabled
        switchSoundEnabled.setOnCheckedChangeListener { _, isChecked ->
            prefs.isSoundEnabled = isChecked
            updateUIState(isChecked)
        }

        // Volume slider
        seekVolume.max = 100
        seekVolume.progress = prefs.soundVolume
        tvVolume.text = "${prefs.soundVolume}%"

        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    prefs.soundVolume = progress
                    tvVolume.text = "$progress%"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (prefs.isSoundEnabled) {
                    soundManager.playKeyClick()
                }
            }
        })

        // Test sound button
        btnTestSound.setOnClickListener {
            soundManager.playKeyClick()
        }

        // Choose custom sound button
        btnChooseSound.setOnClickListener {
            checkPermissionAndPickAudio()
        }

        // Back button
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }

        updateUIState(prefs.isSoundEnabled)
    }

    private fun updateUIState(enabled: Boolean) {
        seekVolume.isEnabled = enabled
        btnTestSound.isEnabled = enabled
        btnChooseSound.isEnabled = enabled
    }

    private fun checkPermissionAndPickAudio() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFilePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    openFilePicker()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openFilePicker() {
        try {
            pickAudioLauncher.launch(arrayOf("audio/*"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedAudioFile(uri: Uri) {
        try {
            // Take persistable permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            
            // Save the URI
            prefs.customSoundUri = uri.toString()
            
            // Reload sound
            soundManager.loadCustomSound(uri)
            
            Toast.makeText(this, "Custom sound loaded successfully", Toast.LENGTH_SHORT).show()
            
            // Test the sound
            soundManager.playKeyClick()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading sound: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
