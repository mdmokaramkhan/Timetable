package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val username: String = "",
    val signedIn: Boolean = false,
    val roleLabel: String = "—",
    val isAdmin: Boolean = false,
)

class ProfileViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.sessionState,
                repository.displayName,
            ) { session, name -> session to name }
                .collect { (session, name) ->
                    val signedIn = session is SessionState.LoggedIn
                    val roleLabel = when (session) {
                        is SessionState.LoggedIn -> when (session.role) {
                            UserRole.Admin -> "Administrator"
                            UserRole.Faculty -> "Faculty"
                        }
                        SessionState.LoggedOut -> "—"
                    }
                    val isAdmin = session is SessionState.LoggedIn && session.role == UserRole.Admin
                    _uiState.update {
                        it.copy(
                            signedIn = signedIn,
                            roleLabel = roleLabel,
                            displayName = name,
                            username = if (session is SessionState.LoggedIn) session.username else "",
                            isAdmin = isAdmin,
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
