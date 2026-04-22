package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

private val objectIdRegex = Regex("^[a-fA-F0-9]{24}$")

data class AuthUiState(
    val loginUsername: String = "",
    val loginPassword: String = "",
    val loginPasswordVisible: Boolean = false,
    val loginError: String? = null,
    val loginLoading: Boolean = false,
    val registerUsername: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val registerFacultyId: String = "",
    val registerPasswordVisible: Boolean = false,
    val registerError: String? = null,
    val registerLoading: Boolean = false,
)

class AuthViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setLoginUsername(value: String) {
        _uiState.update { it.copy(loginUsername = value, loginError = null) }
    }

    fun setLoginPassword(value: String) {
        _uiState.update { it.copy(loginPassword = value, loginError = null) }
    }

    fun toggleLoginPasswordVisible() {
        _uiState.update { it.copy(loginPasswordVisible = !it.loginPasswordVisible) }
    }

    fun setRegisterUsername(value: String) {
        _uiState.update { it.copy(registerUsername = value, registerError = null) }
    }

    fun setRegisterPassword(value: String) {
        _uiState.update { it.copy(registerPassword = value, registerError = null) }
    }

    fun setRegisterConfirmPassword(value: String) {
        _uiState.update { it.copy(registerConfirmPassword = value, registerError = null) }
    }

    fun setRegisterFacultyId(value: String) {
        _uiState.update { it.copy(registerFacultyId = value, registerError = null) }
    }

    fun toggleRegisterPasswordVisible() {
        _uiState.update { it.copy(registerPasswordVisible = !it.registerPasswordVisible) }
    }

    fun login() {
        val username = _uiState.value.loginUsername.trim().lowercase()
        val password = _uiState.value.loginPassword
        when {
            username.isEmpty() -> {
                _uiState.update { it.copy(loginError = "Enter your username") }
                return
            }
            password.isEmpty() -> {
                _uiState.update { it.copy(loginError = "Enter your password") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            val result = repository.login(username, password)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            loginLoading = false,
                            loginError = null,
                            loginPassword = "",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(loginLoading = false, loginError = parseApiError(e))
                    }
                },
            )
        }
    }

    fun register() {
        val username = _uiState.value.registerUsername.trim().lowercase()
        val password = _uiState.value.registerPassword
        val confirm = _uiState.value.registerConfirmPassword
        val facultyIdRaw = _uiState.value.registerFacultyId.trim()

        val usernameError = when {
            username.isEmpty() -> "Choose a username"
            username.length < 3 -> "Username must be at least 3 characters"
            else -> null
        }
        if (usernameError != null) {
            _uiState.update { it.copy(registerError = usernameError) }
            return
        }
        when {
            password.isEmpty() -> {
                _uiState.update { it.copy(registerError = "Enter a password") }
                return
            }
            password.length < 6 -> {
                _uiState.update { it.copy(registerError = "Password must be at least 6 characters") }
                return
            }
            password != confirm -> {
                _uiState.update { it.copy(registerError = "Passwords do not match") }
                return
            }
        }
        if (facultyIdRaw.isNotEmpty() && !objectIdRegex.matches(facultyIdRaw)) {
            _uiState.update { it.copy(registerError = "Faculty ID must be a 24-character hex ID from your admin") }
            return
        }

        val facultyId = facultyIdRaw.takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(registerLoading = true, registerError = null) }
            val result = repository.register(username, password, facultyId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            registerLoading = false,
                            registerError = null,
                            registerPassword = "",
                            registerConfirmPassword = "",
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(registerLoading = false, registerError = parseApiError(e))
                    }
                },
            )
        }
    }
}

private fun parseApiError(e: Throwable): String {
    if (e is HttpException) {
        val raw = e.response()?.errorBody()?.string()?.trim()
        if (!raw.isNullOrEmpty()) {
            try {
                val jo = JSONObject(raw)
                if (jo.has("error")) return jo.getString("error")
            } catch (_: Exception) {
                // fall through
            }
            return raw
        }
        return "HTTP ${e.code()}"
    }
    return e.message?.takeIf { it.isNotEmpty() } ?: "Something went wrong"
}
