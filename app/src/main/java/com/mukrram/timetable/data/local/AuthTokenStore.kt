package com.mukrram.timetable.data.local

/**
 * In-memory token for OkHttp; kept in sync with [TimetablePreferences] when auth changes.
 */
class AuthTokenStore {
    @Volatile
    var token: String? = null
        private set

    fun setToken(value: String?) {
        token = value?.takeIf { it.isNotBlank() }
    }
}
