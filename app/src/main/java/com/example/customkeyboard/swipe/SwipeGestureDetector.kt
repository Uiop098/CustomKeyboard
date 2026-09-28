package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.PointF
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.MotionEvent
import android.view.View

/**
 * Detects swipe gestures on the keyboard and maps them to key paths.
 * Also handles special gestures like:
 * - Swipe left on backspace: Delete word
 * - Swipe right on space: Move cursor right
 * - Swipe left on space: Move cursor left
 * - Swipe up on keys: Capitalize
 */
class SwipeGestureDetector(
    private val context: Context,
    private val keyboardView: KeyboardView,
    private val onSwipeComplete: (List<PointF>) -> Unit,
    private val onGestureDelete: () -> Unit,
    private val onGestureCursorMove: (Int) -> Unit, // -1 for left, 1 for right
    private val onGestureCapitalize: () -> Unit
) {

    private val touchPoints = mutableListOf<PointF>()
    private var isSwiping = false
    private val minSwipeDistance = 50f // pixels
    private val minGestureDistance = 30f // pixels for special gestures
    private var gestureStartKey: Int = -1

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
                gestureStartKey = findKeyAtPoint(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                addTouchPoint(event)
                val distance = calculateDistance()
                if (distance > minSwipeDistance) {
                    isSwiping = true
                    keyboardView.invalidate() // Trigger redraw for swipe trail
                } else if (distance > minGestureDistance) {
                    // Check for special gestures
                    checkSpecialGestures()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSwiping && touchPoints.size >= 2) {
                    onSwipeComplete(touchPoints.toList())
                } else if (gestureStartKey != -1) {
                    // Check for short gestures
                    checkShortGestures()
                }
                touchPoints.clear()
                isSwiping = false
                gestureStartKey = -1
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

    private fun checkSpecialGestures() {
        if (gestureStartKey == -1 || touchPoints.size < 3) return

        val startPoint = touchPoints[0]
        val currentPoint = touchPoints[touchPoints.lastIndex]
        val dx = currentPoint.x - startPoint.x
        val dy = currentPoint.y - startPoint.y

        when (gestureStartKey) {
            Keyboard.KEYCODE_DELETE -> {
                // Swipe left on backspace = delete word
if (dx < -minGestureDistance && Math.abs(dy) < minGestureDistance) {
                    onGestureDelete()
                    touchPoints.clear()
                    isSwiping = false
                }
            }
            32 -> { // Space key
                // Swipe right on space = move cursor right
                if (dx > minGestureDistance && Math.abs(dy) < minGestureDistance) {
                    onGestureCursorMove(1)
                    touchPoints.clear()
                    isSwiping = false
                }
                // Swipe left on space = move cursor left
else if (dx < -minGestureDistance && Math.abs(dy) < minGestureDistance) {
                    onGestureCursorMove(-1)
                    touchPoints.clear()
                    isSwiping = false
                }
            }
            else -> {
                // Swipe up on letter key = capitalize
                if (dy < -minGestureDistance && Math.abs(dx) < minGestureDistance) {
                    onGestureCapitalize()
                    touchPoints.clear()
                    isSwiping = false
                }
            }
        }
    }

    private fun checkShortGestures() {
        if (touchPoints.size < 2) return

        val startPoint = touchPoints[0]
        val endPoint = touchPoints[touchPoints.lastIndex]
        val dx = endPoint.x - startPoint.x
        val dy = endPoint.y - startPoint.y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        // Only trigger if it's a short swipe (< minSwipeDistance) but clear gesture
        if (distance < minSwipeDistance && distance > minGestureDistance) {
            when (gestureStartKey) {
                Keyboard.KEYCODE_DELETE -> {
if (dx < -minGestureDistance && Math.abs(dy) < minGestureDistance) {
                        onGestureDelete()
                    }
                }
                32 -> { // Space key
if (dx > minGestureDistance && Math.abs(dy) < minGestureDistance) {
                        onGestureCursorMove(1)
                    } else if (dx < -minGestureDistance && Math.abs(dy) < minGestureDistance) {
                        onGestureCursorMove(-1)
                    }
                }
                else -> {
if (dy < -minGestureDistance && Math.abs(dx) < minGestureDistance) {
                        onGestureCapitalize()
                    }
                }
            }
        }
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
        return code in 'a'.toInt()..'z'.toInt() || code in 'A'.toInt()..'Z'.toInt()
    }

    data class KeyPosition(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val code: Int
    )
}