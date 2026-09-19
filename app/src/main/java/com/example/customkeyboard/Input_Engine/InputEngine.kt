package com.example.customkeyboard.Input_Engine

import android.view.inputmethod.InputConnection

class InputEngine {
    // Goose (uiop098-3) implementation
    fun handleTapTyping(ic: InputConnection, text: String, isCaps: Boolean) {
        val out = if (text.isNotEmpty() && Character.isLetter(text[0]) && isCaps) text.uppercase() else text
        ic.commitText(out, 1)
    }

    fun handleAutocorrectStub(word: String): String {
        return word // Autocorrect passes through currently
    }
}
