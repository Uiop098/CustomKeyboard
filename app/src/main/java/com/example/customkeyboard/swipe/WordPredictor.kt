package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.PointF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import com.example.customkeyboard.R
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Predicts words from swipe gestures using a dictionary and key positions
 */
class WordPredictor(private val context: Context) {

    private val dictionary = mutableListOf<String>()
    private val keyPositions = mutableMapOf<Int, KeyPosition>()
    private var keyboardView: KeyboardView? = null

    init {
        loadDictionary()
    }

    private fun loadDictionary() {
        try {
            // Load from raw resource
            val inputStream = context.resources.openRawResource(R.raw.dictionary)
            inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    val word = line.trim().lowercase()
                    if (word.length >= 2 && word.all { it.isLetter() }) {
                        dictionary.add(word)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to basic dictionary
            loadDefaultDictionary()
        }
    }

    private fun loadDefaultDictionary() {
        val defaultWords = listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
            "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
            "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
            "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
            "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
            "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
            "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
            "is", "are", "was", "were", "been", "has", "had", "does", "did", "doing",
            "hello", "world", "android", "keyboard", "swipe", "type", "write", "text", "message", "chat"
        )
        dictionary.addAll(defaultWords)
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

        for (word in dictionary) {
            if (word.length < keyCodes.size - 2 || word.length > keyCodes.size + 2) continue

            val score = calculateMatchScore(word, keyCodes)
            if (score < bestScore) {
                bestScore = score
                bestMatch = word
            }
        }

        // Only return if confidence is high enough
        return if (bestScore < 100f) bestMatch else null
    }

    private fun calculateMatchScore(word: String, keyCodes: List<Int>): Float {
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

        return score
    }

    data class KeyPosition(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )
}