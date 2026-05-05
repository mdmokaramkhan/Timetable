package com.mukrram.timetable.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mukrram.timetable.data.model.AppThemeMode
import com.mukrram.timetable.data.remote.JwtPayloadParser
import com.mukrram.timetable.data.remote.dto.SavedTimetableResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(
    name = "timetable_prefs",
)

class TimetablePreferences(
    private val context: Context,
    private val gson: Gson = Gson(),
) {
    private val dataStore = context.dataStore

    val authToken: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_AUTH_TOKEN]
    }

    val displayName: Flow<String> = authToken.map { token ->
        when {
            token.isNullOrBlank() -> "Guest"
            else -> JwtPayloadParser.parseSub(token) ?: "User"
        }
    }

    val lastTimetableJson: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_TIMETABLE_JSON]
    }

    /** When false or unset, the app shows the first-run onboarding flow. */
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HAS_COMPLETED_ONBOARDING] == true
    }

    val themePreference: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        AppThemeMode.fromStorage(prefs[KEY_THEME_MODE])
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.storageValue }
    }

    suspend fun setAuthToken(token: String?) {
        dataStore.edit {
            if (token.isNullOrBlank()) {
                it.remove(KEY_AUTH_TOKEN)
            } else {
                it[KEY_AUTH_TOKEN] = token
            }
        }
    }

    suspend fun cacheLastTimetable(response: SavedTimetableResponse) {
        val json = gson.toJson(response)
        dataStore.edit { it[KEY_LAST_TIMETABLE_JSON] = json }
    }

    suspend fun clearCachedTimetable() {
        dataStore.edit { it.remove(KEY_LAST_TIMETABLE_JSON) }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        dataStore.edit {
            if (completed) {
                it[KEY_HAS_COMPLETED_ONBOARDING] = true
            } else {
                it.remove(KEY_HAS_COMPLETED_ONBOARDING)
            }
        }
    }

    fun parseCachedTimetable(json: String?): SavedTimetableResponse? {
        if (json.isNullOrBlank()) return null
        return runCatching { gson.fromJson(json, SavedTimetableResponse::class.java) }.getOrNull()
    }

    companion object {
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_LAST_TIMETABLE_JSON = stringPreferencesKey("last_timetable_json")
        private val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
