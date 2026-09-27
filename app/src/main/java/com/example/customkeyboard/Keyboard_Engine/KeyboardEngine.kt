package com.example.customkeyboard.Keyboard_Engine

import android.content.Context
import android.inputmethodservice.Keyboard
import com.example.customkeyboard.R
import com.example.customkeyboard.swipe.SwipeKeyboardView

class KeyboardEngine(private val context: Context, private val keyboardView: SwipeKeyboardView) {

    val qwertyKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwerty)
    val symbolsKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_symbols)
    val numbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_numbers)

    var currentMode = LayoutMode.QWERTY
        private set

    enum class LayoutMode {
        QWERTY, SYMBOLS, NUMBERS
    }

    // One-handed mode
    var isOneHandedMode = false
    var oneHandedSide = OneHandedSide.LEFT

    enum class OneHandedSide {
        LEFT, RIGHT
    }

    fun switchTo(mode: LayoutMode) {
        currentMode = mode
        when (mode) {
            LayoutMode.QWERTY -> keyboardView.keyboard = qwertyKeyboard
            LayoutMode.SYMBOLS -> keyboardView.keyboard = symbolsKeyboard
            LayoutMode.NUMBERS -> keyboardView.keyboard = numbersKeyboard
        }
        applyOneHandedMode()
        keyboardView.invalidateAllKeys()
    }

    fun toggleSymbols() {
        if (currentMode == LayoutMode.QWERTY) {
            switchTo(LayoutMode.SYMBOLS)
        } else {
            switchTo(LayoutMode.QWERTY)
        }
    }

    fun toggleNumbers() {
        if (currentMode == LayoutMode.NUMBERS) {
            switchTo(LayoutMode.QWERTY)
        } else {
            switchTo(LayoutMode.NUMBERS)
        }
    }

    fun setOneHandedMode(enabled: Boolean, side: OneHandedSide = OneHandedSide.LEFT) {
        isOneHandedMode = enabled
        oneHandedSide = side
        applyOneHandedMode()
        keyboardView.invalidateAllKeys()
    }

    private fun applyOneHandedMode() {
        if (!isOneHandedMode) return
        // One-handed mode logic would adjust key positions here
        // For now, we just mark the mode and handle it in the layout
    }

    fun getKeyboardView(): SwipeKeyboardView = keyboardView
}