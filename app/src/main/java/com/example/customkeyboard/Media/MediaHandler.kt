package com.example.customkeyboard.Media

/** Handles media extras: GIFs, stickers, images, and attachments. */
interface MediaHandler {

    /** Returns media of [type] available through the media panel. */
    fun loadMedia(type: MediaType): List<MediaItem>

    /** Sends/commits [item] into the active field. */
    fun send(item: MediaItem)

    /** Opens the panel with the requested media [type]. */
    fun openPanel(type: MediaType)

    data class MediaItem(val id: String, val uri: String, val type: MediaType)

    enum class MediaType { GIF, STICKER, IMAGE, VIDEO, AUDIO }
}