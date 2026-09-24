package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

/**
 * Custom KeyboardView that draws swipe trail
 */
class SwipeKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KeyboardView(context, attrs, defStyleAttr) {

    private var swipePoints: List<PointF> = emptyList()
    private var isSwiping = false

    private val swipePaint = Paint().apply {
        color = Color.parseColor("#4285F4") // Google blue
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val startPointPaint = Paint().apply {
        color = Color.parseColor("#34A853") // Green
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val endPointPaint = Paint().apply {
        color = Color.parseColor("#EA4335") // Red
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setSwipePoints(points: List<PointF>, swiping: Boolean) {
        swipePoints = points
        isSwiping = swiping
        invalidate()
    }

    fun clearSwipe() {
        swipePoints = emptyList()
        isSwiping = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSwipeTrail(canvas)
    }

    private fun drawSwipeTrail(canvas: Canvas) {
        if (swipePoints.size < 2) return

        val path = Path()
        val firstPoint = swipePoints[0]
        path.moveTo(firstPoint.x, firstPoint.y)

        for (i in 1 until swipePoints.size) {
            val point = swipePoints[i]
            path.lineTo(point.x, point.y)
        }

        // Draw the swipe trail
        canvas.drawPath(path, swipePaint)

        // Draw start point (green circle)
        canvas.drawCircle(firstPoint.x, firstPoint.y, 12f, startPointPaint)

        // Draw end point (red circle) if not currently swiping
        if (!isSwiping) {
            val lastPoint = swipePoints[swipePoints.lastIndex]
            canvas.drawCircle(lastPoint.x, lastPoint.y, 12f, endPointPaint)
        }
    }
}