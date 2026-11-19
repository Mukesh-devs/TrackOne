package com.dev.trackone.feature.expense.sms.data.local.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The detected-transactions database key.
 *
 * A random 256-bit key is generated once and stored only in wrapped form:
 * AES-GCM-encrypted with a non-exportable Android Keystore key. Nothing is
 * hard-coded, and the wrapped value is useless off this device (its prefs file
 * is also excluded from backup).
 */
@Singleton
class DatabaseKeyStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** [isNew]: no usable key existed, so any database file on disk cannot be opened. */
    class DatabaseKey(val passphrase: ByteArray, val isNew: Boolean)

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    @Synchronized
    fun getOrCreateKey(): DatabaseKey {
        readStoredKey()?.let { return DatabaseKey(it, isNew = false) }

        val raw = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        try {
            // commit(), not apply(): the key must be on disk before data is written with it.
            prefs.edit().putString(PREF_WRAPPED_KEY, wrap(raw)).commit()
            return DatabaseKey(rawKeyPassphrase(raw), isNew = true)
        } finally {
            raw.fill(0)
        }
    }

    /** Null when there is no stored key or it can no longer be unwrapped (e.g. Keystore reset). */
    private fun readStoredKey(): ByteArray? {
        val stored = prefs.getString(PREF_WRAPPED_KEY, null) ?: return null
        val raw = try {
            unwrap(stored)
        } catch (e: GeneralSecurityException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
        return try {
            rawKeyPassphrase(raw)
        } finally {
            raw.fill(0)
        }
    }

    private fun wrap(raw: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
        val sealed = cipher.doFinal(raw)
        return encode(cipher.iv) + SEPARATOR + encode(sealed)
    }

    private fun unwrap(stored: String): ByteArray {
        val (iv, sealed) = stored.split(SEPARATOR).takeIf { it.size == 2 }
            ?: throw IllegalArgumentException("malformed wrapped key")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(GCM_TAG_BITS, decode(iv)))
        return cipher.doFinal(decode(sealed)).also {
            if (it.size != KEY_SIZE_BYTES) throw IllegalArgumentException("unexpected key size")
        }
    }

    private fun keystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BYTES * 8)
                .build(),
        )
        return generator.generateKey()
    }

    /**
     * SQLCipher's raw-key form `x'<64 hex>'`: the random key is used as is,
     * skipping the passphrase KDF that a human-chosen password would need.
     */
    private fun rawKeyPassphrase(raw: ByteArray): ByteArray {
        val hex = raw.joinToString(separator = "") { "%02x".format(it) }
        return "x'$hex'".toByteArray(Charsets.US_ASCII)
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    companion object {
        /** Also named in the app's backup exclusion rules — keep them in sync. */
        const val PREFS_NAME = "detected_transactions_key"

        private const val PREF_WRAPPED_KEY = "wrapped_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        internal const val KEYSTORE_ALIAS = "detected_transactions_db"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
        private const val KEY_SIZE_BYTES = 32
        private const val SEPARATOR = ":"
    }
}
