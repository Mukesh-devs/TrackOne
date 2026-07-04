package com.dev.trackone.core.common.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility functions for date formatting, parsing, and calculations.
 */
object DateUtils {

    private const val DISPLAY_FORMAT = "dd MMM yyyy"
    private const val FULL_FORMAT = "dd MMMM yyyy"
    private const val SHORT_FORMAT = "dd/MM/yy"
    private const val ISO_FORMAT = "yyyy-MM-dd"
    private const val MONTH_YEAR_FORMAT = "MMMM yyyy"
    private const val DAY_MONTH_FORMAT = "dd MMM"

    /**
     * Formats epoch millis to display format (e.g., "03 Jul 2026").
     */
    fun formatDate(epochMillis: Long, pattern: String = DISPLAY_FORMAT): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(epochMillis))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Formats epoch millis to full format (e.g., "03 July 2026").
     */
    fun formatDateFull(epochMillis: Long): String = formatDate(epochMillis, FULL_FORMAT)

    /**
     * Formats epoch millis to short format (e.g., "03/07/26").
     */
    fun formatDateShort(epochMillis: Long): String = formatDate(epochMillis, SHORT_FORMAT)

    /**
     * Formats epoch millis to month-year (e.g., "July 2026").
     */
    fun formatMonthYear(epochMillis: Long): String = formatDate(epochMillis, MONTH_YEAR_FORMAT)

    /**
     * Formats epoch millis to day-month (e.g., "03 Jul").
     */
    fun formatDayMonth(epochMillis: Long): String = formatDate(epochMillis, DAY_MONTH_FORMAT)

    /**
     * Parses a date string to epoch millis.
     */
    fun parseDate(dateStr: String, pattern: String = DISPLAY_FORMAT): Long? {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates days remaining until the given date.
     * Returns negative if the date has passed.
     */
    fun daysUntil(epochMillis: Long): Long {
        val now = System.currentTimeMillis()
        val diff = epochMillis - now
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    /**
     * Checks if a date has expired (is in the past).
     */
    fun isExpired(epochMillis: Long): Boolean {
        return epochMillis < System.currentTimeMillis()
    }

    /**
     * Checks if the date is within the next [days] days.
     */
    fun isWithinDays(epochMillis: Long, days: Int): Boolean {
        val now = System.currentTimeMillis()
        val futureLimit = now + TimeUnit.DAYS.toMillis(days.toLong())
        return epochMillis in now..futureLimit
    }

    /**
     * Returns a human-readable expiry label.
     */
    fun getExpiryLabel(epochMillis: Long): String {
        val days = daysUntil(epochMillis)
        return when {
            days < 0 -> "Expired ${-days} day${if (-days != 1L) "s" else ""} ago"
            days == 0L -> "Expires today"
            days == 1L -> "Expires tomorrow"
            days <= 7 -> "Expires in $days days"
            days <= 30 -> "Expires in ${days / 7} week${if (days / 7 != 1L) "s" else ""}"
            days <= 365 -> "Expires in ${days / 30} month${if (days / 30 != 1L) "s" else ""}"
            else -> "Expires in ${days / 365} year${if (days / 365 != 1L) "s" else ""}"
        }
    }

    /**
     * Returns the start of today (midnight) in epoch millis.
     */
    fun getStartOfDay(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Returns the end of today (23:59:59.999) in epoch millis.
     */
    fun getEndOfDay(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /**
     * Returns the start of the current month in epoch millis.
     */
    fun getStartOfMonth(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Returns the end of the current month in epoch millis.
     */
    fun getEndOfMonth(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /**
     * Returns current epoch millis.
     */
    fun now(): Long = System.currentTimeMillis()
}
