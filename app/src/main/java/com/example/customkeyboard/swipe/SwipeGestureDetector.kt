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
            distance += Math.hypot(p2.x - p1.x, p2.y - p1.y).toFloat()
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
            val keyIndex = keyboardView.getKeyAt(point.x.toInt(), point.y.toInt())
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