package com.mukrram.timetable.data.remote

import com.mukrram.timetable.data.model.UserRole

sealed interface SessionState {
    data object LoggedOut : SessionState

    data class LoggedIn(
        val role: UserRole,
        val username: String,
    ) : SessionState
}
