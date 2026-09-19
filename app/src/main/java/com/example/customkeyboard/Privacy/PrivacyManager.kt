package com.example.customkeyboard.Privacy

/** Enforces privacy safeguards: sanitization, masking, and incognito input. */
interface PrivacyManager {

    /** Strips sensitive metadata before committing [text]. */
    fun sanitize(text: String): String

    /** Masks input so on-screen suggestions do not reveal secrets. */
    fun setMaskedInput(enabled: Boolean)

    /** Enables incognito mode (no history, no learning). */
    fun setIncognitoMode(enabled: Boolean)

    /** Returns whether clipboard content is allowed for prediction. */
    fun allowClipboardForPrediction(): Boolean
}