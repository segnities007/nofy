package com.segnities007.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeystoreHmacKeyFactory {
    fun generate(
        keyAlias: String,
        configure: KeyGenParameterSpec.Builder.() -> Unit = {}
    ): SecretKey {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                return generate(
                    keyAlias = keyAlias,
                    preferStrongBox = true,
                    configure = configure
                )
            } catch (_: StrongBoxUnavailableException) { }
        }

        return generate(
            keyAlias = keyAlias,
            preferStrongBox = false,
            configure = configure
        )
    }

    private fun generate(
        keyAlias: String,
        preferStrongBox: Boolean,
        configure: KeyGenParameterSpec.Builder.() -> Unit
    ): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
            ANDROID_KEYSTORE
        )
        val builder = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setKeySize(HMAC_KEY_SIZE_BITS)

        if (preferStrongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setIsStrongBoxBacked(true)
        }

        builder.configure()
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val HMAC_KEY_SIZE_BITS = 256
}
