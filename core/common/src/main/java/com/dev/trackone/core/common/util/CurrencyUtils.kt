package com.dev.trackone.core.common.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility functions for currency formatting throughout the app.
 */
object CurrencyUtils {

    private val defaultLocale: Locale = Locale("en", "IN")

    /**
     * Formats a [Double] amount into a localized currency string.
     * Default locale is India (₹).
     */
    fun formatAmount(amount: Double, locale: Locale = defaultLocale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return formatter.format(amount)
    }

    /**
     * Formats amount without currency symbol — just the number with grouping.
     */
    fun formatNumber(amount: Double, locale: Locale = defaultLocale): String {
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return formatter.format(amount)
    }

    /**
     * Returns the currency symbol for the given locale.
     */
    fun getCurrencySymbol(locale: Locale = defaultLocale): String {
        return try {
            Currency.getInstance(locale).symbol
        } catch (e: Exception) {
            "₹"
        }
    }

    /**
     * Parses a currency string back to a Double.
     * Strips currency symbols and grouping separators.
     */
    fun parseAmount(amountStr: String): Double? {
        return try {
            amountStr
                .replace(Regex("[^\\d.,\\-]"), "")
                .replace(",", "")
                .toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
