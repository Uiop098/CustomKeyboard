package com.example.customkeyboard

import com.example.customkeyboard.ime.CustomKeyboardIME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyCodeResolverTest {
    private val ime = CustomKeyboardIME()

    @Test
    fun actionCodesReturnNull() {
        assertNull(ime.charForCode(-100))
        assertNull(ime.charForCode(-200)) // <= symbols
        assertNull(ime.charForCode(-5)) // delete
        assertNull(ime.charForCode(-4)) // done
        assertNull(ime.charForCode(-3)) // cancel
        assertNull(ime.charForCode(-2)) // mode switch
        assertNull(ime.charForCode(-1)) // shift
        assertNull(ime.charForCode(0))
    }

    @Test
    fun letterCodesMapToText() {
        assertEquals("a", ime.charForCode('a'.code))
        assertEquals("A", ime.charForCode('A'.code))
        assertEquals(" ", ime.charForCode(' '.code))
    }

    @Test
    fun symbolCodesMapToText() {
        assertEquals("@", ime.charForCode(64))
        assertEquals("#", ime.charForCode(35))
        assertEquals("&", ime.charForCode(38))
        assertEquals(",", ime.charForCode(44))
        assertEquals("\u000A", ime.charForCode(10)) // enter
    }
}