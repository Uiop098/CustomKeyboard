package com.example.customkeyboard.util

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates procedural PCM WAV sound files for instant, crisp, low-latency keyboard clicks.
 */
object SoundGenerator {

    private const val SAMPLE_RATE = 44100

    /**
     * Ensures all built-in sound presets are generated into [cacheDir].
     * Returns a map of SoundType to generated File.
     */
    fun ensureBuiltInSounds(cacheDir: File): Map<SoundManager.SoundType, File> {
        val soundFiles = mutableMapOf<SoundManager.SoundType, File>()

        val presets = listOf(
            SoundManager.SoundType.CLICK_MECHANICAL to ::generateMechanicalClick,
            SoundManager.SoundType.CLICK_TYPEWRITER to ::generateTypewriterClick,
            SoundManager.SoundType.CLICK_POP to ::generatePopClick,
            SoundManager.SoundType.CLICK_WOOD to ::generateWoodClick,
            SoundManager.SoundType.CLICK_SOFT to ::generateSoftClick,
            SoundManager.SoundType.SPACEBAR to ::generateSpacebarClick,
            SoundManager.SoundType.DELETE to ::generateDeleteClick,
            SoundManager.SoundType.RETURN to ::generateReturnClick
        )

        for ((type, generator) in presets) {
            val file = File(cacheDir, "preset_sound_${type.name.lowercase()}.wav")
            if (!file.exists() || file.length() < 44) {
                try {
                    val pcmData = generator()
                    val wavData = createWavData(pcmData, SAMPLE_RATE)
                    FileOutputStream(file).use { it.write(wavData) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (file.exists()) {
                soundFiles[type] = file
            }
        }

        return soundFiles
    }

    /**
     * Mechanical switch click: Dual transient with sharp attack at 4200Hz and secondary tactile resonance.
     */
    private fun generateMechanicalClick(): ShortArray {
        val durationMs = 28
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Transient 1 (impact)
            val env1 = exp(-t * 350.0)
            val tone1 = sin(2.0 * PI * 4200.0 * t) * 0.7 + sin(2.0 * PI * 6500.0 * t) * 0.3

            // Transient 2 (housing resonance slightly delayed)
            val t2 = (t - 0.005).coerceAtLeast(0.0)
            val env2 = if (t >= 0.005) exp(-t2 * 250.0) * 0.5 else 0.0
            val tone2 = sin(2.0 * PI * 1800.0 * t2)

            val mixed = (tone1 * env1 + tone2 * env2) * 28000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Typewriter strike: Heavy metal impact + mechanical rattle.
     */
    private fun generateTypewriterClick(): ShortArray {
        val durationMs = 40
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 220.0)
            val metallic = sin(2.0 * PI * 2800.0 * t) * 0.5 +
                    sin(2.0 * PI * 1400.0 * t) * 0.3 +
                    sin(2.0 * PI * 850.0 * t) * 0.2
            val mixed = metallic * env * 30000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Pop / Bubble click: Fast upward pitch sweep.
     */
    private fun generatePopClick(): ShortArray {
        val durationMs = 35
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 160.0) * sin(PI * (i.toDouble() / numSamples))
            val freq = 450.0 + (1600.0 * (t / (durationMs / 1000.0)))
            val tone = sin(2.0 * PI * freq * t)
            val mixed = tone * env * 28000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Wood block tap: Rich acoustic wood resonance around 950Hz.
     */
    private fun generateWoodClick(): ShortArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 280.0)
            val tone = sin(2.0 * PI * 950.0 * t) * 0.7 + sin(2.0 * PI * 1900.0 * t) * 0.3
            val mixed = tone * env * 29000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Soft / Muted tap: Lowpass subtle click.
     */
    private fun generateSoftClick(): ShortArray {
        val durationMs = 20
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 400.0)
            val tone = sin(2.0 * PI * 600.0 * t) * 0.8 + sin(2.0 * PI * 1200.0 * t) * 0.2
            val mixed = tone * env * 22000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Spacebar deeper thud.
     */
    private fun generateSpacebarClick(): ShortArray {
        val durationMs = 45
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 180.0)
            val tone = sin(2.0 * PI * 400.0 * t) * 0.6 + sin(2.0 * PI * 1800.0 * t) * 0.4
            val mixed = tone * env * 28000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Delete key tap.
     */
    private fun generateDeleteClick(): ShortArray {
        val durationMs = 25
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 300.0)
            val tone = sin(2.0 * PI * 2200.0 * t) * 0.6 + sin(2.0 * PI * 3400.0 * t) * 0.4
            val mixed = tone * env * 26000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Return/Enter key affirmative chime.
     */
    private fun generateReturnClick(): ShortArray {
        val durationMs = 50
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 150.0)
            val tone = sin(2.0 * PI * 1200.0 * t) * 0.5 + sin(2.0 * PI * 1600.0 * t) * 0.5
            val mixed = tone * env * 27000.0
            samples[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return samples
    }

    /**
     * Wraps 16-bit PCM mono samples into a standard WAV header.
     */
    private fun createWavData(samples: ShortArray, sampleRate: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val dataSize = samples.size * 2
        val chunkSize = 36 + dataSize

        // RIFF header
        dos.writeBytes("RIFF")
        dos.writeInt(Integer.reverseBytes(chunkSize))
        dos.writeBytes("WAVE")

        // "fmt " sub-chunk
        dos.writeBytes("fmt ")
        dos.writeInt(Integer.reverseBytes(16)) // Subchunk1Size for PCM
        dos.writeShort(java.lang.Short.reverseBytes(1.toShort()).toInt()) // AudioFormat (1 = PCM)
        dos.writeShort(java.lang.Short.reverseBytes(numChannels.toShort()).toInt())
        dos.writeInt(Integer.reverseBytes(sampleRate))
        dos.writeInt(Integer.reverseBytes(byteRate))
        dos.writeShort(java.lang.Short.reverseBytes(blockAlign.toShort()).toInt())
        dos.writeShort(java.lang.Short.reverseBytes(bitsPerSample.toShort()).toInt())

        // "data" sub-chunk
        dos.writeBytes("data")
        dos.writeInt(Integer.reverseBytes(dataSize))

        for (sample in samples) {
            dos.writeShort(java.lang.Short.reverseBytes(sample).toInt())
        }

        dos.flush()
        return baos.toByteArray()
    }
}
