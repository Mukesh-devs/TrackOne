package com.dev.trackone.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

data class UserPreferences(
    val hasSeenOnboarding: Boolean,
    val hasSeenTour: Boolean,
    val isAppLockEnabled: Boolean = false,
    val hashedPin: String? = null,
    /** Home screen's Apps section: grid of tiles when true, rows when false. */
    val isAppsGridView: Boolean = false,
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val HAS_SEEN_TOUR = booleanPreferencesKey("has_seen_tour")
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val HASHED_PIN = stringPreferencesKey("hashed_pin")
        val IS_APPS_GRID_VIEW = booleanPreferencesKey("is_apps_grid_view")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val hasSeenOnboarding = preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
            val hasSeenTour = preferences[PreferencesKeys.HAS_SEEN_TOUR] ?: false
            val isAppLockEnabled = preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] ?: false
            val hashedPin = preferences[PreferencesKeys.HASHED_PIN]
            val isAppsGridView = preferences[PreferencesKeys.IS_APPS_GRID_VIEW] ?: false
            UserPreferences(
                hasSeenOnboarding,
                hasSeenTour,
                isAppLockEnabled,
                hashedPin,
                isAppsGridView,
            )
        }

    suspend fun completeOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_ONBOARDING] = true
        }
    }

    suspend fun completeTour() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_TOUR] = true
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] = enabled
        }
    }

    /**
     * Persisted rather than kept in composition: the Dashboard leaves the
     * composition entirely whenever a module is opened, so remembered state
     * would reset the layout on every trip home.
     */
    suspend fun setAppsGridView(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_APPS_GRID_VIEW] = enabled
        }
    }

    suspend fun setHashedPin(hashedPin: String?) {
        context.dataStore.edit { preferences ->
            if (hashedPin == null) {
                preferences.remove(PreferencesKeys.HASHED_PIN)
            } else {
                preferences[PreferencesKeys.HASHED_PIN] = hashedPin
            }
        }
    }
}
