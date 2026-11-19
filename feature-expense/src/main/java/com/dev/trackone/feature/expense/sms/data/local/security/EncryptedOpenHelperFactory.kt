package com.dev.trackone.feature.expense.sms.data.local.security

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Opens the database with SQLCipher.
 *
 * The key is fetched when Room first opens the file — on its background query
 * executor — rather than when the database object is injected, so Keystore
 * work never lands on the main thread.
 *
 * If no usable key exists, whatever file is on disk cannot be decrypted (key
 * lost after a restore, or a file from before encryption): it is deleted and
 * recreated. It only ever holds pending suggestions, so that is safe to lose.
 */
class EncryptedOpenHelperFactory(
    private val context: Context,
    private val keyStore: DatabaseKeyStore,
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper =
        LazyOpenHelper(configuration.name) {
            System.loadLibrary("sqlcipher")
            val key = keyStore.getOrCreateKey()
            if (key.isNew) configuration.name?.let(context::deleteDatabase)
            SupportOpenHelperFactory(key.passphrase).create(configuration)
        }
}

/** Defers building the real helper until the database is first opened. */
private class LazyOpenHelper(
    override val databaseName: String?,
    private val open: () -> SupportSQLiteOpenHelper,
) : SupportSQLiteOpenHelper {

    private val lock = Any()
    private var delegate: SupportSQLiteOpenHelper? = null
    private var writeAheadLogging: Boolean? = null

    private fun delegate(): SupportSQLiteOpenHelper = synchronized(lock) {
        delegate ?: open().also { helper ->
            writeAheadLogging?.let(helper::setWriteAheadLoggingEnabled)
            delegate = helper
        }
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) = synchronized(lock) {
        writeAheadLogging = enabled
        delegate?.setWriteAheadLoggingEnabled(enabled)
        Unit
    }

    override val writableDatabase: SupportSQLiteDatabase
        get() = delegate().writableDatabase

    override val readableDatabase: SupportSQLiteDatabase
        get() = delegate().readableDatabase

    override fun close() = synchronized(lock) {
        delegate?.close()
        Unit
    }
}
