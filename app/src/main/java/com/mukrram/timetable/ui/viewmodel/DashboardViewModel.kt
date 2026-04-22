package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.data.repository.LastTimetableSummary
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class DashboardUiState(
    val counts: DashboardCounts? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val lastTimetableSummary: LastTimetableSummary? = null,
)

class DashboardViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            repository.lastTimetableSummary.collect { summary ->
                _uiState.update { it.copy(lastTimetableSummary = summary) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val counts = repository.getDashboardCounts()
                _uiState.update {
                    it.copy(counts = counts, loading = false, error = null)
                }
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
}
