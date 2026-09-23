package com.example.customkeyboard.ui

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.customkeyboard.R
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.util.SoundManager
import com.google.android.material.switchmaterial.SwitchMaterial

class SoundSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_settings)

        prefs = Prefs(this)
        soundManager = SoundManager(this)

        setupViews()
    }

    private fun setupViews() {
        val switchSoundEnabled = findViewById<SwitchMaterial>(R.id.switch_sound_enabled)
        val seekVolume = findViewById<SeekBar>(R.id.seek_volume)
        val tvVolume = findViewById<TextView>(R.id.tv_volume_value)
        val btnTestSound = findViewById<Button>(R.id.btn_test_sound)

        switchSoundEnabled.isChecked = prefs.isSoundEnabled
        switchSoundEnabled.setOnCheckedChangeListener { _, isChecked ->
            prefs.isSoundEnabled = isChecked
            seekVolume.isEnabled = isChecked
            btnTestSound.isEnabled = isChecked
        }

        seekVolume.max = 100
        seekVolume.progress = prefs.soundVolume
        seekVolume.isEnabled = prefs.isSoundEnabled
        tvVolume.text = "${prefs.soundVolume}%"

        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.soundVolume = progress
                tvVolume.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                soundManager.playKeyClick()
            }
        })

        btnTestSound.isEnabled = prefs.isSoundEnabled
        btnTestSound.setOnClickListener {
            soundManager.playKeyClick()
        }

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
