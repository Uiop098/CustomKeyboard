package com.example.customkeyboard.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
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
import java.io.File

class SoundSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager
    private lateinit var tvVolume: TextView
    private lateinit var seekVolume: SeekBar
    private lateinit var btnTestSound: Button
    private lateinit var btnChooseSound: Button
    private lateinit var tvCustomSoundStatus: TextView
    private lateinit var rgSoundPresets: RadioGroup

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
        updateCustomSoundStatus()
    }

    private fun setupViews() {
        val switchSoundEnabled = findViewById<SwitchMaterial>(R.id.switch_sound_enabled)
        seekVolume = findViewById(R.id.seek_volume)
        tvVolume = findViewById(R.id.tv_volume_value)
        btnTestSound = findViewById(R.id.btn_test_sound)
        btnChooseSound = findViewById(R.id.btn_choose_sound)
        tvCustomSoundStatus = findViewById(R.id.tv_custom_sound_status)
        rgSoundPresets = findViewById(R.id.rg_sound_presets)

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

        // Sound Presets
        val currentPreset = SoundManager.SoundType.fromString(prefs.selectedSoundType)
        when (currentPreset) {
            SoundManager.SoundType.CLICK_MECHANICAL -> findViewById<RadioButton>(R.id.rb_sound_mechanical)?.isChecked = true
            SoundManager.SoundType.CLICK_TYPEWRITER -> findViewById<RadioButton>(R.id.rb_sound_typewriter)?.isChecked = true
            SoundManager.SoundType.CLICK_POP -> findViewById<RadioButton>(R.id.rb_sound_pop)?.isChecked = true
            SoundManager.SoundType.CLICK_WOOD -> findViewById<RadioButton>(R.id.rb_sound_wood)?.isChecked = true
            SoundManager.SoundType.CLICK_SOFT -> findViewById<RadioButton>(R.id.rb_sound_soft)?.isChecked = true
            SoundManager.SoundType.SYSTEM_DEFAULT -> findViewById<RadioButton>(R.id.rb_sound_system)?.isChecked = true
            SoundManager.SoundType.CUSTOM_AUDIO -> findViewById<RadioButton>(R.id.rb_sound_custom)?.isChecked = true
            else -> findViewById<RadioButton>(R.id.rb_sound_mechanical)?.isChecked = true
        }

        rgSoundPresets.setOnCheckedChangeListener { _, checkedId ->
            val soundType = when (checkedId) {
                R.id.rb_sound_mechanical -> SoundManager.SoundType.CLICK_MECHANICAL
                R.id.rb_sound_typewriter -> SoundManager.SoundType.CLICK_TYPEWRITER
                R.id.rb_sound_pop -> SoundManager.SoundType.CLICK_POP
                R.id.rb_sound_wood -> SoundManager.SoundType.CLICK_WOOD
                R.id.rb_sound_soft -> SoundManager.SoundType.CLICK_SOFT
                R.id.rb_sound_system -> SoundManager.SoundType.SYSTEM_DEFAULT
                R.id.rb_sound_custom -> SoundManager.SoundType.CUSTOM_AUDIO
                else -> SoundManager.SoundType.CLICK_MECHANICAL
            }
            prefs.selectedSoundType = soundType.name
            soundManager.playKeyClick()
            Toast.makeText(this, "${soundType.displayName} selected", Toast.LENGTH_SHORT).show()
        }

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

    private fun updateCustomSoundStatus() {
        val customFile = File(filesDir, "custom_click_sound.bin")
        if (customFile.exists() && customFile.length() > 0) {
            tvCustomSoundStatus.text = "Custom audio active: (${customFile.length() / 1024} KB)"
            tvCustomSoundStatus.setTextColor(0xFF10B981.toInt())
        } else {
            tvCustomSoundStatus.text = "No custom audio file loaded"
            tvCustomSoundStatus.setTextColor(0xFF94A3B8.toInt())
        }
    }

    private fun updateUIState(enabled: Boolean) {
        seekVolume.isEnabled = enabled
        btnTestSound.isEnabled = enabled
        btnChooseSound.isEnabled = enabled
        for (i in 0 until rgSoundPresets.childCount) {
            rgSoundPresets.getChildAt(i).isEnabled = enabled
        }
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
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Not all providers support persistable flags; copy is stored in filesDir anyway
            }

            // Save and load the audio
            val success = soundManager.saveAndLoadCustomAudio(uri)
            if (success) {
                prefs.selectedSoundType = SoundManager.SoundType.CUSTOM_AUDIO.name
                findViewById<RadioButton>(R.id.rb_sound_custom)?.isChecked = true
                updateCustomSoundStatus()
                Toast.makeText(this, "Custom sound loaded successfully!", Toast.LENGTH_SHORT).show()
                soundManager.playKeyClick()
            } else {
                Toast.makeText(this, "Failed to load custom audio file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading sound: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
