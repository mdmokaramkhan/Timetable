package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.RoomCreateRequest
import com.mukrram.timetable.data.remote.dto.RoomDto
import com.mukrram.timetable.data.remote.dto.RoomUpdateRequest
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class RoomManageUiState(
    val items: List<RoomDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pendingMessage: String? = null,
)

class RoomManageViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomManageUiState())
    val uiState: StateFlow<RoomManageUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun consumeMessage() {
        _uiState.update { it.copy(pendingMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val list = repository.getRooms()
                _uiState.update { it.copy(items = list, loading = false, error = null) }
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string()?.ifBlank { null }
                    ?: "HTTP ${e.code()}"
                _uiState.update { it.copy(loading = false, error = msg) }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "Network error")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    fun create(name: String, type: String) {
        viewModelScope.launch {
            try {
                repository.createRoom(RoomCreateRequest(name = name, type = type))
                _uiState.update { it.copy(pendingMessage = "Room added") }
                load()
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string()?.ifBlank { null }
                    ?: "HTTP ${e.code()}"
                _uiState.update { it.copy(error = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Save failed") }
            }
        }
    }

    fun update(id: String, name: String, type: String) {
        viewModelScope.launch {
            try {
                repository.updateRoom(
                    id,
                    RoomUpdateRequest(name = name, type = type),
                )
                _uiState.update { it.copy(pendingMessage = "Room updated") }
                load()
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string()?.ifBlank { null }
                    ?: "HTTP ${e.code()}"
                _uiState.update { it.copy(error = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Update failed") }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteRoom(id)
                _uiState.update { it.copy(pendingMessage = "Room deleted") }
                load()
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string()?.ifBlank { null }
                    ?: "HTTP ${e.code()}"
                _uiState.update { it.copy(error = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Delete failed") }
            }
        }
    }
}
