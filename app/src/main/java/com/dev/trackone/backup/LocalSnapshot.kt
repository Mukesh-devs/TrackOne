package com.dev.trackone.backup

import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expiry.data.local.ExpiryEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialises the whole local database to JSON.
 *
 * Used once, by [com.dev.trackone.cloud.CloudAccountManager], to dump a
 * pre-cloud database to app storage before uploading it to a fresh account —
 * a last-resort copy if that upload goes wrong. There is no import counterpart:
 * the API is the source of truth, so data comes back from the account, not from
 * a file.
 *
 * org.json rather than Moshi/Gson: the shape is written in exactly one place and
 * never parsed back, so a code-generated adapter would buy nothing.
 */
object LocalSnapshotSerializer {

    private const val KEY_VERSION = "version"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val KEY_EXPIRY_ITEMS = "expiry_items"
    private const val KEY_EXPENSE_ITEMS = "expense_items"
    private const val KEY_CREDENTIAL_ITEMS = "credential_items"

    fun toJson(
        expiryItems: List<ExpiryEntity>,
        expenseItems: List<ExpenseEntity>,
        credentialItems: List<CredentialEntity>
    ): String {
        val root = JSONObject()
        root.put(KEY_VERSION, 1)
        root.put(KEY_TIMESTAMP, System.currentTimeMillis())

        // Expiry
        val expiryArray = JSONArray()
        for (item in expiryItems) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("provider", item.provider)
            obj.put("label", item.label)
            obj.put("purchasedDate", item.purchasedDate)
            obj.put("expiryDate", item.expiryDate)
            obj.put("notes", item.notes)
            obj.put("amount", item.amount)
            obj.put("username", item.username)
            obj.put("email", item.email)
            obj.put("password", item.password)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)
            expiryArray.put(obj)
        }
        root.put(KEY_EXPIRY_ITEMS, expiryArray)

        // Expense
        val expenseArray = JSONArray()
        for (item in expenseItems) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("amount", item.amount)
            obj.put("isIncome", item.isIncome)
            obj.put("category", item.category)
            obj.put("paymentMode", item.paymentMode)
            obj.put("date", item.date)
            obj.put("notes", item.notes)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)
            expenseArray.put(obj)
        }
        root.put(KEY_EXPENSE_ITEMS, expenseArray)

        // Credential
        val credentialArray = JSONArray()
        for (item in credentialItems) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("username", item.username)
            obj.put("email", item.email)
            obj.put("password", item.password)
            obj.put("url", item.url)
            obj.put("category", item.category)
            obj.put("notes", item.notes)
            obj.put("isFavorite", item.isFavorite)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)
            credentialArray.put(obj)
        }
        root.put(KEY_CREDENTIAL_ITEMS, credentialArray)

        return root.toString()
    }
}
