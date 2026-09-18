package com.example.customkeyboard

import android.content.Context
import android.media.AudioManager

class SoundManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun playKeyClick(volume: Float = 0.7f) {
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, volume)
    }

    fun release() {
        // No-op for AudioManager system sound effect
    }
}
