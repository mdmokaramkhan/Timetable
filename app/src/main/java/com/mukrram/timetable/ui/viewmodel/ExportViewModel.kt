package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.repository.TimetableRepository
import com.mukrram.timetable.ui.analytics.formatTimetableAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ExportUiState(
    val batches: List<BatchDto> = emptyList(),
    val selectedBatchName: String? = null,
    val exportText: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class ExportViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState(loading = true))
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadBatches()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onBatchSelected(name: String) {
        _uiState.update { it.copy(selectedBatchName = name) }
        loadExport(name)
    }

    fun refresh() {
        val batch = _uiState.value.selectedBatchName ?: return
        loadExport(batch)
    }

    fun retryAfterError() {
        loadBatches()
    }

    private fun loadBatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val batches = repository.getBatches()
                val selected = batches.firstOrNull()?.name
                _uiState.update {
                    it.copy(
                        batches = batches,
                        selectedBatchName = selected,
                    )
                }
                if (selected != null) {
                    loadExportForBatch(selected)
                } else {
                    _uiState.update { it.copy(loading = false, exportText = null) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = humanMessage(e))
                }
            }
        }
    }

    private fun loadExport(batchName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            loadExportForBatch(batchName)
        }
    }

    private suspend fun loadExportForBatch(batchName: String) {
        try {
            val tt = repository.getTimetable(batchName)
            val text = formatTimetableAsText(tt.batch, tt.schedule, tt.updatedAt)
            _uiState.update {
                it.copy(loading = false, exportText = text, error = null)
            }
        } catch (e: Exception) {
            val notFound = e is HttpException && e.code() == 404
            _uiState.update {
                it.copy(
                    loading = false,
                    exportText = null,
                    error = if (notFound) {
                        "No saved timetable for this batch yet."
                    } else {
                        humanMessage(e)
                    },
                )
            }
        }
    }

    private fun humanMessage(e: Exception): String {
        if (e is HttpException) {
            return e.message ?: "Request failed (${e.code()})"
        }
        return e.message ?: "Something went wrong"
    }
}
