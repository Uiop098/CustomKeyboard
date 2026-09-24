package com.example.customkeyboard.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customkeyboard.R
import com.example.customkeyboard.data.Prefs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ClipboardHistoryActivity : AppCompatActivity() {

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var systemClipboard: ClipboardManager
    private lateinit var rvClips: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var switchAutoCleanup: SwitchMaterial
    private lateinit var tvItemCount: TextView
    private lateinit var adapter: ClipAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard_history)

        clipboardManager = ClipboardManager(this)
        systemClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        setupViews()
        setupRecyclerView()
        loadClips()
    }

    private fun setupViews() {
        rvClips = findViewById(R.id.rv_clips)
        etSearch = findViewById(R.id.et_search)
        switchAutoCleanup = findViewById(R.id.switch_auto_cleanup)
        tvItemCount = findViewById(R.id.tv_item_count)
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        val btnClearUnpinned = findViewById<TextView>(R.id.btn_clear_unpinned)
        val btnImportExport = findViewById<TextView>(R.id.btn_import_export)

        btnBack.setOnClickListener { finish() }
        btnAdd.setOnClickListener { showAddEditDialog(null) }
        btnClearUnpinned.setOnClickListener { showClearUnpinnedDialog() }
        btnImportExport.setOnClickListener { showImportExportDialog() }

        switchAutoCleanup.isChecked = true
        switchAutoCleanup.setOnCheckedChangeListener { _, isChecked ->
            // Auto cleanup is always enabled in manager, but we can add preference later
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = ClipAdapter(this, clipboardManager.getItems()) { item, action ->
            when (action) {
                ClipAdapter.Action.PASTE -> pasteItem(item)
                ClipAdapter.Action.EDIT -> showAddEditDialog(item)
                ClipAdapter.Action.PIN -> togglePin(item)
                ClipAdapter.Action.DELETE -> deleteItem(item)
                ClipAdapter.Action.COPY -> copyToSystemClipboard(item)
            }
        }
        rvClips.layoutManager = LinearLayoutManager(this)
        rvClips.adapter = adapter
    }

    private fun loadClips() {
        adapter.updateItems(clipboardManager.getItems())
        updateItemCount()
    }

    private fun updateItemCount() {
        val items = clipboardManager.getItems()
        val pinned = items.count { it.isPinned }
        val total = items.size
        tvItemCount.text = "$total items ($pinned pinned)"
    }

    private fun pasteItem(item: ClipboardItem) {
        val ic = (applicationContext as? android.inputmethodservice.InputMethodService)?.currentInputConnection
        ic?.commitText(item.text, 1)
        Toast.makeText(this, "Pasted: ${item.getPreview()}", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun copyToSystemClipboard(item: ClipboardItem) {
        val clip = ClipData.newPlainText("clipboard_item", item.text)
        systemClipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to system clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun togglePin(item: ClipboardItem) {
        clipboardManager.togglePin(item.id)
        loadClips()
        val status = if (!item.isPinned) "pinned" else "unpinned"
        Toast.makeText(this, "Item $status", Toast.LENGTH_SHORT).show()
    }

    private fun deleteItem(item: ClipboardItem) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Delete \"${item.getPreview()}\"?")
            .setPositiveButton("Delete") { _, _ ->
                clipboardManager.deleteItem(item.id)
                loadClips()
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddEditDialog(existingItem: ClipboardItem?) {
        val isEditing = existingItem != null
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_clipboard_edit, null)
        dialog.setView(view)

        val tilText = view.findViewById<TextInputLayout>(R.id.til_text)
        val etText = view.findViewById<TextInputEditText>(R.id.et_text)
        val tilLabel = view.findViewById<TextInputLayout>(R.id.til_label)
        val etLabel = view.findViewById<TextInputEditText>(R.id.et_label)
        val tilCategory = view.findViewById<TextInputLayout>(R.id.til_category)
        val etCategory = view.findViewById<TextInputEditText>(R.id.et_category)
        val switchPin = view.findViewById<SwitchMaterial>(R.id.switch_pin)

        if (isEditing) {
            etText.setText(existingItem.text)
            etLabel.setText(existingItem.label)
            etCategory.setText(existingItem.category)
            switchPin.isChecked = existingItem.isPinned
            dialog.setTitle("Edit Clipboard Item")
        } else {
            // Pre-fill with current system clipboard
            val clip = systemClipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (clip != null && clip.isNotEmpty()) {
                etText.setText(clip)
            }
            dialog.setTitle("Add Clipboard Item")
        }

        dialog.setPositiveButton(isEditing ? "Save" : "Add") { _, _ ->
            val text = etText.text.toString().trim()
            val label = etLabel.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val pinned = switchPin.isChecked

            if (text.isEmpty()) {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (isEditing) {
                clipboardManager.updateItem(
                    existingItem.id,
                    newText = text,
                    newLabel = label.ifEmpty { null },
                    newCategory = category.ifEmpty { "General" },
                    newPinned = pinned
                )
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
            } else {
                clipboardManager.addItem(text)
                // Update label/category/pin if provided
                val newItem = clipboardManager.getItems().firstOrNull { it.text == text }
                if (newItem != null && (label.isNotEmpty() || category.isNotEmpty() || pinned)) {
                    clipboardManager.updateItem(
                        newItem.id,
                        newLabel = label.ifEmpty { null },
                        newCategory = category.ifEmpty { "General" },
                        newPinned = pinned
                    )
                }
                Toast.makeText(this, "Added to clipboard history", Toast.LENGTH_SHORT).show()
            }
            loadClips()
        }
        dialog.setNegativeButton("Cancel", null)
        dialog.show()
    }

    private fun showClearUnpinnedDialog() {
        val count = clipboardManager.getItems().count { !it.isPinned }
        if (count == 0) {
            Toast.makeText(this, "No unpinned items to clear", Toast.LENGTH_SHORT).show()
            return
        }
        new AlertDialog.Builder(this)
            .setTitle("Clear Unpinned Items")
            .setMessage("Delete $count unpinned items? Pinned items will be kept.")
            .setPositiveButton("Clear") { _, _ ->
                val deleted = clipboardManager.clearUnpinned()
                loadClips()
                Toast.makeText(this, "$deleted items cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showImportExportDialog() {
        val options = arrayOf("Export to JSON", "Import from JSON")
        AlertDialog.Builder(this)
            .setTitle("Import/Export")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportToFile()
                    1 -> importFromFile()
                }
            }
            .show()
    }

    private fun exportToFile() {
        val json = clipboardManager.exportToJson()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "clipboard_history_${System.currentTimeMillis()}.json")
        }
        startActivityForResult(intent, 1001)
        // Note: In real implementation, use ActivityResultContracts.CreateDocument
        // For simplicity, we'll just copy to clipboard
        val clip = ClipData.newPlainText("clipboard_export", json)
        systemClipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Exported JSON copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun importFromFile() {
        // Simplified: paste from clipboard
        val clipText = systemClipboard.primaryClip?.getItemAt(0)?.text?.toString()
        if (clipText == null || clipText.isEmpty()) {
            Toast.makeText(this, "No JSON in clipboard", Toast.LENGTH_SHORT).show()
            return
        }
        val imported = clipboardManager.importFromJson(clipText)
        loadClips()
        Toast.makeText(this, "Imported $imported items", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadClips()
    }
}