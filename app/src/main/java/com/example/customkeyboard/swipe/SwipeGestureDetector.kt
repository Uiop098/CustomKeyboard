package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.PointF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.MotionEvent
import android.view.View

/**
 * Detects swipe gestures on the keyboard and maps them to key paths
 */
class SwipeGestureDetector(
    private val context: Context,
    private val keyboardView: KeyboardView,
    private val onSwipeComplete: (List<PointF>) -> Unit
) {

    private val touchPoints = mutableListOf<PointF>()
    private var isSwiping = false
    private val minSwipeDistance = 50f // pixels
    private val keyCache = mutableMapOf<Int, KeyPosition>()

    init {
        keyboardView.setOnTouchListener { _, event ->
            onTouchEvent(event)
            true // Consume touch events
        }
    }

    private fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPoints.clear()
                addTouchPoint(event)
                isSwiping = false
            }
            MotionEvent.ACTION_MOVE -> {
                addTouchPoint(event)
                val distance = calculateDistance()
                if (distance > minSwipeDistance) {
                    isSwiping = true
                    keyboardView.invalidate() // Trigger redraw for swipe trail
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSwiping && touchPoints.size >= 2) {
                    onSwipeComplete(touchPoints.toList())
                }
                touchPoints.clear()
                isSwiping = false
                keyboardView.invalidate()
            }
        }
        return true
    }

    private fun addTouchPoint(event: MotionEvent) {
        val x = event.x
        val y = event.y
        touchPoints.add(PointF(x, y))
    }

    private fun calculateDistance(): Float {
        if (touchPoints.size < 2) return 0f
        var distance = 0f
        for (i in 1 until touchPoints.size) {
            val p1 = touchPoints[i - 1]
            val p2 = touchPoints[i]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            distance += Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return distance
    }

    fun getTouchPoints(): List<PointF> = touchPoints.toList()
    fun isSwiping(): Boolean = isSwiping

    /**
     * Maps touch points to the nearest keys on the keyboard
     */
    fun mapTouchPointsToKeys(): List<Int> {
        val keyCodes = mutableListOf<Int>()
        val keyboard = keyboardView.keyboard ?: return keyCodes

        for (point in touchPoints) {
            val keyIndex = findKeyAtPoint(keyboard, point.x, point.y)
            if (keyIndex >= 0) {
                val key = keyboard.keys[keyIndex]
                // Only add letter keys (not modifiers, space, delete, etc.)
                if (isLetterKey(key.codes[0])) {
                    keyCodes.add(key.codes[0])
                }
            }
        }
        return keyCodes
    }

    private fun findKeyAtPoint(keyboard: Keyboard, x: Float, y: Float): Int {
        for (i in keyboard.keys.indices) {
            val key = keyboard.keys[i]
            if (x >= key.x && x <= key.x + key.width &&
                y >= key.y && y <= key.y + key.height) {
                return i
            }
        }
        return -1
    }

    private fun isLetterKey(code: Int): Boolean {
        return code in 'a'..'z' || code in 'A'..'Z'
    }

    data class KeyPosition(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val code: Int
    )
}