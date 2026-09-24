package com.example.customkeyboard.clipboard

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

/**
 * Data model for clipboard items
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
        return if (text.length > 50) "${text.substring(0, 50)}..." else text
    }

    fun getDisplayText(): String {
        return if (label.isNotEmpty()) label else getPreview()
    }
}

/**
 * Manages clipboard history with persistence, auto-cleanup, pin, and edit features
 */
class ClipboardManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("clipboard_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type: Type = object : TypeToken<ArrayList<ClipboardItem>>() {}.type

    // Configuration
    private val maxItems = 100
    private val maxAgeDays = 30
    private val autoCleanupEnabled = true

    // In-memory cache
    private var cachedItems: List<ClipboardItem>? = null

    init {
        migrateLegacyData()
    }

    private fun migrateLegacyData() {
        // Check if we have legacy data in old format
        val legacyJson = prefs.getString("clipboard_history", null)
        if (legacyJson != null && legacyJson.isNotEmpty()) {
            try {
                val legacyItems = gson.fromJson(legacyJson, type) ?: emptyList()
                saveItems(legacyItems)
                prefs.edit().remove("clipboard_history").apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Get all clipboard items (pinned first, then by timestamp desc)
     */
    fun getItems(): List<ClipboardItem> {
        if (cachedItems != null) return cachedItems!!
        
        val json = prefs.getString("clipboard_items", null)
        if (json == null || json.isEmpty()) {
            cachedItems = emptyList()
            return cachedItems!!
        }
        
        try {
            val items = gson.fromJson(json, type) ?: emptyList()
            cachedItems = sortItems(items)
            return cachedItems!!
        } catch (e: Exception) {
            e.printStackTrace()
            cachedItems = emptyList()
            return cachedItems!!
        }
    }

    private fun sortItems(items: List<ClipboardItem>): List<ClipboardItem> {
        return items.sortedWith(
            compareByDescending<ClipboardItem> { it.isPinned }
                .thenByDescending { it.timestamp }
        )
    }

    /**
     * Add a new clipboard item (auto-dedupe)
     */
    fun addItem(text: String): ClipboardItem {
        if (text.trim().isEmpty()) return ClipboardItem(text = "")
        
        val items = getItems().toMutableList()
        
        // Remove exact duplicates
        items.removeAll { it.text == text.trim() }
        
        // Create new item
        val newItem = ClipboardItem(text = text.trim())
        items.add(0, newItem)
        
        // Auto cleanup
        if (autoCleanupEnabled) {
            cleanup(items)
        }
        
        // Enforce max items (keep pinned)
        if (items.size > maxItems) {
            val pinned = items.filter { it.isPinned }
            val unpinned = items.filter { !it.isPinned }
            val toKeep = pinned + unpinned.take(maxItems - pinned.size)
            saveItems(toKeep)
        } else {
            saveItems(items)
        }
        
        return newItem
    }

    /**
     * Update an existing clipboard item (edit text, label, category, pin status)
     */
    fun updateItem(id: String, newText: String? = null, newLabel: String? = null, 
                   newCategory: String? = null, newPinned: Boolean? = null): Boolean {
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
     * Toggle pin status
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
     * Delete a clipboard item
     */
    fun deleteItem(id: String): Boolean {
        val items = getItems().toMutableList()
        val removed = items.removeIf { it.id == id }
        if (removed) saveItems(items)
        return removed
    }

    /**
     * Delete multiple items
     */
    fun deleteItems(ids: Set<String>): Int {
        val items = getItems().toMutableList()
        val initialSize = items.size
        items.removeAll { it.id in ids }
        val deleted = initialSize - items.size
        if (deleted > 0) saveItems(items)
        return deleted
    }

    /**
     * Clear all unpinned items
     */
    fun clearUnpinned(): Int {
        val items = getItems().filter { it.isPinned }
        val deleted = getItems().size - items.size
        saveItems(items)
        return deleted
    }

    /**
     * Clear all items
     */
    fun clearAll() {
        saveItems(emptyList())
    }

    /**
     * Search items by text
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

    /**
     * Get items by category
     */
    fun getByCategory(category: String): List<ClipboardItem> {
        return getItems().filter { it.category == category }
    }

    /**
     * Get all categories
     */
    fun getCategories(): List<String> {
        return getItems().map { it.category }.distinct().sorted()
    }

    /**
     * Auto cleanup: remove old unpinned items
     */
    private fun cleanup(items: MutableList<ClipboardItem>) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
        items.removeIf { !it.isPinned && it.timestamp < cutoffTime }
    }

    private fun saveItems(items: List<ClipboardItem>) {
        cachedItems = sortItems(items)
        prefs.edit().putString("clipboard_items", gson.toJson(cachedItems)).apply()
    }

    /**
     * Get item by ID
     */
    fun getItem(id: String): ClipboardItem? {
        return getItems().firstOrNull { it.id == id }
    }

    /**
     * Export clipboard history as JSON
     */
    fun exportToJson(): String {
        return gson.toJson(getItems())
    }

    /**
     * Import clipboard history from JSON
     */
    fun importFromJson(json: String): Int {
        try {
            val imported = gson.fromJson(json, type) ?: emptyList()
            val existing = getItems().toMutableList()
            
            // Merge avoiding duplicates
            for (item in imported) {
                if (!existing.any { it.text == item.text }) {
                    existing.add(0, item)
                }
            }
            
            if (existing.size > maxItems) {
                val pinned = existing.filter { it.isPinned }
                val unpinned = existing.filter { !it.isPinned }
                saveItems(pinned + unpinned.take(maxItems - pinned.size))
            } else {
                saveItems(existing)
            }
            
            return imported.size
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
}