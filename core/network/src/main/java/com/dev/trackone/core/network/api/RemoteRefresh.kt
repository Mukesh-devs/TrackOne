package com.dev.trackone.core.network.api

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coalesces the background cache refreshes the repositories fire on every read.
 *
 * Without this, one screen can kick off half a dozen identical `GET`s: each
 * repository read starts a refresh, and screens legitimately re-subscribe
 * (period changes, tab switches, config changes). Requests for the same key are
 * serialised, and a key that succeeded within [DEFAULT_MIN_INTERVAL_MS] is
 * skipped — a write path calls [invalidate] so the very next read still goes to
 * the network.
 */
@Singleton
class RemoteRefreshCoordinator @Inject constructor() {

    private val registryGuard = Mutex()
    private val locks = mutableMapOf<String, Mutex>()
    private val lastSuccessAt = mutableMapOf<String, Long>()

    /**
     * Runs [block] unless an equivalent refresh just succeeded.
     *
     * Failures are swallowed by design: a read must keep showing cached data
     * when the network is unavailable. Cancellation still propagates.
     */
    suspend fun refresh(
        key: String,
        minIntervalMs: Long = DEFAULT_MIN_INTERVAL_MS,
        block: suspend () -> Unit,
    ) {
        val lock = registryGuard.withLock { locks.getOrPut(key) { Mutex() } }
        lock.withLock {
            val now = SystemClock.elapsedRealtime()
            val last = lastSuccessAt[key]
            if (last != null && now - last < minIntervalMs) return
            try {
                block()
                lastSuccessAt[key] = SystemClock.elapsedRealtime()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.d(TAG, "Background refresh of '$key' failed; serving cache", e)
            }
        }
    }

    /** Forces the next read of [key] to hit the network. */
    suspend fun invalidate(key: String) {
        registryGuard.withLock { lastSuccessAt.remove(key) }
    }

    /** Forces every key to refetch — used when the signed-in account changes. */
    suspend fun invalidateAll() {
        registryGuard.withLock { lastSuccessAt.clear() }
    }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MS = 30_000L
        private const val TAG = "RemoteRefresh"
    }
}

/**
 * The read half of the cloud-first contract: emit whatever Room already has
 * *immediately*, and pull fresh data from the API alongside it.
 *
 * The refresh runs in its own coroutine rather than in `onStart`, so a slow or
 * dead network never delays the first cached emission. When it succeeds it
 * writes to Room, and Room's own flow pushes the new rows out of this same
 * collector — the caller just sees a second emission.
 */
fun <T> Flow<T>.cachedWithRemoteRefresh(
    coordinator: RemoteRefreshCoordinator,
    key: String,
    refresh: suspend () -> Unit,
): Flow<T> = channelFlow {
    launch { coordinator.refresh(key, block = refresh) }
    collect { send(it) }
}
