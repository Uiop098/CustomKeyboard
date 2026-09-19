package com.example.customkeyboard.Keyboard_Engine

import android.content.Context
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import com.example.customkeyboard.R

class KeyboardEngine(private val context: Context, private val keyboardView: KeyboardView) {
    
    val qwertyKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_qwerty)
    val symbolsKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_symbols)
    
    // Future extension for numbers or region layouts
    // val numbersKeyboard: Keyboard = Keyboard(context, R.xml.keyboard_numbers)

    var currentMode = LayoutMode.QWERTY
        private set

    enum class LayoutMode {
        QWERTY, SYMBOLS, NUMBERS
    }

    fun switchTo(mode: LayoutMode) {
        currentMode = mode
        when (mode) {
            LayoutMode.QWERTY -> keyboardView.keyboard = qwertyKeyboard
            LayoutMode.SYMBOLS -> keyboardView.keyboard = symbolsKeyboard
            LayoutMode.NUMBERS -> { /* To loop numbers layout later */ }
        }
        keyboardView.invalidateAllKeys()
    }
    
    fun toggleSymbols() {
        if (currentMode == LayoutMode.QWERTY) {
            switchTo(LayoutMode.SYMBOLS)
        } else {
            switchTo(LayoutMode.QWERTY)
        }
    }
}
