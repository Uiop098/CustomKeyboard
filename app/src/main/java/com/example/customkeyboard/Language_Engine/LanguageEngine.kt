package com.example.customkeyboard.Language_Engine

/** Manages supported languages, dictionaries, and word prediction. */
interface LanguageEngine {

    /** Activates [language], switching dictionaries and models. */
    fun switchLanguage(language: String)

    /** Returns next-word predictions for [prefix]. */
    fun predictNextWord(prefix: String): List<String>

    /** Suggests corrections for a mistyped [word]. */
    fun spellCheck(word: String): List<String>

    /** Returns the currently active language tag. */
    fun activeLanguage(): String

    companion object {
        const val DEFAULT_LANGUAGE = "en-US"
    }
}