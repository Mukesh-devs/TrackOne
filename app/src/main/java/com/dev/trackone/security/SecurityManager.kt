package com.dev.trackone.security

import java.security.MessageDigest

object SecurityManager {
    /**
     * Hashes the given PIN using SHA-256 for secure storage.
     * In a production app with a backend, a salt should be used, 
     * but for a local-only PIN lock, a raw hash is sufficient against casual attacks.
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
