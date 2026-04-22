package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.SubjectCreateRequest
import com.mukrram.timetable.data.remote.dto.SubjectDto
import com.mukrram.timetable.data.remote.dto.SubjectUpdateRequest
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class SubjectManageUiState(
    val items: List<SubjectDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pendingMessage: String? = null,
)

class SubjectManageViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectManageUiState())
    val uiState: StateFlow<SubjectManageUiState> = _uiState.asStateFlow()

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
                val list = repository.getSubjects()
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

    fun create(name: String, lecturesPerWeek: Int) {
        viewModelScope.launch {
            try {
                repository.createSubject(SubjectCreateRequest(name, lecturesPerWeek))
                _uiState.update { it.copy(pendingMessage = "Subject added") }
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

    fun update(id: String, name: String, lecturesPerWeek: Int) {
        viewModelScope.launch {
            try {
                repository.updateSubject(
                    id,
                    SubjectUpdateRequest(name = name, lecturesPerWeek = lecturesPerWeek),
                )
                _uiState.update { it.copy(pendingMessage = "Subject updated") }
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
                repository.deleteSubject(id)
                _uiState.update { it.copy(pendingMessage = "Subject deleted") }
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
