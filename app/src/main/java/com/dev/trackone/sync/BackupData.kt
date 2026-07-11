package com.dev.trackone.sync

import com.dev.trackone.feature.credential.data.local.CredentialEntity
import com.dev.trackone.feature.expense.data.local.ExpenseEntity
import com.dev.trackone.feature.expiry.data.local.ExpiryEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Parses and serializes BackupData to and from JSON.
 * We use org.json to avoid pulling in Gson/Moshi just for this simple feature.
 */
object BackupDataSerializer {

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
    
    fun fromJson(jsonString: String): ParsedBackup {
        val root = JSONObject(jsonString)
        
        val expiryList = mutableListOf<ExpiryEntity>()
        val expiryArray = root.optJSONArray(KEY_EXPIRY_ITEMS)
        if (expiryArray != null) {
            for (i in 0 until expiryArray.length()) {
                val obj = expiryArray.getJSONObject(i)
                expiryList.add(
                    ExpiryEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        provider = obj.getString("provider"),
                        label = obj.optString("label", ""),
                        purchasedDate = obj.getLong("purchasedDate"),
                        expiryDate = obj.getLong("expiryDate"),
                        notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                        amount = if (obj.has("amount") && !obj.isNull("amount")) obj.getDouble("amount") else null,
                        username = if (obj.has("username") && !obj.isNull("username")) obj.getString("username") else null,
                        email = if (obj.has("email") && !obj.isNull("email")) obj.getString("email") else null,
                        password = if (obj.has("password") && !obj.isNull("password")) obj.getString("password") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        
        val expenseList = mutableListOf<ExpenseEntity>()
        val expenseArray = root.optJSONArray(KEY_EXPENSE_ITEMS)
        if (expenseArray != null) {
            for (i in 0 until expenseArray.length()) {
                val obj = expenseArray.getJSONObject(i)
                expenseList.add(
                    ExpenseEntity(
                        id = obj.optLong("id", 0L),
                        title = obj.getString("title"),
                        amount = obj.getDouble("amount"),
                        isIncome = obj.getBoolean("isIncome"),
                        category = obj.getString("category"),
                        paymentMode = obj.optString("paymentMode", "CASH"),
                        date = obj.getLong("date"),
                        notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        
        val credentialList = mutableListOf<CredentialEntity>()
        val credentialArray = root.optJSONArray(KEY_CREDENTIAL_ITEMS)
        if (credentialArray != null) {
            for (i in 0 until credentialArray.length()) {
                val obj = credentialArray.getJSONObject(i)
                credentialList.add(
                    CredentialEntity(
                        id = obj.optLong("id", 0L),
                        title = obj.getString("title"),
                        username = obj.getString("username"),
                        email = obj.getString("email"),
                        password = obj.getString("password"),
                        url = obj.optString("url", ""),
                        category = obj.optString("category", ""),
                        notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null,
                        isFavorite = obj.optBoolean("isFavorite", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
        
        return ParsedBackup(expiryList, expenseList, credentialList)
    }
}

data class ParsedBackup(
    val expiryItems: List<ExpiryEntity>,
    val expenseItems: List<ExpenseEntity>,
    val credentialItems: List<CredentialEntity>
)
