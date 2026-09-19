package com.example.customkeyboard

import com.example.customkeyboard.ime.charForCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeyCodeResolverTest {

    @Test
    fun actionCodesReturnNull() {
        assertNull(charForCode(-100))
        assertNull(charForCode(-200)) // <= symbols
        assertNull(charForCode(-5)) // delete
        assertNull(charForCode(-4)) // done
        assertNull(charForCode(-3)) // cancel
        assertNull(charForCode(-2)) // mode switch
        assertNull(charForCode(-1)) // shift
        assertNull(charForCode(0))
    }

    @Test
    fun letterCodesMapToText() {
        assertEquals("a", charForCode('a'.code))
        assertEquals("A", charForCode('A'.code))
        assertEquals(" ", charForCode(' '.code))
    }

    @Test
    fun symbolCodesMapToText() {
        assertEquals("@", charForCode(64))
        assertEquals("#", charForCode(35))
        assertEquals("&", charForCode(38))
        assertEquals(",", charForCode(44))
        assertEquals("\u000A", charForCode(10)) // enter
    }
}