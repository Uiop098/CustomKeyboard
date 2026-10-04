package com.example.customkeyboard.clipboard

import android.content.ClipData
import android.content.ClipboardManager as SystemClipboardManager
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.UUID

/**
 * Data model for clipboard items.
 */
data class ClipboardItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    var isPinned: Boolean = false,
    var label: String = "",
    var category: String = "General"
) {
    fun getPreview(): String {
        val trimmed = text.trim()
        return if (trimmed.length > 50) "${trimmed.substring(0, 50)}…" else trimmed
    }

    fun getDisplayText(): String {
        return if (label.isNotEmpty()) label else getPreview()
    }
}

/**
 * Manages clipboard history with persistence, auto-cleanup, pin, search, and system clipboard sync.
 */
class ClipboardManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("clipboard_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val maxItems = 100
    private val maxAgeDays = 30
    private val autoCleanupEnabled = true

    private var cachedItems: List<ClipboardItem>? = null

    init {
        migrateLegacyData()
    }

    private fun migrateLegacyData() {
        val legacyJson = prefs.getString("clipboard_history", null)
        if (!legacyJson.isNullOrEmpty()) {
            try {
                val legacyArray = gson.fromJson(legacyJson, Array<ClipboardItem>::class.java)
                val legacyItems = legacyArray?.toList() ?: emptyList()
                saveItems(legacyItems)
                prefs.edit().remove("clipboard_history").apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Synchronizes any new text from the Android system clipboard.
     */
    fun syncFromSystemClipboard(systemClipboard: SystemClipboardManager?): ClipboardItem? {
        if (systemClipboard == null) return null
        return try {
            if (systemClipboard.hasPrimaryClip()) {
                val clip = systemClipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val clipText = clip.getItemAt(0)?.text?.toString()?.trim()
                    if (!clipText.isNullOrEmpty()) {
                        val currentFirst = getItems().firstOrNull()?.text?.trim()
                        if (currentFirst != clipText) {
                            return addItem(clipText)
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get all clipboard items (pinned first, then by timestamp desc).
     */
    fun getItems(): List<ClipboardItem> {
        cachedItems?.let { return it }

        val json = prefs.getString("clipboard_items", null)
        if (json.isNullOrEmpty()) {
            cachedItems = emptyList()
            return cachedItems!!
        }

        return try {
            val itemsArray = gson.fromJson(json, Array<ClipboardItem>::class.java)
            val items = itemsArray?.toList() ?: emptyList()
            cachedItems = sortItems(items)
            cachedItems!!
        } catch (e: Exception) {
            e.printStackTrace()
            cachedItems = emptyList()
            cachedItems!!
        }
    }

    private fun sortItems(items: List<ClipboardItem>): List<ClipboardItem> {
        return items.sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
    }

    /**
     * Add a new clipboard item (auto-deduplicate).
     */
    fun addItem(text: String): ClipboardItem {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return ClipboardItem(text = "")

        val items = getItems().toMutableList()

        // Remove exact duplicates to move to top
        items.removeAll { it.text == cleanText }

        val newItem = ClipboardItem(text = cleanText)
        items.add(0, newItem)

        if (autoCleanupEnabled) {
            cleanup(items)
        }

        if (items.size > maxItems) {
            val pinned = items.filter { it.isPinned }
            val unpinned = items.filter { !it.isPinned }
            val toKeep = pinned + unpinned.take((maxItems - pinned.size).coerceAtLeast(0))
            saveItems(toKeep)
        } else {
            saveItems(items)
        }

        return newItem
    }

    /**
     * Update an existing clipboard item.
     */
    fun updateItem(
        id: String,
        newText: String? = null,
        newLabel: String? = null,
        newCategory: String? = null,
        newPinned: Boolean? = null
    ): Boolean {
        val items = getItems().toMutableList()
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return false

        val oldItem = items[index]
        val updatedItem = oldItem.copy(
            text = newText ?: oldItem.text,
            label = newLabel ?: oldItem.label,
            category = newCategory ?: oldItem.category,
            isPinned = newPinned ?: oldItem.isPinned
        )
        items[index] = updatedItem
        saveItems(items)
        return true
    }

    /**
     * Toggle pin status.
     */
    fun togglePin(id: String): Boolean {
        val items = getItems().toMutableList()
        val index = items.indexOfFirst { it.id == id }
        if (index == -1) return false

        val oldItem = items[index]
        items[index] = oldItem.copy(isPinned = !oldItem.isPinned)
        saveItems(items)
        return true
    }

    /**
     * Delete a clipboard item.
     */
    fun deleteItem(id: String): Boolean {
        val items = getItems().toMutableList()
        val removed = items.removeIf { it.id == id }
        if (removed) saveItems(items)
        return removed
    }

    /**
     * Clear all unpinned items.
     */
    fun clearUnpinned(): Int {
        val items = getItems().filter { it.isPinned }
        val deleted = getItems().size - items.size
        saveItems(items)
        return deleted
    }

    /**
     * Clear all items.
     */
    fun clearAll() {
        saveItems(emptyList())
    }

    /**
     * Search items.
     */
    fun search(query: String): List<ClipboardItem> {
        if (query.trim().isEmpty()) return getItems()
        val lowerQuery = query.lowercase()
        return getItems().filter { item ->
            item.text.lowercase().contains(lowerQuery) ||
            item.label.lowercase().contains(lowerQuery) ||
            item.category.lowercase().contains(lowerQuery)
        }
    }

    private fun cleanup(items: MutableList<ClipboardItem>) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
        items.removeIf { !it.isPinned && it.timestamp < cutoffTime }
    }

    private fun saveItems(items: List<ClipboardItem>) {
        cachedItems = sortItems(items)
        prefs.edit().putString("clipboard_items", gson.toJson(cachedItems)).apply()
    }

    fun exportToJson(): String {
        return gson.toJson(getItems())
    }

    fun importFromJson(json: String): Int {
        return try {
            val importedArray = gson.fromJson(json, Array<ClipboardItem>::class.java)
            val imported = importedArray?.toList() ?: emptyList()
            val existing = getItems().toMutableList()

            for (item in imported) {
                if (!existing.any { it.text == item.text }) {
                    existing.add(0, item)
                }
            }

            if (existing.size > maxItems) {
                val pinned = existing.filter { it.isPinned }
                val unpinned = existing.filter { !it.isPinned }
                saveItems(pinned + unpinned.take((maxItems - pinned.size).coerceAtLeast(0)))
            } else {
                saveItems(existing)
            }

            imported.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
