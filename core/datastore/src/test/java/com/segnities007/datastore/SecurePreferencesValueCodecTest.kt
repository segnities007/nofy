package com.segnities007.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SecurePreferencesValueCodecTest {

    @Test
    fun `round trips supported value types`() {
        assertEquals(
            "secret",
            SecurePreferencesValueCodec.decodeString(
                SecurePreferencesValueCodec.encodeString("secret")
            )
        )
        assertEquals(
            true,
            SecurePreferencesValueCodec.decodeBoolean(
                SecurePreferencesValueCodec.encodeBoolean(true)
            )
        )
        assertEquals(
            42,
            SecurePreferencesValueCodec.decodeInt(
                SecurePreferencesValueCodec.encodeInt(42)
            )
        )
        assertEquals(
            42L,
            SecurePreferencesValueCodec.decodeLong(
                SecurePreferencesValueCodec.encodeLong(42L)
            )
        )
        assertEquals(
            1.15f,
            SecurePreferencesValueCodec.decodeFloat(
                SecurePreferencesValueCodec.encodeFloat(1.15f)
            )
        )
    }

    @Test
    fun `returns null for wrong type prefix`() {
        assertNull(SecurePreferencesValueCodec.decodeBoolean("s:secret"))
        assertNull(SecurePreferencesValueCodec.decodeInt("l:12"))
        assertNull(SecurePreferencesValueCodec.decodeLong("b:1"))
        assertNull(SecurePreferencesValueCodec.decodeFloat("i:3"))
    }
}
