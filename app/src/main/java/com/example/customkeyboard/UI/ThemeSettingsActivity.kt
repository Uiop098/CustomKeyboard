package com.example.customkeyboard.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customkeyboard.R
import com.example.customkeyboard.data.Prefs
import com.example.customkeyboard.theme.KeyboardTheme
import com.example.customkeyboard.theme.ThemeManager
import com.google.android.material.switchmaterial.SwitchMaterial

class ThemeSettingsActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private lateinit var themeManager: ThemeManager

    private lateinit var previewBox: View
    private lateinit var previewTopStrip: View
    private lateinit var tvHeightValue: TextView
    private lateinit var tvSpacingValue: TextView
    private lateinit var tvRadiusValue: TextView
    private lateinit var tvBgImageStatus: TextView
    private lateinit var rvPresets: RecyclerView
    private lateinit var presetAdapter: ThemePresetAdapter

    private val pickBgImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val success = themeManager.saveCustomBackgroundImage(it)
            if (success) {
                Toast.makeText(this, "Wallpaper applied successfully!", Toast.LENGTH_SHORT).show()
                presetAdapter.selectedThemeId = "CUSTOM"
                presetAdapter.notifyDataSetChanged()
                updateUIState()
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)

        prefs = Prefs(this)
        themeManager = ThemeManager(this)

        setupViews()
        setupThemePresets()
        updateUIState()
    }

    private fun setupViews() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        previewBox = findViewById(R.id.preview_keyboard_box)
        previewTopStrip = findViewById(R.id.preview_top_strip)
        tvHeightValue = findViewById(R.id.tv_height_value)
        tvSpacingValue = findViewById(R.id.tv_spacing_value)
        tvRadiusValue = findViewById(R.id.tv_radius_value)
        tvBgImageStatus = findViewById(R.id.tv_bg_image_status)
        rvPresets = findViewById(R.id.rv_theme_presets)

        // Height slider
        val seekHeight = findViewById<SeekBar>(R.id.seek_keyboard_height)
        seekHeight.max = 80 // 70% to 150% (progress 0 = 70%, progress 30 = 100%)
        seekHeight.progress = (prefs.keyboardHeightPercent - 70).coerceIn(0, 80)
        tvHeightValue.text = "${prefs.keyboardHeightPercent}%"
        seekHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val percent = progress + 70
                    prefs.keyboardHeightPercent = percent
                    tvHeightValue.text = "$percent%"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Key Spacing slider
        val seekSpacing = findViewById<SeekBar>(R.id.seek_key_spacing)
        seekSpacing.max = 9 // 1dp to 10dp
        seekSpacing.progress = (prefs.keySpacingDp - 1).coerceIn(0, 9)
        tvSpacingValue.text = "${prefs.keySpacingDp} dp"
        seekSpacing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val dp = progress + 1
                    prefs.keySpacingDp = dp
                    tvSpacingValue.text = "$dp dp"
                    updateLivePreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Key Corner Radius slider
        val seekRadius = findViewById<SeekBar>(R.id.seek_key_radius)
        seekRadius.max = 18 // 2dp to 20dp
        seekRadius.progress = (prefs.keyCornerRadiusDp - 2).coerceIn(0, 18)
        tvRadiusValue.text = "${prefs.keyCornerRadiusDp} dp"
        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val dp = progress + 2
                    prefs.keyCornerRadiusDp = dp
                    tvRadiusValue.text = "$dp dp"
                    updateLivePreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Pick background image
        findViewById<Button>(R.id.btn_pick_bg_image).setOnClickListener {
            pickBgImageLauncher.launch("image/*")
        }

        // Remove background image
        findViewById<Button>(R.id.btn_remove_bg_image).setOnClickListener {
            themeManager.removeCustomBackgroundImage()
            Toast.makeText(this, "Custom image removed", Toast.LENGTH_SHORT).show()
            presetAdapter.selectedThemeId = prefs.currentThemeId
            presetAdapter.notifyDataSetChanged()
            updateUIState()
        }

        // Accent Colors
        val colorClicks = mapOf(
            R.id.color_blue to 0xFF3B82F6.toInt(),
            R.id.color_purple to 0xFF8B5CF6.toInt(),
            R.id.color_green to 0xFF10B981.toInt(),
            R.id.color_orange to 0xFFF59E0B.toInt(),
            R.id.color_cyan to 0xFF00E5FF.toInt(),
            R.id.color_pink to 0xFFFF007F.toInt()
        )
        for ((viewId, color) in colorClicks) {
            findViewById<View>(viewId).setOnClickListener {
                prefs.accentColor = color
                Toast.makeText(this, "Accent color updated", Toast.LENGTH_SHORT).show()
                updateUIState()
            }
        }

        // Feature Switches
        val switchNumberRow = findViewById<SwitchMaterial>(R.id.switch_number_row)
        switchNumberRow.isChecked = prefs.isNumberRowEnabled
        switchNumberRow.setOnCheckedChangeListener { _, isChecked ->
            prefs.isNumberRowEnabled = isChecked
        }

        val switchGlass = findViewById<SwitchMaterial>(R.id.switch_glass_mode)
        switchGlass.isChecked = prefs.isGlassModeEnabled
        switchGlass.setOnCheckedChangeListener { _, isChecked ->
            prefs.isGlassModeEnabled = isChecked
            updateUIState()
        }
    }

    private fun setupThemePresets() {
        rvPresets.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        presetAdapter = ThemePresetAdapter(ThemeManager.ALL_THEMES, prefs.currentThemeId) { theme ->
            prefs.currentThemeId = theme.id
            presetAdapter.selectedThemeId = theme.id
            presetAdapter.notifyDataSetChanged()
            updateUIState()
            Toast.makeText(this, "${theme.name} activated", Toast.LENGTH_SHORT).show()
        }
        rvPresets.adapter = presetAdapter
    }

    private fun updateUIState() {
        if (themeManager.hasCustomBackgroundImage()) {
            tvBgImageStatus.text = "Active: Custom Image Wallpaper"
            tvBgImageStatus.setTextColor(0xFF10B981.toInt())
        } else {
            tvBgImageStatus.text = "No custom image loaded"
            tvBgImageStatus.setTextColor(0xFF94A3B8.toInt())
        }
        updateLivePreview()
    }

    private fun updateLivePreview() {
        val theme = themeManager.getCurrentTheme()
        val density = resources.displayMetrics.density
        val radiusPx = prefs.keyCornerRadiusDp * density

        previewBox.setBackgroundColor(theme.keyboardBgColor)
        previewTopStrip.setBackgroundColor(theme.topStripBgColor)

        val keyDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(theme.keyBgColor)
            if (theme.keyBorderWidthDp > 0) {
                setStroke((theme.keyBorderWidthDp * density).toInt(), theme.keyBorderColor)
            }
        }

        val actionKeyDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusPx
            setColor(theme.keyActionBgColor)
            if (theme.keyBorderWidthDp > 0) {
                setStroke((theme.keyBorderWidthDp * density).toInt(), theme.keyBorderColor)
            }
        }

        val keys = listOf(
            findViewById<TextView>(R.id.pk_q),
            findViewById<TextView>(R.id.pk_w),
            findViewById<TextView>(R.id.pk_e),
            findViewById<TextView>(R.id.pk_r),
            findViewById<TextView>(R.id.pk_t),
            findViewById<TextView>(R.id.pk_y),
            findViewById<TextView>(R.id.pk_shift),
            findViewById<TextView>(R.id.pk_space)
        )
        for (k in keys) {
            k?.setTextColor(theme.keyTextColor)
            k?.background = keyDrawable.constantState?.newDrawable()?.mutate() ?: keyDrawable
        }

        val enterKey = findViewById<TextView>(R.id.pk_enter)
        enterKey?.setTextColor(Color.WHITE)
        enterKey?.background = actionKeyDrawable
    }

    inner class ThemePresetAdapter(
        private val themes: List<KeyboardTheme>,
        var selectedThemeId: String,
        private val onSelect: (KeyboardTheme) -> Unit
    ) : RecyclerView.Adapter<ThemePresetAdapter.ThemeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_theme_card, parent, false)
            return ThemeViewHolder(view)
        }

        override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
            val theme = themes[position]
            holder.bind(theme)
        }

        override fun getItemCount(): Int = themes.size

        inner class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val tvName: TextView = view.findViewById(R.id.tv_theme_name)
            private val tvStatus: TextView = view.findViewById(R.id.tv_theme_status)
            private val previewBox: View = view.findViewById(R.id.layout_preview_box)
            private val key1: View = view.findViewById(R.id.v_key_1)
            private val key2: View = view.findViewById(R.id.v_key_2)
            private val key3: View = view.findViewById(R.id.v_key_3)
            private val keySpace: View = view.findViewById(R.id.v_key_space)

            fun bind(theme: KeyboardTheme) {
                tvName.text = theme.name
                val isSelected = theme.id == selectedThemeId

                tvStatus.text = if (isSelected) "Active ✓" else theme.category.title
                tvStatus.setTextColor(if (isSelected) 0xFF10B981.toInt() else 0xFF94A3B8.toInt())

                previewBox.setBackgroundColor(theme.keyboardBgColor)
                key1.setBackgroundColor(theme.keyBgColor)
                key2.setBackgroundColor(theme.keyBgColor)
                key3.setBackgroundColor(theme.keyActionBgColor)
                keySpace.setBackgroundColor(theme.keyBgColor)

                itemView.setOnClickListener { onSelect(theme) }
            }
        }
    }
}
