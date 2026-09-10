package com.example.customkeyboard

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

class SoundManager(context: Context) {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isLoaded = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isLoaded = true
        }

        soundId = soundPool?.load(context, R.raw.key_click, 1) ?: 0
    }

    fun playKeyClick(volume: Float = 0.7f) {
        if (isLoaded) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
