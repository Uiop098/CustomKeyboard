package com.example.customkeyboard.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import com.example.customkeyboard.data.Prefs
import java.io.File
import java.io.FileOutputStream

class SoundManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val prefs = Prefs(context)
    private var soundPool: SoundPool? = null

    private val soundMap = mutableMapOf<SoundType, Int>()
    private val loadedSoundIds = mutableSetOf<Int>()
    private var customSoundPoolId: Int = 0

    enum class SoundType(val displayName: String) {
        CLICK_MECHANICAL("Mechanical Switch"),
        CLICK_TYPEWRITER("Vintage Typewriter"),
        CLICK_POP("Bubble Pop"),
        CLICK_WOOD("Wood Block"),
        CLICK_SOFT("Soft Tap"),
        SPACEBAR("Spacebar"),
        DELETE("Delete Key"),
        RETURN("Enter Key"),
        SYSTEM_DEFAULT("System Default"),
        CUSTOM_AUDIO("Custom Audio File");

        companion object {
            fun fromString(name: String): SoundType {
                return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CLICK_MECHANICAL
            }
        }
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
                .setMaxStreams(6)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSoundIds.add(sampleId)
                }
            }

            loadAllSounds()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads procedural WAV presets and custom user audio.
     */
    private fun loadAllSounds() {
        try {
            val pool = soundPool ?: return
            loadedSoundIds.clear()
            soundMap.clear()

            // 1. Load built-in procedural WAV files
            val presetFiles = SoundGenerator.ensureBuiltInSounds(context.cacheDir)
            for ((type, file) in presetFiles) {
                if (file.exists() && file.length() > 0) {
                    val id = pool.load(file.absolutePath, 1)
                    if (id > 0) {
                        soundMap[type] = id
                    }
                }
            }

            // 2. Load custom user audio if available
            loadCustomAudioFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads custom audio from permanent app internal storage.
     */
    private fun loadCustomAudioFile() {
        val pool = soundPool ?: return
        val customFile = File(context.filesDir, "custom_click_sound.bin")
        if (customFile.exists() && customFile.length() > 0) {
            try {
                customSoundPoolId = pool.load(customFile.absolutePath, 1)
                soundMap[SoundType.CUSTOM_AUDIO] = customSoundPoolId
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Reloads configuration and sounds whenever settings change.
     */
    fun reloadSettings() {
        try {
            // Re-load custom audio if updated
            val customFile = File(context.filesDir, "custom_click_sound.bin")
            if (customFile.exists() && customFile.length() > 0 && !soundMap.containsKey(SoundType.CUSTOM_AUDIO)) {
                loadCustomAudioFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Plays a key click sound according to user preferences.
     */
    fun playKeyClick(soundType: SoundType = SoundType.CLICK_MECHANICAL) {
        if (!prefs.isSoundEnabled) return

        val selectedType = SoundType.fromString(prefs.selectedSoundType)

        // If system default is selected, use AudioManager
        if (selectedType == SoundType.SYSTEM_DEFAULT) {
            playSystemEffect(soundType)
            return
        }

        val volume = (prefs.soundVolume / 100f).coerceIn(0.01f, 1.0f)
        val pool = soundPool

        if (pool != null) {
            val targetType = when {
                selectedType == SoundType.CUSTOM_AUDIO && soundMap.containsKey(SoundType.CUSTOM_AUDIO) -> SoundType.CUSTOM_AUDIO
                soundType == SoundType.SPACEBAR && soundMap.containsKey(SoundType.SPACEBAR) -> SoundType.SPACEBAR
                soundType == SoundType.DELETE && soundMap.containsKey(SoundType.DELETE) -> SoundType.DELETE
                soundType == SoundType.RETURN && soundMap.containsKey(SoundType.RETURN) -> SoundType.RETURN
                else -> selectedType
            }

            val soundId = soundMap[targetType] ?: soundMap[SoundType.CLICK_MECHANICAL] ?: 0

            if (soundId > 0 && (loadedSoundIds.contains(soundId) || loadedSoundIds.isEmpty())) {
                val streamId = pool.play(soundId, volume, volume, 1, 0, 1.0f)
                if (streamId != 0) return
            }
        }

        // Fallback to system click
        playSystemEffect(soundType)
    }

    private fun playSystemEffect(soundType: SoundType) {
        try {
            val effect = when (soundType) {
                SoundType.SPACEBAR -> AudioManager.FX_KEYPRESS_SPACEBAR
                SoundType.DELETE -> AudioManager.FX_KEYPRESS_DELETE
                SoundType.RETURN -> AudioManager.FX_KEYPRESS_RETURN
                else -> AudioManager.FX_KEYPRESS_STANDARD
            }
            audioManager?.playSoundEffect(effect)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSpacebarSound() {
        playKeyClick(SoundType.SPACEBAR)
    }

    fun playDeleteSound() {
        playKeyClick(SoundType.DELETE)
    }

    fun playReturnSound() {
        playKeyClick(SoundType.RETURN)
    }

    /**
     * Imports and permanently saves custom audio from a Uri.
     */
    fun saveAndLoadCustomAudio(uri: Uri): Boolean {
        return try {
            val customFile = File(context.filesDir, "custom_click_sound.bin")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(customFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (customFile.exists() && customFile.length() > 0) {
                prefs.customSoundUri = uri.toString()
                prefs.selectedSoundType = SoundType.CUSTOM_AUDIO.name
                val id = soundPool?.load(customFile.absolutePath, 1) ?: 0
                if (id > 0) {
                    customSoundPoolId = id
                    soundMap[SoundType.CUSTOM_AUDIO] = id
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removeCustomAudio() {
        val customFile = File(context.filesDir, "custom_click_sound.bin")
        if (customFile.exists()) {
            customFile.delete()
        }
        prefs.customSoundUri = ""
        soundMap.remove(SoundType.CUSTOM_AUDIO)
        if (prefs.selectedSoundType == SoundType.CUSTOM_AUDIO.name) {
            prefs.selectedSoundType = SoundType.CLICK_MECHANICAL.name
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundMap.clear()
            loadedSoundIds.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
