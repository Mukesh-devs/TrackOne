package com.dev.trackone.core.common.model

/**
 * A generic sealed interface for representing the result of an operation.
 * Used across the app to handle success, error, and loading states.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}

/**
 * Extension to map the success data to a different type.
 */
inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
    is AppResult.Loading -> this
}

/**
 * Extension to perform an action on success data.
 */
inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

/**
 * Extension to perform an action on error.
 */
inline fun <T> AppResult<T>.onError(action: (String, Throwable?) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(message, throwable)
    return this
}

/**
 * Returns the data on success, or null otherwise.
 */
fun <T> AppResult<T>.getOrNull(): T? = when (this) {
    is AppResult.Success -> data
    else -> null
}

/**
 * Returns the data on success, or the [defaultValue] otherwise.
 */
fun <T> AppResult<T>.getOrDefault(defaultValue: T): T = when (this) {
    is AppResult.Success -> data
    else -> defaultValue
}
