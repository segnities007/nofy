package com.segnities007.datastore

internal object SecurePreferencesValueCodec {
    private const val STRING_PREFIX = "s:"
    private const val BOOLEAN_PREFIX = "b:"
    private const val INT_PREFIX = "i:"
    private const val LONG_PREFIX = "l:"
    private const val FLOAT_PREFIX = "f:"

    fun encodeString(value: String): String = STRING_PREFIX + value

    fun decodeString(payload: String): String? {
        return payload.takeIf { it.startsWith(STRING_PREFIX) }?.removePrefix(STRING_PREFIX)
    }

    fun encodeBoolean(value: Boolean): String = BOOLEAN_PREFIX + if (value) "1" else "0"

    fun decodeBoolean(payload: String): Boolean? {
        val value = payload.takeIf { it.startsWith(BOOLEAN_PREFIX) }
            ?.removePrefix(BOOLEAN_PREFIX)
            ?: return null
        return when (value) {
            "1" -> true
            "0" -> false
            else -> null
        }
    }

    fun encodeInt(value: Int): String = INT_PREFIX + value

    fun decodeInt(payload: String): Int? {
        return payload.takeIf { it.startsWith(INT_PREFIX) }
            ?.removePrefix(INT_PREFIX)
            ?.toIntOrNull()
    }

    fun encodeLong(value: Long): String = LONG_PREFIX + value

    fun decodeLong(payload: String): Long? {
        return payload.takeIf { it.startsWith(LONG_PREFIX) }
            ?.removePrefix(LONG_PREFIX)
            ?.toLongOrNull()
    }

    fun encodeFloat(value: Float): String = FLOAT_PREFIX + value

    fun decodeFloat(payload: String): Float? {
        return payload.takeIf { it.startsWith(FLOAT_PREFIX) }
            ?.removePrefix(FLOAT_PREFIX)
            ?.toFloatOrNull()
    }
}
