package com.dev.trackone.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
    val hasSeenTour: Boolean
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val HAS_SEEN_TOUR = booleanPreferencesKey("has_seen_tour")
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
            UserPreferences(hasSeenOnboarding, hasSeenTour)
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
}
