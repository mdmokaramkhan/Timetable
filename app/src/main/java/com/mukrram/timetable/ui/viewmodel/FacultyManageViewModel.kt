package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.FacultyCreateRequest
import com.mukrram.timetable.data.remote.dto.FacultyDto
import com.mukrram.timetable.data.remote.dto.FacultyUpdateRequest
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class FacultyManageUiState(
    val items: List<FacultyDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pendingMessage: String? = null,
)

class FacultyManageViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacultyManageUiState())
    val uiState: StateFlow<FacultyManageUiState> = _uiState.asStateFlow()

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
                val list = repository.getFaculty()
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

    fun create(name: String, subjectsCsv: String, maxLoad: Int) {
        viewModelScope.launch {
            try {
                val subjects = subjectsCsv.split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                repository.createFaculty(
                    FacultyCreateRequest(name = name, subjects = subjects, maxLoad = maxLoad),
                )
                _uiState.update { it.copy(pendingMessage = "Faculty added") }
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

    fun update(id: String, name: String, subjectsCsv: String, maxLoad: Int) {
        viewModelScope.launch {
            try {
                val subjects = subjectsCsv.split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                repository.updateFaculty(
                    id,
                    FacultyUpdateRequest(name = name, subjects = subjects, maxLoad = maxLoad),
                )
                _uiState.update { it.copy(pendingMessage = "Faculty updated") }
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
                repository.deleteFaculty(id)
                _uiState.update { it.copy(pendingMessage = "Faculty deleted") }
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
