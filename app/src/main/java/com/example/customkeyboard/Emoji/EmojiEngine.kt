package com.example.customkeyboard.Emoji

/** Provides emoji data, categories, and insertion into the active field. */
interface EmojiEngine {

    /** Loads all emoji grouped by [EmojiCategory]. */
    fun loadEmojis(): Map<EmojiCategory, List<Emoji>>

    /** Searches the emoji catalog for [query]. */
    fun searchEmoji(query: String): List<Emoji>

    /** Inserts [emoji] into the focused text field. */
    fun insertEmoji(emoji: Emoji)

    /** Tracks recently used emoji for quick access. */
    fun recentEmoji(limit: Int): List<Emoji>

    data class Emoji(val unicode: String, val name: String, val keywords: List<String>)

    enum class EmojiCategory { SMILEYS, PEOPLE, ANIMALS, FOOD, ACTIVITIES, OBJECTS, SYMBOLS }
}