package com.example.customkeyboard

import android.content.Context
import android.media.AudioManager

class SoundManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playKeyClick() {
        // Plays standard IME sound
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
    }
}
