package com.example.customkeyboard.Clipboard

/** Manages clipboard history, copy, paste, and pinning. */
interface ClipboardEngine {

    /** Copies [text] into the clipboard and history. */
    fun copy(text: String)

    /** Pastes the current clipboard content into the field. */
    fun paste()

    /** Returns recent clipboard entries, newest first. */
    fun history(limit: Int): List<String>

    /** Pins [entryId] so it survives history clearing. */
    fun pin(entryId: String)

    /** Clears history except pinned entries. */
    fun clearHistory()
}