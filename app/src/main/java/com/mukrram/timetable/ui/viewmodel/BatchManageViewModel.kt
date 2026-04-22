package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.BatchCreateRequest
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.BatchUpdateRequest
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class BatchManageUiState(
    val items: List<BatchDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val pendingMessage: String? = null,
)

class BatchManageViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchManageUiState())
    val uiState: StateFlow<BatchManageUiState> = _uiState.asStateFlow()

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
                val list = repository.getBatches()
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

    fun create(name: String, department: String) {
        viewModelScope.launch {
            try {
                repository.createBatch(BatchCreateRequest(name = name, department = department))
                _uiState.update { it.copy(pendingMessage = "Batch added") }
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

    fun update(id: String, name: String, department: String) {
        viewModelScope.launch {
            try {
                repository.updateBatch(
                    id,
                    BatchUpdateRequest(name = name, department = department),
                )
                _uiState.update { it.copy(pendingMessage = "Batch updated") }
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
                repository.deleteBatch(id)
                _uiState.update { it.copy(pendingMessage = "Batch deleted") }
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
