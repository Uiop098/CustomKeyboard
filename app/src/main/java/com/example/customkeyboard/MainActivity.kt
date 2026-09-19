package com.example.customkeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = Prefs(this)

        val btnEnable = findViewById<Button>(R.id.btn_enable_keyboard)
        val btnSelect = findViewById<Button>(R.id.btn_select_keyboard)
        val switchSound = findViewById<SwitchMaterial>(R.id.switch_sound)
        val switchVibrate = findViewById<SwitchMaterial>(R.id.switch_vibrate)
        val switchPopup = findViewById<SwitchMaterial>(R.id.switch_popup)
        val crashBanner = findViewById<TextView>(R.id.tv_crash_report)
        val crashClear = findViewById<Button>(R.id.btn_clear_crash)

        // If a crash was captured on a previous run, surface it so it can be diagnosed.
        val crashLog = CrashLogger.readLog(this)
        if (crashLog.isNotEmpty()) {
            crashBanner.text = "Last crash:\n$crashLog"
            crashBanner.visibility = TextView.VISIBLE
            crashClear.visibility = Button.VISIBLE
        }
        crashClear.setOnClickListener {
            CrashLogger.clearLog(this)
            crashBanner.visibility = TextView.GONE
            crashClear.visibility = Button.GONE
        }

        // Enable in Android Settings
        btnEnable.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivity(intent)
        }

        // Open input method picker dialog
        btnSelect.setOnClickListener {
            try {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            } catch (e: Exception) {
                Log.e("MainActivity", "showInputMethodPicker failed", e)
            }
        }

        // Sound Preference
        switchSound.isChecked = prefs.isSoundEnabled
        switchSound.setOnCheckedChangeListener { _, isChecked ->
            prefs.isSoundEnabled = isChecked
        }

        // Vibration Preference
        switchVibrate.isChecked = prefs.isVibrationEnabled
        switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            prefs.isVibrationEnabled = isChecked
        }

        // Popup Preview Preference
        switchPopup.isChecked = prefs.isPopupPreviewEnabled
        switchPopup.setOnCheckedChangeListener { _, isChecked ->
            prefs.isPopupPreviewEnabled = isChecked
        }
    }

    override fun onResume() {
        super.onResume()
        updateKeyboardStatus()
    }

    private fun updateKeyboardStatus() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val enabledList = imm.enabledInputMethodList
            val isEnabled = enabledList.any { it.packageName == packageName }

            val statusText = findViewById<TextView>(R.id.tv_ime_status)
            if (isEnabled) {
                statusText.text = "Keyboard Status: ENABLED (Ready to type)"
                statusText.setTextColor(getColor(R.color.accent_green))
            } else {
                statusText.text = "Keyboard Status: NOT ENABLED (Tap Step 1 above)"
                statusText.setTextColor(getColor(R.color.accent_orange))
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "updateKeyboardStatus failed", e)
        }
    }
}