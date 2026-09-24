package com.example.customkeyboard.ime

import android.annotation.SuppressLint
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customkeyboard.R
import com.example.customkeyboard.Keyboard_Engine.KeyboardEngine
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.swipe.SwipeGestureDetector
import com.example.customkeyboard.swipe.SwipeKeyboardView
import com.example.customkeyboard.swipe.WordPredictor
import com.example.customkeyboard.util.SoundManager
import com.example.customkeyboard.ui.EmojiAdapter
import com.example.customkeyboard.data.EmojiData

/** Maps a key code to the text it commits, or null for action/control keys. */
fun charForCode(code: Int): String? = when (code) {
    -100, -200, Keyboard.KEYCODE_DELETE, Keyboard.KEYCODE_DONE, Keyboard.KEYCODE_CANCEL,
    Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_SHIFT, 0 -> null
    else -> code.toChar().toString()
}

@Suppress("DEPRECATION")
class CustomKeyboardIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: SwipeKeyboardView
    private lateinit var keyboardEngine: KeyboardEngine
    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager
    private lateinit var swipeDetector: SwipeGestureDetector
    private lateinit var wordPredictor: WordPredictor

    private var isCaps = false
    private var isEmojiShowing = false
    private var emojiContainer: LinearLayout? = null
    private var isSwipeEnabled = true

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        soundManager = SoundManager(this)
        wordPredictor = WordPredictor(this)
    }

    override fun onCreateInputView(): View {
        return try {
            createInputView()
        } catch (t: Throwable) {
            android.util.Log.e("CustomKeyboardIME", "onCreateInputView failed", t)
            android.widget.TextView(this).apply {
                text = getString(R.string.keyboard_load_failed, t.javaClass.simpleName)
                setTextColor(0xFFF8FAFC.toInt())
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(24, 24, 24, 24)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_container, null)
        keyboardView = root.findViewById(R.id.keyboard_view)
        emojiContainer = root.findViewById(R.id.emoji_container)

        keyboardEngine = KeyboardEngine(this, keyboardView)
        keyboardEngine.switchTo(KeyboardEngine.LayoutMode.QWERTY)

        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.isPreviewEnabled = prefs.isPopupPreviewEnabled

        // Initialize swipe detector
        swipeDetector = SwipeGestureDetector(this, keyboardView) { touchPoints ->
            handleSwipeComplete(touchPoints)
        }

        // Initialize word predictor with keyboard view
        wordPredictor.setKeyboardView(keyboardView)

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

        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                playFeedback(SoundManager.SoundType.DELETE)
                val selectedText = ic.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DEL))
                    ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DEL))
                } else {
                    ic.commitText("", 1)
                }
            }
            Keyboard.KEYCODE_SHIFT -> {
                playFeedback(SoundManager.SoundType.CLICK_SOFT)
                isCaps = !isCaps
                keyboardEngine.qwertyKeyboard.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                playFeedback(SoundManager.SoundType.RETURN)
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
            }
            32 -> { // Space key
                playFeedback(SoundManager.SoundType.SPACEBAR)
                ic.commitText(" ", 1)
            }
            -2 -> { // Switch between QWERTY and Symbols
                playFeedback(SoundManager.SoundType.CLICK_SOFT)
                keyboardEngine.toggleSymbols()
            }
            -200 -> { // '=\<' navigation key action
                playFeedback()
                ic.commitText("=<", 1)
            }
            -100 -> { // Toggle Emoji Window
                playFeedback(SoundManager.SoundType.CLICK_POP)
                toggleEmojiPicker(true)
            }
            else -> {
                playFeedback()
                val text = charForCode(primaryCode) ?: return
                val out = if (text.isNotEmpty() && Character.isLetter(text[0]) && isCaps) {
                    text.uppercase()
                } else {
                    text
                }
                ic.commitText(out, 1)
                if (isCaps) {
                    isCaps = false
                    keyboardEngine.qwertyKeyboard.isShifted = false
                    keyboardView.invalidateAllKeys()
                }
            }
        }
    }

    private fun handleSwipeComplete(touchPoints: List<android.graphics.PointF>) {
        if (!isSwipeEnabled) return

        val predictedWord = wordPredictor.predict(touchPoints)
        if (predictedWord != null && predictedWord.isNotEmpty()) {
            val ic = currentInputConnection ?: return
            // Add space before the word if not at start
            val surroundingText = ic.getSurroundingText(1, 0)
            val surroundingStr = surroundingText?.text?.toString() ?: ""
            val prefix = if (surroundingStr.isNotEmpty() && !surroundingStr.endsWith(" ")) {
                " "
            } else {
                ""
            }
            val finalText = "$prefix$predictedWord"
            ic.commitText(finalText, 1)
            playFeedback(SoundManager.SoundType.CLICK_MECHANICAL)
            
            // Show feedback
            showSwipeFeedback(predictedWord)
        }
        
        // Clear swipe trail
        keyboardView.clearSwipe()
    }

    private fun showSwipeFeedback(word: String) {
        // Could show a brief toast or suggestion bar
        // For now just log it
        android.util.Log.d("SwipeTyping", "Predicted: $word")
    }

    private fun toggleEmojiPicker(show: Boolean) {
        isEmojiShowing = show
        emojiContainer?.visibility = if (show) View.VISIBLE else View.GONE
        keyboardView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun playFeedback(soundType: SoundManager.SoundType = SoundManager.SoundType.CLICK_MECHANICAL) {
        if (prefs.isSoundEnabled) {
            soundManager.playKeyClick(soundType)
        }
        if (prefs.isVibrationEnabled) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(prefs.vibrationDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(prefs.vibrationDurationMs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
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