package com.dev.trackone.core.common.util

/**
 * App-wide constants.
 */
object Constants {

    // Database
    const val DATABASE_NAME = "trackone_database"
    const val DATABASE_VERSION = 1

    // Date Formats
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val DATE_FORMAT_FULL = "dd MMMM yyyy"
    const val DATE_FORMAT_SHORT = "dd/MM/yy"
    const val DATE_FORMAT_ISO = "yyyy-MM-dd"
    const val DATE_FORMAT_MONTH_YEAR = "MMMM yyyy"
    const val DATE_FORMAT_DAY_MONTH = "dd MMM"
    const val DATE_FORMAT_TIME = "hh:mm a"
    const val DATE_FORMAT_DATE_TIME = "dd MMM yyyy, hh:mm a"

    // Upcoming expiry thresholds (in days)
    const val UPCOMING_7_DAYS = 7
    const val UPCOMING_15_DAYS = 15
    const val UPCOMING_30_DAYS = 30

    // Search debounce (ms)
    const val SEARCH_DEBOUNCE_MS = 300L

    // Animation durations (ms)
    const val ANIMATION_DURATION_SHORT = 200
    const val ANIMATION_DURATION_MEDIUM = 300
    const val ANIMATION_DURATION_LONG = 500

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
}
