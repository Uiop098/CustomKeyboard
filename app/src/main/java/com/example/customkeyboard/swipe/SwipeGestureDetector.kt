package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.PointF
import android.inputmethodservice.Keyboard
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detects swipe gestures on the keyboard and maps them to key paths.
 * Also handles special gestures like:
 * - Swipe left on backspace: Delete word
 * - Swipe right on space: Move cursor right
 * - Swipe left on space: Move cursor left
 * - Swipe up on keys: Capitalize
 */
@Suppress("DEPRECATION")
class SwipeGestureDetector(
    private val context: Context,
    private val keyboardView: SwipeKeyboardView,
    private val onSwipeComplete: (List<PointF>) -> Unit,
    private val onGestureDelete: () -> Unit,
    private val onGestureCursorMove: (Int) -> Unit, // -1 for left, 1 for right
    private val onGestureCapitalize: () -> Unit
) {

    private val touchPoints = mutableListOf<PointF>()
    private var isSwiping = false
    private val minSwipeDistance = 45f // pixels
    private val minGestureDistance = 35f // pixels for special gestures
    private var gestureStartKey: Int = -1

    init {
        // Link with SwipeKeyboardView
        keyboardView.swipeDetector = this
    }

    /**
     * Intercepts and processes touch events for gestures and swiping.
     * Returns true when the event should be consumed (i.e. swipe is happening),
     * or false when normal key clicking should proceed.
     */
    fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchPoints.clear()
                addTouchPoint(event)
                isSwiping = false
                gestureStartKey = getKeyAtPoint(event.x, event.y)
                return false // Allow normal key down to proceed
            }

            MotionEvent.ACTION_MOVE -> {
                addTouchPoint(event)
                val distance = calculateDistance()
                if (distance > minSwipeDistance) {
                    isSwiping = true
                    keyboardView.setSwipePoints(touchPoints, true)
                    return true // Consume as swipe
                } else if (distance > minGestureDistance) {
                    // Check for special gestures (spacebar cursor move, backspace delete word)
                    if (checkSpecialGestures()) {
                        return true
                    }
                }
                return isSwiping
            }

            MotionEvent.ACTION_UP -> {
                if (isSwiping && touchPoints.size >= 2) {
                    onSwipeComplete(touchPoints.toList())
                    touchPoints.clear()
                    isSwiping = false
                    gestureStartKey = -1
                    keyboardView.clearSwipe()
                    return true // Swiped word committed, consume
                }

                if (gestureStartKey != -1 && checkShortGestures()) {
                    touchPoints.clear()
                    isSwiping = false
                    gestureStartKey = -1
                    keyboardView.clearSwipe()
                    return true
                }

                touchPoints.clear()
                isSwiping = false
                gestureStartKey = -1
                keyboardView.clearSwipe()
                return false // Allow normal key press to register!
            }

            MotionEvent.ACTION_CANCEL -> {
                touchPoints.clear()
                isSwiping = false
                gestureStartKey = -1
                keyboardView.clearSwipe()
                return false
            }
        }
        return false
    }

    private fun addTouchPoint(event: MotionEvent) {
        touchPoints.add(PointF(event.x, event.y))
    }

    private fun calculateDistance(): Float {
        if (touchPoints.size < 2) return 0f
        var distance = 0f
        for (i in 1 until touchPoints.size) {
            val p1 = touchPoints[i - 1]
            val p2 = touchPoints[i]
            val dx = p2.x - p1.x
            val dy = p2.y - p1.y
            distance += sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return distance
    }

    private fun checkSpecialGestures(): Boolean {
        if (gestureStartKey == -1 || touchPoints.size < 3) return false

        val startPoint = touchPoints[0]
        val currentPoint = touchPoints[touchPoints.lastIndex]
        val dx = currentPoint.x - startPoint.x
        val dy = currentPoint.y - startPoint.y

        when (gestureStartKey) {
            Keyboard.KEYCODE_DELETE -> {
                // Swipe left on backspace = delete word
                if (dx < -minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                    onGestureDelete()
                    touchPoints.clear()
                    isSwiping = false
                    return true
                }
            }
            32 -> { // Space key
                // Swipe right on space = move cursor right
                if (dx > minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                    onGestureCursorMove(1)
                    touchPoints.clear()
                    isSwiping = false
                    return true
                }
                // Swipe left on space = move cursor left
                else if (dx < -minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                    onGestureCursorMove(-1)
                    touchPoints.clear()
                    isSwiping = false
                    return true
                }
            }
            else -> {
                // Swipe up on letter key = capitalize
                if (dy < -minGestureDistance && abs(dx) < minGestureDistance * 1.2f) {
                    onGestureCapitalize()
                    touchPoints.clear()
                    isSwiping = false
                    return true
                }
            }
        }
        return false
    }

    private fun checkShortGestures(): Boolean {
        if (touchPoints.size < 2) return false

        val startPoint = touchPoints[0]
        val endPoint = touchPoints[touchPoints.lastIndex]
        val dx = endPoint.x - startPoint.x
        val dy = endPoint.y - startPoint.y
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance < minSwipeDistance && distance > minGestureDistance) {
            when (gestureStartKey) {
                Keyboard.KEYCODE_DELETE -> {
                    if (dx < -minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                        onGestureDelete()
                        return true
                    }
                }
                32 -> {
                    if (dx > minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                        onGestureCursorMove(1)
                        return true
                    } else if (dx < -minGestureDistance && abs(dy) < minGestureDistance * 1.2f) {
                        onGestureCursorMove(-1)
                        return true
                    }
                }
                else -> {
                    if (dy < -minGestureDistance && abs(dx) < minGestureDistance * 1.2f) {
                        onGestureCapitalize()
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getTouchPoints(): List<PointF> = touchPoints.toList()
    fun isSwiping(): Boolean = isSwiping

    private fun getKeyAtPoint(x: Float, y: Float): Int {
        val keyboard = keyboardView.keyboard ?: return -1
        val index = findKeyAtPoint(keyboard, x, y)
        return if (index in keyboard.keys.indices) {
            keyboard.keys[index].codes.firstOrNull() ?: -1
        } else {
            -1
        }
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
}
