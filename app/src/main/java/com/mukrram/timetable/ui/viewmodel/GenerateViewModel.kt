package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.TimetableGridDefaults
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.GenerateTimetableRequest
import com.mukrram.timetable.data.remote.dto.GenerateTimetableResponse
import com.mukrram.timetable.data.remote.dto.SaveTimetableRequest
import com.mukrram.timetable.data.remote.dto.TimetableOptionDto
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class GenerateUiState(
    val loadingSummary: Boolean = false,
    val loadingGenerate: Boolean = false,
    val savingOptionId: String? = null,
    val counts: DashboardCounts? = null,
    val batches: List<BatchDto> = emptyList(),
    val selectedBatchName: String? = null,
    /** Matches server-side default capacity; synced with slider before first edit. */
    val maxClassesPerDayText: String = "6",
    /** Backend returns 2–3 option rows; caller should keep in that range. */
    val optionsCount: Int = 3,
    val generateResult: GenerateTimetableResponse? = null,
    val error: String? = null,
    val saveSuccessMessage: String? = null,
)

class GenerateViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateUiState(loadingSummary = true))
    val uiState: StateFlow<GenerateUiState> = _uiState.asStateFlow()

    init {
        refreshSummary()
    }

    fun refreshSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSummary = true, error = null) }
            try {
                val faculty = async { repository.getFaculty() }
                val subjects = async { repository.getSubjects() }
                val rooms = async { repository.getRooms() }
                val batches = async { repository.getBatches() }
                val counts = DashboardCounts(
                    facultyCount = faculty.await().size,
                    subjectsCount = subjects.await().size,
                    roomsCount = rooms.await().size,
                    batchesCount = batches.await().size,
                )
                val batchList = batches.await()
                _uiState.update {
                    it.copy(
                        loadingSummary = false,
                        counts = counts,
                        batches = batchList,
                        selectedBatchName = it.selectedBatchName
                            ?: batchList.firstOrNull()?.name,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingSummary = false,
                        error = humanMessage(e),
                    )
                }
            }
        }
    }

    fun onBatchSelected(name: String) {
        _uiState.update { it.copy(selectedBatchName = name, generateResult = null) }
    }

    fun onMaxClassesPerDayChange(text: String) {
        if (text.all { it.isDigit() } || text.isEmpty()) {
            _uiState.update { it.copy(maxClassesPerDayText = text) }
        }
    }

    fun onOptionsCountChange(count: Int) {
        val c = count.coerceIn(2, 3)
        _uiState.update { it.copy(optionsCount = c) }
    }

    fun generate() {
        val batch = _uiState.value.selectedBatchName?.trim().orEmpty()
        if (batch.isEmpty()) {
            _uiState.update { it.copy(error = "Select a batch") }
            return
        }
        val maxPerDay = _uiState.value.maxClassesPerDayText.trim().toIntOrNull()
        val opts = _uiState.value.optionsCount.coerceIn(2, 3)
        viewModelScope.launch {
            _uiState.update {
                it.copy(loadingGenerate = true, error = null, generateResult = null)
            }
            try {
                val body = GenerateTimetableRequest(
                    batch = batch,
                    optionsCount = opts,
                    maxClassesPerDay = maxPerDay,
                    days = TimetableGridDefaults.DAYS,
                    slots = TimetableGridDefaults.SLOTS,
                )
                val result = repository.generateTimetable(body)
                _uiState.update { it.copy(loadingGenerate = false, generateResult = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loadingGenerate = false, error = humanMessage(e))
                }
            }
        }
    }

    fun selectAndSaveOption(option: TimetableOptionDto) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(savingOptionId = option.id, error = null, saveSuccessMessage = null)
            }
            try {
                repository.saveTimetable(
                    SaveTimetableRequest(
                        batch = option.batch,
                        schedule = option.schedule,
                    ),
                )
                _uiState.update {
                    it.copy(
                        savingOptionId = null,
                        saveSuccessMessage = "Timetable saved for ${option.batch}",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(savingOptionId = null, error = humanMessage(e))
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeSaveMessage() {
        _uiState.update { it.copy(saveSuccessMessage = null) }
    }

    private fun humanMessage(e: Exception): String {
        if (e is HttpException) {
            val raw = e.response()?.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                val msg = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(raw)?.groupValues?.getOrNull(1)
                if (!msg.isNullOrBlank()) return msg
            }
            return e.message ?: "Request failed (${e.code()})"
        }
        return e.message ?: "Something went wrong"
    }
}
