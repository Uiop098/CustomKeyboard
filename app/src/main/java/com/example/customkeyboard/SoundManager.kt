package com.example.customkeyboard

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

class SoundManager(context: Context) {
<<<<<<< Updated upstream
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playKeyClick() {
        // Plays standard IME sound
        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
=======
    private val soundPool: SoundPool
    private val clickSoundId: Int
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder().setMaxStreams(4).build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(4, AudioManager.STREAM_SYSTEM, 0)
        }
        
        // Ensure you have a res/raw/click_sound.mp3 or .ogg file in your project.
        // For fallback, use Android's default sound effect.
        clickSoundId = try {
            soundPool.load(context, R.raw.click_sound, 1)
        } catch(e: Exception) {
            0
        }
    }

    fun playKeyClick() {
        if (clickSoundId != 0) {
            val volume = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM).toFloat() / 
                         audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM).toFloat()
            soundPool.play(clickSoundId, volume, volume, 1, 0, 1f)
        } else {
            // Fallback to default system click
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
>>>>>>> Stashed changes
    }
}
