package com.example.customkeyboard.Keyboard_Engine

import android.content.Context
import android.inputmethodservice.Keyboard
import com.example.customkeyboard.R
import com.example.customkeyboard.swipe.SwipeKeyboardView

class KeyboardEngine(private val context: Context, private val keyboardView: SwipeKeyboardView) {

    val qwertyKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwerty)
    val qwertyNumbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwerty_numbers)
    val azertyKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_azerty)
    val azertyNumbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_azerty_numbers)
    val qwertzKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwertz)
    val qwertzNumbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwertz_numbers)
    val symbolsKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_symbols)
    val numbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_numbers)

    var currentMode = LayoutMode.QWERTY
        private set

    enum class LayoutMode {
        QWERTY, QWERTY_NUMBERS,
        AZERTY, AZERTY_NUMBERS,
        QWERTZ, QWERTZ_NUMBERS,
        SYMBOLS, NUMBERS
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
            LayoutMode.QWERTY_NUMBERS -> keyboardView.keyboard = qwertyNumbersKeyboard
            LayoutMode.AZERTY -> keyboardView.keyboard = azertyKeyboard
            LayoutMode.AZERTY_NUMBERS -> keyboardView.keyboard = azertyNumbersKeyboard
            LayoutMode.QWERTZ -> keyboardView.keyboard = qwertzKeyboard
            LayoutMode.QWERTZ_NUMBERS -> keyboardView.keyboard = qwertzNumbersKeyboard
            LayoutMode.SYMBOLS -> keyboardView.keyboard = symbolsKeyboard
            LayoutMode.NUMBERS -> keyboardView.keyboard = numbersKeyboard
        }
        applyOneHandedMode()
        keyboardView.invalidateAllKeys()
    }

    fun toggleSymbols() {
        if (currentMode != LayoutMode.SYMBOLS) {
            switchTo(LayoutMode.SYMBOLS)
        } else {
            // Revert to primary based on prefs or default
            switchTo(LayoutMode.QWERTY)
        }
    }

    fun toggleNumbers() {
        if (currentMode != LayoutMode.NUMBERS) {
            switchTo(LayoutMode.NUMBERS)
        } else {
            switchTo(LayoutMode.QWERTY)
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
    }

    fun getKeyboardView(): SwipeKeyboardView = keyboardView
}
