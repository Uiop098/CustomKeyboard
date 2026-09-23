package com.example.customkeyboard.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.customkeyboard.data.Prefs

class SoundManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = Prefs(context)
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var isInitialized = false

    enum class SoundType {
        CLICK_MECHANICAL,
        CLICK_SOFT,
        CLICK_TYPEWRITER,
        CLICK_POP,
        CLICK_WOOD,
        SPACEBAR,
        DELETE,
        RETURN,
        SYSTEM_DEFAULT
    }

    init {
        initializeSoundPool()
    }

    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    isInitialized = true
                }
            }

            loadSounds()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSounds() {
        try {
            val customSoundId = prefs.customSoundResourceId
            if (customSoundId != 0) {
                soundMap[SoundType.CLICK_MECHANICAL] = soundPool?.load(context, customSoundId, 1) ?: 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playKeyClick(soundType: SoundType = SoundType.CLICK_MECHANICAL) {
        if (!prefs.isSoundEnabled) return

        try {
            when (soundType) {
                SoundType.SYSTEM_DEFAULT -> {
                    audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                }
                else -> {
                    val soundId = soundMap[soundType]
                    if (soundId != null && soundId > 0 && isInitialized) {
                        val volume = prefs.soundVolume / 100f
                        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
                    } else {
                        audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
        }
    }

    fun playSpacebarSound() {
        if (!prefs.isSoundEnabled) return
        playKeyClick(SoundType.SPACEBAR)
    }

    fun playDeleteSound() {
        if (!prefs.isSoundEnabled) return
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playReturnSound() {
        if (!prefs.isSoundEnabled) return
        try {
            audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCustomSound(resourceId: Int) {
        try {
            soundMap.clear()
            if (resourceId != 0) {
                soundMap[SoundType.CLICK_MECHANICAL] = soundPool?.load(context, resourceId, 1) ?: 0
            }
            prefs.customSoundResourceId = resourceId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundMap.clear()
            isInitialized = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}