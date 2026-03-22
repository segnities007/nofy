package com.segnities007.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SecurePreferencesValueCodecTest {
    @Test
    fun encodeAndDecodeString_roundTrips() {
        val payload = SecurePreferencesValueCodec.encodeString("vault-state")

        assertEquals("vault-state", SecurePreferencesValueCodec.decodeString(payload))
    }

    @Test
    fun decodeBoolean_rejectsUnexpectedValue() {
        assertNull(SecurePreferencesValueCodec.decodeBoolean("b:2"))
    }

    @Test
    fun decodeInt_rejectsWrongPrefix() {
        assertNull(SecurePreferencesValueCodec.decodeInt("s:42"))
    }

    @Test
    fun encodeAndDecodeLong_roundTrips() {
        val payload = SecurePreferencesValueCodec.encodeLong(123456789L)

        assertEquals(123456789L, SecurePreferencesValueCodec.decodeLong(payload))
    }
}
