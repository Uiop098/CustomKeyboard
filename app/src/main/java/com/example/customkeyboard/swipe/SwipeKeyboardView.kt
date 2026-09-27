package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

/**
 * Custom KeyboardView that draws swipe trail with gradient visualization
 */
class SwipeKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KeyboardView(context, attrs, defStyleAttr) {

    private var swipePoints: List<PointF> = emptyList()
    private var isSwiping = false

    private val swipePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val startPointPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val endPointPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val trailWidthPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
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

        // Create gradient for the trail
        val colors = intArrayOf(
            Color.parseColor("#4285F4"), // Blue start
            Color.parseColor("#8B5CF6"), // Purple middle
            Color.parseColor("#EC4899")  // Pink end
        )
        val positions = floatArrayOf(0f, 0.5f, 1f)
        val gradient = LinearGradient(
            swipePoints[0].x, swipePoints[0].y,
            swipePoints[swipePoints.lastIndex].x, swipePoints[swipePoints.lastIndex].y,
            colors, positions, Shader.TileMode.CLAMP
        )
        swipePaint.shader = gradient
        swipePaint.strokeWidth = 10f

        // Draw the swipe trail with gradient
        canvas.drawPath(path, swipePaint)

        // Draw start point (green circle with pulse effect)
        val startPoint = swipePoints[0]
        val startPaint = Paint().apply {
            color = Color.parseColor("#34A853")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(startPoint.x, startPoint.y, 14f, startPaint)

        // Inner white dot for start
        val startInnerPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(startPoint.x, startPoint.y, 6f, startInnerPaint)

        // Draw end point (red circle) if not currently swiping
        if (!isSwiping) {
            val lastPoint = swipePoints[swipePoints.lastIndex]
            val endPaint = Paint().apply {
                color = Color.parseColor("#EA4335")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastPoint.x, lastPoint.y, 14f, endPaint)

            // Inner white dot for end
            val endInnerPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastPoint.x, lastPoint.y, 6f, endInnerPaint)
        }

        // Draw directional arrows along the path for longer swipes
        if (swipePoints.size > 10 && !isSwiping) {
            drawDirectionArrows(canvas)
        }
    }

    private fun drawDirectionArrows(canvas: Canvas) {
        val arrowPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw 3 arrows along the path
        for (i in 2 until swipePoints.size - 2 step (swipePoints.size / 3)) {
            if (i + 1 < swipePoints.size) {
                val p1 = swipePoints[i]
                val p2 = swipePoints[i + 1]
                val angle = Math.atan2((p2.y - p1.y).toDouble(), (p2.x - p1.x).toDouble())
                
                val arrowSize = 12f
                val cx = p1.x
                val cy = p1.y
                
                val path = android.graphics.Path()
                path.moveTo(
                    (cx + arrowSize * Math.cos(angle)).toFloat(),
                    (cy + arrowSize * Math.sin(angle)).toFloat()
                )
                path.lineTo(
                    (cx + arrowSize * Math.cos(angle + 2.5)).toFloat(),
                    (cy + arrowSize * Math.sin(angle + 2.5)).toFloat()
                )
                path.lineTo(
                    (cx + arrowSize * Math.cos(angle - 2.5)).toFloat(),
                    (cy + arrowSize * Math.sin(angle - 2.5)).toFloat()
                )
                path.close()
                
                canvas.drawPath(path, arrowPaint)
            }
        }
    }
}