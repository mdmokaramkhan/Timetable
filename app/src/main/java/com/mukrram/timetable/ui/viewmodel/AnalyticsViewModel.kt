package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.AnalyticsResponseDto
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.repository.TimetableRepository
import com.mukrram.timetable.ui.analytics.ScheduleAnalytics
import com.mukrram.timetable.ui.analytics.computeScheduleAnalytics
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AnalyticsUiState(
    val batches: List<BatchDto> = emptyList(),
    val selectedBatchName: String? = null,
    /** GET /analytics — campus-wide aggregates */
    val serverSnapshot: AnalyticsResponseDto? = null,
    /** Computed from GET timetable/{batch} for the selected batch */
    val batchScheduleAnalytics: ScheduleAnalytics? = null,
    /** True when the selected batch has no saved timetable (404); not a fatal error. */
    val batchTimetableMissing: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

class AnalyticsViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState(loading = true))
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onBatchSelected(name: String) {
        _uiState.update { it.copy(selectedBatchName = name) }
        loadBatchTimetableOnly(name)
    }

    fun refresh() {
        loadAll()
    }

    fun retryAfterError() {
        loadAll()
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val batchesDeferred = async { repository.getBatches() }
                val analyticsDeferred = async { repository.getAnalytics() }
                val batches = batchesDeferred.await()
                val server = analyticsDeferred.await()
                val selected = _uiState.value.selectedBatchName?.takeIf { want ->
                    batches.any { it.name == want }
                } ?: batches.firstOrNull()?.name
                _uiState.update {
                    it.copy(
                        batches = batches,
                        selectedBatchName = selected,
                        serverSnapshot = server,
                    )
                }
                if (selected != null) {
                    loadBatchTimetableForBatch(selected)
                } else {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            batchScheduleAnalytics = null,
                            batchTimetableMissing = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        serverSnapshot = null,
                        batchScheduleAnalytics = null,
                        error = humanMessage(e),
                    )
                }
            }
        }
    }

    private fun loadBatchTimetableOnly(batchName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            loadBatchTimetableForBatch(batchName)
        }
    }

    private suspend fun loadBatchTimetableForBatch(batchName: String) {
        try {
            val tt = repository.getTimetable(batchName)
            val a = computeScheduleAnalytics(tt.schedule)
            _uiState.update {
                it.copy(
                    loading = false,
                    batchScheduleAnalytics = a,
                    batchTimetableMissing = false,
                    error = null,
                )
            }
        } catch (e: Exception) {
            val notFound = e is HttpException && e.code() == 404
            _uiState.update {
                it.copy(
                    loading = false,
                    batchScheduleAnalytics = null,
                    batchTimetableMissing = notFound,
                    error = if (notFound) null else humanMessage(e),
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
