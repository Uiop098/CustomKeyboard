package com.example.customkeyboard.ime

import android.annotation.SuppressLint
import android.content.ClipboardManager as SystemClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customkeyboard.Keyboard_Engine.KeyboardEngine
import com.example.customkeyboard.R
import com.example.customkeyboard.clipboard.ClipAdapter
import com.example.customkeyboard.clipboard.ClipboardHistoryActivity
import com.example.customkeyboard.clipboard.ClipboardManager
import com.example.customkeyboard.data.EmojiData
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.swipe.SwipeGestureDetector
import com.example.customkeyboard.swipe.SwipeKeyboardView
import com.example.customkeyboard.swipe.WordPredictor
import com.example.customkeyboard.theme.ThemeManager
import com.example.customkeyboard.ui.EmojiAdapter
import com.example.customkeyboard.ui.MainActivity
import com.example.customkeyboard.ui.ThemeSettingsActivity
import com.example.customkeyboard.util.SoundManager

@Suppress("DEPRECATION")
class CustomKeyboardIME : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: SwipeKeyboardView
    private lateinit var keyboardEngine: KeyboardEngine
    private lateinit var prefs: Prefs
    private lateinit var soundManager: SoundManager
    private lateinit var themeManager: ThemeManager
    private lateinit var swipeDetector: SwipeGestureDetector
    private lateinit var wordPredictor: WordPredictor
    private lateinit var clipboardManager: ClipboardManager

    private var isCaps = false
    private var isEmojiShowing = false
    private var isQuickClipboardShowing = false
    private var isNumberRowEnabled = false

    private var root: View? = null
    private var keyboardRoot: LinearLayout? = null
    private var emojiContainer: LinearLayout? = null
    private var quickClipboardContainer: LinearLayout? = null
    private var featuresRow: LinearLayout? = null
    private var suggestionsRow: LinearLayout? = null

    private var tvSuggestion1: TextView? = null
    private var tvSuggestion2: TextView? = null
    private var tvSuggestion3: TextView? = null
    private var rvQuickClips: RecyclerView? = null

    private var currentLayoutIndex = 0
    private val layouts = listOf("QWERTY", "AZERTY", "QWERTZ")

    internal fun charForCode(code: Int): String? = when (code) {
        -100, -200, Keyboard.KEYCODE_DELETE, Keyboard.KEYCODE_DONE, Keyboard.KEYCODE_CANCEL,
        Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_SHIFT, 0 -> null
        else -> code.toChar().toString()
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        soundManager = SoundManager(this)
        themeManager = ThemeManager(this)
        wordPredictor = WordPredictor(this)
        clipboardManager = ClipboardManager(this)

        isNumberRowEnabled = prefs.isNumberRowEnabled
        currentLayoutIndex = layouts.indexOf(prefs.keyboardLayout).coerceAtLeast(0)
    }

    override fun onCreateInputView(): View {
        return try {
            createInputView()
        } catch (t: Throwable) {
            android.util.Log.e("CustomKeyboardIME", "onCreateInputView failed", t)
            TextView(this).apply {
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
        root = layoutInflater.inflate(R.layout.keyboard_container, null)
        keyboardRoot = root!!.findViewById(R.id.keyboard_root)
        keyboardView = root!!.findViewById(R.id.keyboard_view)
        emojiContainer = root!!.findViewById(R.id.emoji_container)
        quickClipboardContainer = root!!.findViewById(R.id.quick_clipboard_container)
        featuresRow = root!!.findViewById(R.id.features_row)
        suggestionsRow = root!!.findViewById(R.id.suggestions_row)

        tvSuggestion1 = root!!.findViewById(R.id.tv_suggestion_1)
        tvSuggestion2 = root!!.findViewById(R.id.tv_suggestion_2)
        tvSuggestion3 = root!!.findViewById(R.id.tv_suggestion_3)
        rvQuickClips = root!!.findViewById(R.id.rv_quick_clips)

        keyboardEngine = KeyboardEngine(this, keyboardView)
        applyCurrentLayout()

        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.isPreviewEnabled = false // Disabled to avoid NPE in platform KeyboardView

        swipeDetector = SwipeGestureDetector(
            this,
            keyboardView,
            onSwipeComplete = { touchPoints -> handleSwipeComplete(touchPoints) },
            onGestureDelete = { handleGestureDelete() },
            onGestureCursorMove = { direction -> handleGestureCursorMove(direction) },
            onGestureCapitalize = { handleGestureCapitalize() }
        )

        wordPredictor.setKeyboardView(keyboardView)
        wordPredictor.setPrefs(prefs)

        setupEmojiPicker(root!!)
        setupQuickClipboard(root!!)
        setupFeatureButtons(root!!)
        setupSuggestionButtons()

        applyDimensionsAndTheme()
        updateQuickClipPreview(root!!)

        return root!!
    }

    private fun applyDimensionsAndTheme() {
        val rootLayout = root ?: return
        val theme = themeManager.getCurrentTheme()

        // 1. Apply Height Scale
        val baseHeight = resources.getDimensionPixelSize(R.dimen.key_height) * 4.4f
        val scale = (prefs.keyboardHeightPercent / 100f).coerceIn(0.7f, 1.5f)
        val targetHeight = (baseHeight * scale).toInt()

        val params = keyboardView.layoutParams
        params.height = targetHeight
        keyboardView.layoutParams = params

        // 2. Apply theme to keyboard view
        keyboardView.reloadTheme()

        // 3. Apply theme colors to container rows
        keyboardRoot?.setBackgroundColor(theme.keyboardBgColor)
        rootLayout.findViewById<View>(R.id.top_strip_container)?.setBackgroundColor(theme.topStripBgColor)
        emojiContainer?.setBackgroundColor(theme.keyboardBgColor)
        quickClipboardContainer?.setBackgroundColor(theme.keyboardBgColor)
    }

    private fun applyCurrentLayout() {
        val layout = layouts[currentLayoutIndex]
        val mode = when (layout) {
            "AZERTY" -> if (isNumberRowEnabled) KeyboardEngine.LayoutMode.AZERTY_NUMBERS else KeyboardEngine.LayoutMode.AZERTY
            "QWERTZ" -> if (isNumberRowEnabled) KeyboardEngine.LayoutMode.QWERTZ_NUMBERS else KeyboardEngine.LayoutMode.QWERTZ
            else -> if (isNumberRowEnabled) KeyboardEngine.LayoutMode.QWERTY_NUMBERS else KeyboardEngine.LayoutMode.QWERTY
        }
        keyboardEngine.switchTo(mode)
        prefs.keyboardLayout = layout
    }

    private fun setupFeatureButtons(root: View) {
        root.findViewById<View>(R.id.btn_feature_clipboard).setOnClickListener {
            toggleQuickClipboard(!isQuickClipboardShowing)
        }
        root.findViewById<View>(R.id.btn_feature_settings).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        root.findViewById<View>(R.id.btn_feature_numbers).setOnClickListener {
            isNumberRowEnabled = !isNumberRowEnabled
            prefs.isNumberRowEnabled = isNumberRowEnabled
            applyCurrentLayout()
            Toast.makeText(this, if (isNumberRowEnabled) "Number row enabled" else "Number row hidden", Toast.LENGTH_SHORT).show()
        }
        root.findViewById<View>(R.id.btn_feature_layout).setOnClickListener {
            currentLayoutIndex = (currentLayoutIndex + 1) % layouts.size
            applyCurrentLayout()
            Toast.makeText(this, "Layout: ${layouts[currentLayoutIndex]}", Toast.LENGTH_SHORT).show()
        }
        root.findViewById<View>(R.id.btn_feature_theme).setOnClickListener {
            val intent = Intent(this, ThemeSettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        root.findViewById<View>(R.id.btn_strip_toggle).setOnClickListener {
            toggleSuggestionsRow(true)
        }
        root.findViewById<View>(R.id.btn_sugg_clipboard).setOnClickListener {
            toggleQuickClipboard(true)
        }
    }

    private fun setupSuggestionButtons() {
        val listener = View.OnClickListener { v ->
            val text = (v as TextView).text.toString()
            if (text.isNotEmpty()) {
                replaceCurrentWord(text)
                toggleSuggestionsRow(false)
            }
        }
        tvSuggestion1?.setOnClickListener(listener)
        tvSuggestion2?.setOnClickListener(listener)
        tvSuggestion3?.setOnClickListener(listener)

        root?.findViewById<View>(R.id.btn_back_to_features)?.setOnClickListener {
            toggleSuggestionsRow(false)
        }
    }

    private fun setupEmojiPicker(root: View) {
        val emojiRecycler = root.findViewById<RecyclerView>(R.id.emoji_recycler)
        emojiRecycler.layoutManager = GridLayoutManager(this, 7)
        emojiRecycler.adapter = EmojiAdapter(EmojiData.ALL_EMOJIS) { emoji ->
            currentInputConnection?.commitText(emoji, 1)
            playFeedback(SoundManager.SoundType.CLICK_POP)
        }
        root.findViewById<View>(R.id.btn_back_to_keyboard).setOnClickListener {
            toggleEmojiPicker(false)
        }
    }

    private fun setupQuickClipboard(root: View) {
        val recycler = rvQuickClips ?: return
        recycler.layoutManager = LinearLayoutManager(this)
        reloadQuickClipboardAdapter()

        root.findViewById<View>(R.id.btn_close_quick_clip).setOnClickListener {
            toggleQuickClipboard(false)
        }
        root.findViewById<View>(R.id.btn_open_full_clipboard).setOnClickListener {
            val intent = Intent(this, ClipboardHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun reloadQuickClipboardAdapter() {
        val recycler = rvQuickClips ?: return
        val adapter = ClipAdapter(this, clipboardManager.getItems()) { item, action ->
            when (action) {
                ClipAdapter.Action.PASTE -> {
                    currentInputConnection?.commitText(item.text, 1)
                    playFeedback(SoundManager.SoundType.SPACEBAR)
                    toggleQuickClipboard(false)
                    toggleSuggestionsRow(false)
                }
                ClipAdapter.Action.PIN -> {
                    clipboardManager.togglePin(item.id)
                    reloadQuickClipboardAdapter()
                }
                ClipAdapter.Action.DELETE -> {
                    clipboardManager.deleteItem(item.id)
                    reloadQuickClipboardAdapter()
                }
                else -> {}
            }
        }
        recycler.adapter = adapter
    }

    private fun updateQuickClipPreview(root: View) {
        val tvQuickClip = root.findViewById<TextView>(R.id.tv_quick_clip) ?: return
        val lastClip = clipboardManager.getItems().firstOrNull()
        if (lastClip != null && lastClip.text.isNotEmpty()) {
            tvQuickClip.text = lastClip.getPreview()
            tvQuickClip.visibility = View.VISIBLE
            tvQuickClip.setOnClickListener {
                currentInputConnection?.commitText(lastClip.text, 1)
                playFeedback(SoundManager.SoundType.SPACEBAR)
                toggleSuggestionsRow(false)
            }
        } else {
            tvQuickClip.visibility = View.GONE
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        isCaps = false

        // Reload sounds dynamically
        soundManager.reloadSettings()

        // Sync system clipboard
        try {
            val systemClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? SystemClipboardManager
            clipboardManager.syncFromSystemClipboard(systemClipboard)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Apply updated theme and height
        if (root != null) {
            isNumberRowEnabled = prefs.isNumberRowEnabled
            applyCurrentLayout()
            applyDimensionsAndTheme()
            updateQuickClipPreview(root!!)
        }

        toggleEmojiPicker(false)
        toggleQuickClipboard(false)
        toggleSuggestionsRow(false)
    }

    private fun toggleEmojiPicker(show: Boolean) {
        isEmojiShowing = show
        emojiContainer?.visibility = if (show) View.VISIBLE else View.GONE
        keyboardView.visibility = if (show) View.GONE else View.VISIBLE
        if (show) toggleQuickClipboard(false)
    }

    private fun toggleQuickClipboard(show: Boolean) {
        isQuickClipboardShowing = show
        if (show) reloadQuickClipboardAdapter()
        quickClipboardContainer?.visibility = if (show) View.VISIBLE else View.GONE
        keyboardView.visibility = if (show) View.GONE else View.VISIBLE
        if (show) toggleEmojiPicker(false)
    }

    private fun toggleSuggestionsRow(show: Boolean) {
        featuresRow?.visibility = if (show) View.GONE else View.VISIBLE
        suggestionsRow?.visibility = if (show) View.VISIBLE else View.GONE
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
                updateSuggestions()
            }
            Keyboard.KEYCODE_SHIFT -> {
                playFeedback(SoundManager.SoundType.CLICK_MECHANICAL)
                isCaps = !isCaps
                keyboardView.isShifted = isCaps
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_DONE -> {
                playFeedback(SoundManager.SoundType.RETURN)
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER))
            }
            32 -> { // Space
                playFeedback(SoundManager.SoundType.SPACEBAR)
                ic.commitText(" ", 1)
                toggleSuggestionsRow(false)
            }
            -2 -> { // Switch mode
                playFeedback(SoundManager.SoundType.CLICK_MECHANICAL)
                keyboardEngine.toggleSymbols()
            }
            -100 -> { // Emoji
                playFeedback(SoundManager.SoundType.CLICK_POP)
                toggleEmojiPicker(true)
            }
            else -> {
                playFeedback(SoundManager.SoundType.CLICK_MECHANICAL)
                val text = charForCode(primaryCode) ?: return
                val out = if (isCaps) text.uppercase() else text
                ic.commitText(out, 1)

                if (isCaps && !keyboardView.isShifted) {
                    isCaps = false
                    keyboardView.isShifted = false
                    keyboardView.invalidateAllKeys()
                }
                updateSuggestions()
            }
        }
    }

    private fun updateSuggestions() {
        currentInputConnection ?: return
        val currentWord = getCurrentWord()
        if (currentWord.length >= 2) {
            val suggestions = wordPredictor.getSuggestions(currentWord, 3)
            if (suggestions.isNotEmpty()) {
                tvSuggestion1?.text = if (suggestions.size > 1) suggestions[1] else ""
                tvSuggestion2?.text = suggestions[0] // Center primary
                tvSuggestion3?.text = if (suggestions.size > 2) suggestions[2] else ""
                toggleSuggestionsRow(true)
            }
        } else {
            toggleSuggestionsRow(false)
        }
    }

    private fun getCurrentWord(): String {
        val ic = currentInputConnection ?: return ""
        val before = ic.getTextBeforeCursor(20, 0) ?: ""
        val parts = before.split(Regex("\\s+"))
        return parts.lastOrNull() ?: ""
    }

    private fun replaceCurrentWord(newWord: String) {
        val ic = currentInputConnection ?: return
        val current = getCurrentWord()
        if (current.isNotEmpty()) {
            ic.deleteSurroundingText(current.length, 0)
        }
        ic.commitText(newWord + " ", 1)
    }

    private fun handleGestureDelete() {
        val ic = currentInputConnection ?: return
        val surroundingText = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        val words = surroundingText.trim().split("\\s+".toRegex())
        if (words.isNotEmpty()) {
            ic.deleteSurroundingText(words.last().length + 1, 0)
        }
    }

    private fun handleGestureCursorMove(direction: Int) {
        val ic = currentInputConnection ?: return
        val code = if (direction < 0) android.view.KeyEvent.KEYCODE_DPAD_LEFT else android.view.KeyEvent.KEYCODE_DPAD_RIGHT
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, code))
        ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, code))
    }

    private fun handleGestureCapitalize() {
        isCaps = true
        keyboardView.isShifted = true
        keyboardView.invalidateAllKeys()
    }

    private fun handleSwipeComplete(touchPoints: List<android.graphics.PointF>) {
        val word = wordPredictor.predict(touchPoints)
        if (!word.isNullOrEmpty()) {
            currentInputConnection?.commitText(word + " ", 1)
            playFeedback(SoundManager.SoundType.SPACEBAR)
        }
        keyboardView.clearSwipe()
    }

    private fun playFeedback(soundType: SoundManager.SoundType = SoundManager.SoundType.CLICK_MECHANICAL) {
        soundManager.playKeyClick(soundType)

        if (prefs.isVibrationEnabled) {
            try {
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(prefs.vibrationDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(prefs.vibrationDurationMs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
