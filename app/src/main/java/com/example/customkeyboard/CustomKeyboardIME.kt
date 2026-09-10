package com.example.customkeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomKeyboardIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var symbolsKeyboard: Keyboard
    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager

    private var isCaps = false
    private var isSymbols = false
    private var isEmojiShowing = false
    private var emojiContainer: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        soundManager = SoundManager(this)
    }

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_container, null)
        keyboardView = root.findViewById(R.id.keyboard_view)
        emojiContainer = root.findViewById(R.id.emoji_container)

        qwertyKeyboard = Keyboard(this, R.xml.keyboard_qwerty)
        symbolsKeyboard = Keyboard(this, R.xml.keyboard_symbols)

        keyboardView.keyboard = qwertyKeyboard
        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.isPreviewEnabled = prefs.isPopupPreviewEnabled

        setupEmojiPicker(root)
        return root
    }

    private fun setupEmojiPicker(root: View) {
        val emojiRecycler = root.findViewById<RecyclerView>(R.id.emoji_recycler)
        emojiRecycler.layoutManager = GridLayoutManager(this, 7)
        val adapter = EmojiAdapter(EmojiData.ALL_EMOJIS) { emoji ->
            currentInputConnection?.commitText(emoji, 1)
            playFeedback()
        }
        emojiRecycler.adapter = adapter

        root.findViewById<View>(R.id.btn_back_to_keyboard).setOnClickListener {
            toggleEmojiPicker(false)
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic: InputConnection = currentInputConnection ?: return
        playFeedback()

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                val selectedText = ic.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            Keyboard.KEYCODE_SHIFT -> {
                isCaps = !isCaps
                qwertyKeyboard.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            }
            -2 -> { // Switch between QWERTY and Symbols
                isSymbols = !isSymbols
                keyboardView.keyboard = if (isSymbols) symbolsKeyboard else qwertyKeyboard
                keyboardView.invalidateAllKeys()
            }
            -100 -> { // Toggle Emoji Window
                toggleEmojiPicker(true)
            }
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && isCaps) {
                    code = code.uppercaseChar()
                }
                ic.commitText(code.toString(), 1)
            }
        }
    }

    private fun toggleEmojiPicker(show: Boolean) {
        isEmojiShowing = show
        emojiContainer?.visibility = if (show) View.VISIBLE else View.GONE
        keyboardView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun playFeedback() {
        if (prefs.isSoundEnabled) {
            soundManager.playKeyClick()
        }
        if (prefs.isVibrationEnabled) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(prefs.vibrationDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(prefs.vibrationDurationMs)
            }
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {
        currentInputConnection?.commitText(text, 1)
    }
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
