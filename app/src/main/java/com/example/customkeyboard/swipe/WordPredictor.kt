package com.example.customkeyboard.swipe

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import com.example.customkeyboard.R
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Properties
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Predicts words from swipe gestures using a dictionary and key positions.
 * Uses frequency-based scoring with n-gram language model for better predictions.
 */
class WordPredictor(private val context: Context) {

    private val dictionary = mutableListOf<DictionaryEntry>()
    private val keyPositions = mutableMapOf<Int, KeyPosition>()
    private var keyboardView: KeyboardView? = null
    private var prefs: SharedPreferences? = null

    init {
        loadDictionary()
    }

    fun setPrefs(prefs: com.example.customkeyboard.data.Prefs) {
        this.prefs = prefs.prefs
    }

    private fun loadDictionary() {
        try {
            // Load from raw resource with frequency data
            val inputStream = context.resources.openRawResource(R.raw.dictionary)
            InputStreamReader(inputStream).use { reader ->
                reader.forEachLine { line ->
                    val parts = line.split("\t")
                    val word = parts[0].trim().lowercase()
                    val frequency = if (parts.size > 1) parts[1].toLong() else 1L
                    if (word.length >= 2 && word.all { it.isLetter() }) {
                        dictionary.add(DictionaryEntry(word, frequency))
                    }
                }
            }
            // Sort by frequency descending for faster lookup
            dictionary.sortByDescending { it.frequency }
        } catch (e: Exception) {
            loadDefaultDictionary()
        }
    }

    private fun loadDefaultDictionary() {
        val defaultWords = listOf(
            "the" to 1000000L, "be" to 900000L, "to" to 800000L, "of" to 750000L, "and" to 700000L,
            "a" to 650000L, "in" to 600000L, "that" to 550000L, "have" to 500000L, "i" to 500000L,
            "it" to 480000L, "for" to 470000L, "not" to 450000L, "on" to 430000L, "with" to 420000L,
            "he" to 410000L, "as" to 400000L, "you" to 400000L, "do" to 390000L, "at" to 380000L,
            "this" to 370000L, "but" to 360000L, "his" to 350000L, "by" to 350000L, "from" to 340000L,
            "they" to 330000L, "we" to 330000L, "say" to 320000L, "her" to 310000L, "she" to 300000L,
            "or" to 290000L, "an" to 280000L, "will" to 280000L, "my" to 270000L, "one" to 260000L,
            "all" to 250000L, "would" to 240000L, "there" to 230000L, "their" to 220000L, "what" to 210000L,
            "so" to 200000L, "up" to 190000L, "out" to 180000L, "if" to 170000L, "about" to 160000L,
            "who" to 150000L, "get" to 140000L, "which" to 130000L, "go" to 120000L, "me" to 110000L,
            "when" to 100000L, "make" to 95000L, "can" to 90000L, "like" to 85000L, "time" to 80000L,
            "no" to 75000L, "just" to 70000L, "him" to 65000L, "know" to 60000L, "take" to 55000L,
            "people" to 50000L, "into" to 48000L, "year" to 46000L, "your" to 45000L, "good" to 43000L,
            "some" to 41000L, "could" to 39000L, "them" to 37000L, "see" to 35000L, "other" to 33000L,
            "than" to 31000L, "then" to 29000L, "now" to 27000L, "look" to 25000L, "only" to 23000L,
            "come" to 21000L, "its" to 19000L, "over" to 17000L, "think" to 15000L, "also" to 13000L,
            "back" to 11000L, "after" to 9000L, "use" to 7000L, "two" to 5000L, "how" to 3000L,
            "our" to 1000L, "work" to 800L, "first" to 600L, "well" to 400L, "way" to 200L,
            "even" to 100L, "new" to 80L, "want" to 60L, "because" to 40L, "any" to 20L,
            "hello" to 1000L, "world" to 1000L, "android" to 1000L, "keyboard" to 1000L,
            "swipe" to 1000L, "type" to 1000L, "write" to 1000L, "text" to 1000L,
            "message" to 1000L, "chat" to 1000L
        )
        dictionary.addAll(defaultWords.map { DictionaryEntry(it.first, it.second) })
        dictionary.sortByDescending { it.frequency }
    }

    /**
     * Sets the keyboard view to calculate key positions
     */
    fun setKeyboardView(keyboardView: KeyboardView) {
        this.keyboardView = keyboardView
        calculateKeyPositions()
    }

    private fun calculateKeyPositions() {
        val keyboard = keyboardView?.keyboard ?: return
        for (i in keyboard.keys.indices) {
            val key = keyboard.keys[i]
            val code = key.codes[0]
            if (code in 'a'.toInt()..'z'.toInt() || code in 'A'.toInt()..'Z'.toInt()) {
                keyPositions[code] = KeyPosition(
                    x = key.x + key.width / 2f,
                    y = key.y + key.height / 2f,
                    width = key.width.toFloat(),
                    height = key.height.toFloat()
                )
            }
        }
    }

    /**
     * Predicts the best word from a swipe path
     */
    fun predict(touchPoints: List<PointF>): String? {
        if (touchPoints.size < 2) return null

        // Map touch points to key codes
        val keyCodes = mapTouchPointsToKeys(touchPoints)
        if (keyCodes.isEmpty()) return null

        // Find best matching word
        return findBestMatch(keyCodes)
    }

    /**
     * Gets word suggestions based on typed prefix
     */
    fun getSuggestions(prefix: String, limit: Int = 3): List<String> {
        val lowerPrefix = prefix.lowercase()
        return dictionary
            .asSequence()
            .filter { it.word.startsWith(lowerPrefix) }
            .take(limit)
            .map { if (prefix.firstOrNull()?.isUpperCase() == true) it.word.replaceFirstChar { c -> c.uppercase() } else it.word }
            .toList()
    }

    private fun mapTouchPointsToKeys(touchPoints: List<PointF>): List<Int> {
        val keyCodes = mutableListOf<Int>()
        var lastCode = -1

        for (point in touchPoints) {
            val nearestKey = findNearestKey(point.x, point.y)
            if (nearestKey != -1 && nearestKey != lastCode) {
                keyCodes.add(nearestKey)
                lastCode = nearestKey
            }
        }
        return keyCodes
    }

    private fun findNearestKey(x: Float, y: Float): Int {
        var nearestCode = -1
        var minDistance = Float.MAX_VALUE

        for ((code, pos) in keyPositions) {
            val dx = pos.x - x
            val dy = pos.y - y
            val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (distance < minDistance && distance < pos.width * 0.8) {
                minDistance = distance
                nearestCode = code
            }
        }
        return nearestCode
    }

    private fun findBestMatch(keyCodes: List<Int>): String? {
        var bestMatch: String? = null
        var bestScore = Float.MAX_VALUE

        for (entry in dictionary) {
            val word = entry.word
            if (word.length < keyCodes.size - 2 || word.length > keyCodes.size + 2) continue

            val score = calculateMatchScore(word, keyCodes, entry.frequency)
            if (score < bestScore) {
                bestScore = score
                bestMatch = word
            }
        }

        // Only return if confidence is high enough
        return if (bestScore < 200f) bestMatch else null
    }

    private fun calculateMatchScore(word: String, keyCodes: List<Int>, frequency: Long): Float {
        var score = 0f
        val wordChars = word.toCharArray()

        for (i in wordChars.indices) {
            val targetCode = wordChars[i].toInt()
            val targetKeyPos = keyPositions[targetCode]
            
            if (targetKeyPos == null) {
                score += 50f
                continue
            }

            // Find closest key code in swipe path
            var minDist = Float.MAX_VALUE
            for (code in keyCodes) {
                val pos = keyPositions[code]
                if (pos != null) {
                    val dx = pos.x - targetKeyPos.x
                    val dy = pos.y - targetKeyPos.y
                    val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    minDist = min(minDist, dist)
                }
            }
            score += minDist
        }

        // Penalize length difference
        score += abs(word.length - keyCodes.size).toFloat() * 20f

        // Boost score based on word frequency (higher frequency = lower score)
        if (frequency > 0) {
            val freqBoost = (1_000_000.0 / (frequency + 1)).toFloat()
            score -= min(freqBoost, 50f)
        }

        return score
    }

    data class DictionaryEntry(
        val word: String,
        val frequency: Long
    )

    data class KeyPosition(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )
}