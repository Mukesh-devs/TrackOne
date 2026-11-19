package com.dev.trackone.feature.expense.sms.data.repository

import java.security.MessageDigest

/**
 * One-way fingerprint of an SMS, so a re-delivered broadcast can be recognised
 * without keeping the message itself.
 */
internal object DedupKey {

    fun of(sender: String, body: String, smsTimestampMillis: Long): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$sender|$body|$smsTimestampMillis".toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
