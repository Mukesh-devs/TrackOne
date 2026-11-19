package com.dev.trackone.feature.expense.sms.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dev.trackone.feature.expense.sms.data.local.security.DatabaseKeyStore
import com.dev.trackone.feature.expense.sms.data.local.security.EncryptedOpenHelperFactory
import com.dev.trackone.feature.expense.sms.domain.model.DetectionStatus
import com.dev.trackone.feature.expense.sms.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val keyStore = DatabaseKeyStore(context)
    private val name = DetectedTransactionsDatabase.NAME

    @Before
    @After
    fun reset() {
        context.deleteDatabase(name)
        // clear() rather than deleting the file: the process caches prefs in memory.
        prefs().edit().clear().commit()
        deleteKeystoreKey()
    }

    private fun prefs() = context.getSharedPreferences(DatabaseKeyStore.PREFS_NAME, Context.MODE_PRIVATE)

    private fun deleteKeystoreKey() {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(DatabaseKeyStore.KEYSTORE_ALIAS)
    }

    private fun open(): DetectedTransactionsDatabase =
        Room.databaseBuilder(context, DetectedTransactionsDatabase::class.java, name)
            .openHelperFactory(EncryptedOpenHelperFactory(context, keyStore))
            .build()

    private fun row(dedupKey: String) = DetectedTransactionEntity(
        name = "Test Shop",
        amountPaise = 25_000,
        type = TransactionType.DEBIT,
        occurredAt = 1_000L,
        status = DetectionStatus.PENDING_REVIEW,
        dedupKey = dedupKey,
        createdAt = 1_000L,
    )

    @Test
    fun keyIsGeneratedOnceThenReused() {
        val first = keyStore.getOrCreateKey()
        val second = keyStore.getOrCreateKey()

        assertTrue(first.isNew)
        assertFalse(second.isNew)
        assertArrayEquals(first.passphrase, second.passphrase)
        // Raw-key form: x'<64 hex>'
        assertEquals(67, first.passphrase.size)
    }

    @Test
    fun wrappedKeyIsNotStoredInPlainText() {
        val key = keyStore.getOrCreateKey()
        val stored = prefs().all.values.joinToString()
        val hex = String(key.passphrase, Charsets.US_ASCII).removePrefix("x'").removeSuffix("'")

        assertFalse(stored.contains(hex))
    }

    @Test
    fun fileIsEncryptedAndReadableWithStoredKey() = runBlocking {
        open().apply {
            detectedTransactionDao().insert(row("a"))
            close()
        }

        val header = context.getDatabasePath(name).inputStream().use { input ->
            ByteArray(16).also { input.read(it) }
        }
        assertNotEquals("SQLite format 3\u0000", String(header, Charsets.US_ASCII))

        val reopened = open()
        assertEquals("Test Shop", reopened.detectedTransactionDao().getById(1L)?.name)
        reopened.close()
    }

    @Test
    fun lostKeyRecreatesEmptyDatabase() = runBlocking {
        open().apply {
            detectedTransactionDao().insert(row("a"))
            close()
        }
        // The wrapped key is still stored, but the Keystore key that unwraps it is gone.
        deleteKeystoreKey()

        val recreated = open()
        assertEquals(null, recreated.detectedTransactionDao().getById(1L))
        assertEquals(1L, recreated.detectedTransactionDao().insert(row("b")))
        recreated.close()
    }

    @Test
    fun plainDatabaseFromEarlierBuildIsReplaced() = runBlocking {
        Room.databaseBuilder(context, DetectedTransactionsDatabase::class.java, name).build().apply {
            detectedTransactionDao().insert(row("a"))
            close()
        }

        val encrypted = open()
        assertEquals(null, encrypted.detectedTransactionDao().getById(1L))
        encrypted.detectedTransactionDao().insert(row("b"))
        encrypted.close()

        assertEquals("Test Shop", open().detectedTransactionDao().getById(1L)?.name)
    }
}
