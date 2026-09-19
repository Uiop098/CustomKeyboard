package com.example.customkeyboard.Input_Engine

/** Bridges keyboard events with the active text field's InputConnection. */
interface InputEngine {

    /** Commits [text] to the focused field. */
    fun commitText(text: String)

    /** Deletes [count] characters before the cursor. */
    fun deleteText(count: Int)

    /** Moves the cursor relative to the current position. */
    fun moveCursor(delta: Int)

    /** Performs an action like Enter, Done, or Search. */
    fun performAction(action: Action)

    /** Checks the composed text for spelling/grammar issues before commit. */
    fun grammarCheck(text: String): List<Issue>

    data class Issue(val start: Int, val end: Int, val suggestion: String)

    enum class Action { ENTER, DONE, SEARCH, NEXT, SEND }
}