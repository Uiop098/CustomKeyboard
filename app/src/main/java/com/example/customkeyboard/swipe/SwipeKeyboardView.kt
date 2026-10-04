package com.example.customkeyboard.swipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.theme.KeyboardTheme
import com.example.customkeyboard.theme.ThemeManager

/**
 * Enhanced KeyboardView supporting themes, frosted glass, custom wallpaper backgrounds,
 * custom key styling & radius, gradient swipe trails, and safe key popup previews.
 */
@Suppress("DEPRECATION")
class SwipeKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : KeyboardView(context, attrs, defStyleAttr) {

    private val prefs = Prefs(context)
    private val themeManager = ThemeManager(context)

    private var activeTheme: KeyboardTheme = themeManager.getCurrentTheme()
    private var customBgBitmap: Bitmap? = null

    private var pressedKey: Keyboard.Key? = null
    private var isShiftedState = false

    private var swipePoints: List<PointF> = emptyList()
    var isSwiping = false

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
    }
    private val popupPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val shiftIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val swipePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    var swipeDetector: SwipeGestureDetector? = null

    init {
        isPreviewEnabled = false // Keep platform preview disabled to prevent KeyboardView showKey NPE
        reloadTheme()
    }

    /**
     * Applies active theme, key corner radius, and custom wallpaper to the keyboard view.
     */
    fun reloadTheme() {
        activeTheme = themeManager.getCurrentTheme()

        // 1. Load custom background image if applicable
        customBgBitmap = if (activeTheme.id == "CUSTOM" || themeManager.hasCustomBackgroundImage()) {
            themeManager.loadCustomBackgroundBitmap()
        } else {
            null
        }

        // 2. Set background color or transparent for glass/custom
        if (customBgBitmap != null) {
            setBackgroundColor(Color.TRANSPARENT)
        } else {
            setBackgroundColor(activeTheme.keyboardBgColor)
        }

        invalidateAllKeys()
        invalidate()
    }

    override fun setShifted(shifted: Boolean): Boolean {
        isShiftedState = shifted
        val result = super.setShifted(shifted)
        invalidateAllKeys()
        invalidate()
        return result
    }

    override fun isShifted(): Boolean {
        return isShiftedState
    }

    override fun invalidateAllKeys() {
        super.invalidateAllKeys()
        invalidate()
    }

    fun setSwipePoints(points: List<PointF>, swiping: Boolean) {
        swipePoints = points
        isSwiping = swiping
        if (swiping) {
            pressedKey = null
        }
        invalidate()
    }

    fun clearSwipe() {
        swipePoints = emptyList()
        isSwiping = false
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val detectorConsumed = swipeDetector?.onInterceptTouchEvent(event) ?: false

        if (detectorConsumed) {
            pressedKey = null
            invalidate()
            if (event.actionMasked == MotionEvent.ACTION_MOVE && swipePoints.size > 2) {
                val cancelEvent = MotionEvent.obtain(event)
                cancelEvent.action = MotionEvent.ACTION_CANCEL
                super.onTouchEvent(cancelEvent)
                cancelEvent.recycle()
            }
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = findKeyAt(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val current = findKeyAt(event.x, event.y)
                if (current != pressedKey) {
                    pressedKey = current
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
            }
        }

        return super.onTouchEvent(event)
    }

    private fun findKeyAt(x: Float, y: Float): Keyboard.Key? {
        val kbd = keyboard ?: return null
        for (k in kbd.keys) {
            if (x >= k.x && x <= k.x + k.width && y >= k.y && y <= k.y + k.height) {
                return k
            }
        }
        return null
    }

    override fun onDraw(canvas: Canvas) {
        // 1. Draw custom background bitmap or glass backdrop
        drawCustomBackground(canvas)

        // 2. Custom Themed Key Rendering (replaces static KeyboardView onDraw)
        drawThemedKeys(canvas)

        // 3. Draw key popup preview if active
        drawKeyPopupPreview(canvas)

        // 4. Draw swipe gesture trail
        drawSwipeTrail(canvas)
    }

    private fun isActionKey(key: Keyboard.Key): Boolean {
        val code = key.codes.firstOrNull() ?: 0
        return code == Keyboard.KEYCODE_DELETE ||
               code == Keyboard.KEYCODE_SHIFT ||
               code == Keyboard.KEYCODE_DONE ||
               code == Keyboard.KEYCODE_CANCEL ||
               code == Keyboard.KEYCODE_MODE_CHANGE ||
               code == -2 || // switch symbols/numbers
               code == -100 // emoji
    }

    private fun drawThemedKeys(canvas: Canvas) {
        val kbd = keyboard ?: return
        val density = resources.displayMetrics.density
        val scaledDensity = resources.displayMetrics.scaledDensity

        val spacing = (prefs.keySpacingDp * density / 2f).coerceAtLeast(1f)
        val radius = prefs.keyCornerRadiusDp * density

        for (key in kbd.keys) {
            val isAction = isActionKey(key)
            val isPressed = (key == pressedKey) || key.pressed

            val left = key.x + spacing
            val top = key.y + spacing
            val right = key.x + key.width - spacing
            val bottom = key.y + key.height - spacing

            if (right <= left || bottom <= top) continue

            val keyRect = RectF(left, top, right, bottom)

            // 1. Key Background
            val bgColor = when {
                isPressed -> if (isAction) activeTheme.keyActionPressedBgColor else activeTheme.keyPressedBgColor
                isAction -> activeTheme.keyActionBgColor
                else -> activeTheme.keyBgColor
            }
            keyPaint.color = bgColor
            canvas.drawRoundRect(keyRect, radius, radius, keyPaint)

            // 2. Key Border (if theme enables it or in glass mode)
            if (activeTheme.keyBorderWidthDp > 0) {
                borderPaint.strokeWidth = activeTheme.keyBorderWidthDp * density
                borderPaint.color = activeTheme.keyBorderColor
                canvas.drawRoundRect(keyRect, radius, radius, borderPaint)
            }

            // 3. Shift Key Active Indicator
            if (key.codes.firstOrNull() == Keyboard.KEYCODE_SHIFT && isShiftedState) {
                shiftIndicatorPaint.color = activeTheme.accentColor
                shiftIndicatorPaint.style = Paint.Style.FILL
                val dotRadius = 3.5f * density
                canvas.drawCircle(keyRect.centerX(), keyRect.top + 8f * density, dotRadius, shiftIndicatorPaint)
            }

            // 4. Key Icon
            val icon = key.icon
            if (icon != null) {
                val iconW = icon.intrinsicWidth.takeIf { it > 0 } ?: (20 * density).toInt()
                val iconH = icon.intrinsicHeight.takeIf { it > 0 } ?: (20 * density).toInt()
                val iconLeft = (keyRect.centerX() - iconW / 2).toInt()
                val iconTop = (keyRect.centerY() - iconH / 2).toInt()
                icon.setBounds(iconLeft, iconTop, iconLeft + iconW, iconTop + iconH)
                icon.draw(canvas)
            }
            // 5. Key Label
            else if (key.label != null) {
                val rawLabel = key.label.toString()
                val label = if (isShiftedState && rawLabel.length == 1 && rawLabel[0].isLetter()) {
                    rawLabel.uppercase()
                } else {
                    rawLabel
                }

                // Calculate font size
                textPaint.textSize = when {
                    label.length == 1 -> (20f * scaledDensity).coerceAtMost(keyRect.height() * 0.55f)
                    label.length <= 4 -> (14f * scaledDensity).coerceAtMost(keyRect.height() * 0.40f)
                    else -> (11f * scaledDensity).coerceAtMost(keyRect.height() * 0.35f)
                }

                textPaint.color = if (isAction && !activeTheme.isDark && activeTheme.id == "LIGHT_MINIMAL") {
                    Color.WHITE
                } else {
                    activeTheme.keyTextColor
                }

                val fontMetrics = textPaint.fontMetrics
                val centerY = keyRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
                canvas.drawText(label, keyRect.centerX(), centerY, textPaint)
            }

            // 6. Secondary / Hint character in top right corner
            if (key.popupCharacters != null && key.popupCharacters.isNotEmpty() && key.width > 26 * density && key.label != null && key.label.length == 1) {
                val hint = key.popupCharacters[0].toString()
                hintPaint.textSize = (10f * scaledDensity).coerceAtMost(keyRect.height() * 0.30f)
                hintPaint.color = activeTheme.keySecondaryTextColor
                canvas.drawText(hint, keyRect.right - 4 * density, keyRect.top + 11 * density, hintPaint)
            }
        }
    }

    private fun drawKeyPopupPreview(canvas: Canvas) {
        if (!prefs.isPopupPreviewEnabled || isSwiping) return
        val key = pressedKey ?: return
        val label = key.label ?: return
        if (key.codes.firstOrNull() == 32) return // Skip popup for spacebar

        val density = resources.displayMetrics.density
        val scaledDensity = resources.displayMetrics.scaledDensity

        val rawLabel = label.toString()
        val text = if (isShiftedState && rawLabel.length == 1 && rawLabel[0].isLetter()) {
            rawLabel.uppercase()
        } else {
            rawLabel
        }

        val popupWidth = (key.width * 1.35f).coerceAtLeast(46f * density)
        val popupHeight = (key.height * 1.25f).coerceAtLeast(50f * density)
        val popupCenterX = key.x + key.width / 2f
        val popupCenterY = key.y - popupHeight / 2f - 4f * density

        val popupLeft = (popupCenterX - popupWidth / 2f).coerceIn(4f * density, width - popupWidth - 4f * density)
        val popupTop = popupCenterY - popupHeight / 2f
        val popupRight = popupLeft + popupWidth
        val popupBottom = popupTop + popupHeight

        val popupRect = RectF(popupLeft, popupTop, popupRight, popupBottom)
        val popupRadius = 12f * density

        // Draw elevated popup card background
        popupPaint.style = Paint.Style.FILL
        popupPaint.color = if (activeTheme.isDark) 0xF01E293B.toInt() else 0xF0FFFFFF.toInt()
        canvas.drawRoundRect(popupRect, popupRadius, popupRadius, popupPaint)

        // Draw accent border
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 2f * density
        borderPaint.color = activeTheme.accentColor
        canvas.drawRoundRect(popupRect, popupRadius, popupRadius, borderPaint)

        // Draw large preview text
        popupTextPaint.textSize = (26f * scaledDensity).coerceAtMost(popupHeight * 0.65f)
        popupTextPaint.color = if (activeTheme.isDark) 0xFFFFFFFF.toInt() else 0xFF0F172A.toInt()
        val fontMetrics = popupTextPaint.fontMetrics
        val centerY = popupRect.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(text, popupRect.centerX(), centerY, popupTextPaint)
    }

    private fun drawCustomBackground(canvas: Canvas) {
        val bitmap = customBgBitmap
        if (bitmap != null && !bitmap.isRecycled) {
            val viewWidth = width
            val viewHeight = height
            if (viewWidth > 0 && viewHeight > 0) {
                // Center-crop scaled background
                val srcRect = calculateCenterCropRect(bitmap.width, bitmap.height, viewWidth, viewHeight)
                val dstRect = Rect(0, 0, viewWidth, viewHeight)
                canvas.drawBitmap(bitmap, srcRect, dstRect, bgPaint)

                // Dark translucent scrim for readability
                scrimPaint.color = 0x88000000.toInt()
                canvas.drawRect(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat(), scrimPaint)
            }
        }
    }

    private fun calculateCenterCropRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcAspect = srcW.toFloat() / srcH.toFloat()
        val dstAspect = dstW.toFloat() / dstH.toFloat()

        return if (srcAspect > dstAspect) {
            val cropW = (srcH * dstAspect).toInt()
            val xOffset = (srcW - cropW) / 2
            Rect(xOffset, 0, xOffset + cropW, srcH)
        } else {
            val cropH = (srcW / dstAspect).toInt()
            val yOffset = (srcH - cropH) / 2
            Rect(0, yOffset, srcW, yOffset + cropH)
        }
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

        // Use theme accent color in trail
        val accent = activeTheme.accentColor
        val colors = intArrayOf(
            accent,
            Color.parseColor("#8B5CF6"),
            Color.parseColor("#EC4899")
        )
        val positions = floatArrayOf(0f, 0.5f, 1f)
        val gradient = LinearGradient(
            swipePoints[0].x, swipePoints[0].y,
            swipePoints[swipePoints.lastIndex].x, swipePoints[swipePoints.lastIndex].y,
            colors, positions, Shader.TileMode.CLAMP
        )
        swipePaint.shader = gradient
        swipePaint.strokeWidth = 10f

        canvas.drawPath(path, swipePaint)

        // Draw start point
        val startPoint = swipePoints[0]
        val startPaint = Paint().apply {
            color = Color.parseColor("#34A853")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(startPoint.x, startPoint.y, 14f, startPaint)

        val startInnerPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(startPoint.x, startPoint.y, 6f, startInnerPaint)

        // Draw end point if completed
        if (!isSwiping) {
            val lastPoint = swipePoints[swipePoints.lastIndex]
            val endPaint = Paint().apply {
                color = Color.parseColor("#EA4335")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastPoint.x, lastPoint.y, 14f, endPaint)

            val endInnerPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(lastPoint.x, lastPoint.y, 6f, endInnerPaint)
        }
    }
}
