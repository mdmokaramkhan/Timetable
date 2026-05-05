package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.remote.dto.AffectedSlotDto
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.FacultyDto
import com.mukrram.timetable.data.remote.dto.SubstituteRequest
import com.mukrram.timetable.data.remote.dto.UnavailableFacultyDto
import com.mukrram.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SubstitutionUiState(
    val loadingBatches: Boolean = true,
    val loadingFaculty: Boolean = false,
    val loadingAffected: Boolean = false,
    val loadingSuggestion: Boolean = false,
    val applying: Boolean = false,
    val batches: List<BatchDto> = emptyList(),
    val facultyList: List<FacultyDto> = emptyList(),
    val selectedBatch: String? = null,
    val absentFaculty: String? = null,
    val affectedSlots: List<AffectedSlotDto> = emptyList(),
    val selectedSlot: AffectedSlotDto? = null,
    val suggestedReplacement: String? = null,
    val alternatives: List<String> = emptyList(),
    /** From POST /substitute when day/slot given — candidates ruled out by load or conflicts. */
    val unavailable: List<UnavailableFacultyDto> = emptyList(),
    val replacementOverride: String? = null,
    val error: String? = null,
)

class SubstitutionViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubstitutionUiState())
    val uiState: StateFlow<SubstitutionUiState> = _uiState.asStateFlow()

    init {
        refreshLists()
    }

    fun refreshLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingBatches = true, loadingFaculty = true, error = null) }
            try {
                val batches = repository.getBatches()
                val faculty = repository.getFaculty()
                _uiState.update {
                    it.copy(
                        loadingBatches = false,
                        loadingFaculty = false,
                        batches = batches,
                        facultyList = faculty.sortedBy { f -> f.name.lowercase() },
                        selectedBatch = it.selectedBatch?.takeIf { n -> batches.any { b -> b.name == n } }
                            ?: batches.firstOrNull()?.name,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loadingBatches = false, loadingFaculty = false, error = humanMessage(e))
                }
            }
        }
    }

    fun onBatchSelected(name: String) {
        _uiState.update {
            it.copy(
                selectedBatch = name,
                absentFaculty = null,
                affectedSlots = emptyList(),
                selectedSlot = null,
                suggestedReplacement = null,
                alternatives = emptyList(),
                unavailable = emptyList(),
                replacementOverride = null,
            )
        }
    }

    fun onAbsentFacultySelected(name: String?) {
        _uiState.update {
            it.copy(
                absentFaculty = name,
                affectedSlots = emptyList(),
                selectedSlot = null,
                suggestedReplacement = null,
                alternatives = emptyList(),
                unavailable = emptyList(),
                replacementOverride = null,
            )
        }
        if (name != null) {
            loadAffectedSlots()
        }
    }

    fun loadAffectedSlots() {
        val batch = _uiState.value.selectedBatch ?: return
        val absent = _uiState.value.absentFaculty ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingAffected = true, error = null) }
            try {
                val res = repository.substitute(
                    SubstituteRequest(batch = batch, absentFaculty = absent),
                )
                _uiState.update {
                    it.copy(
                        loadingAffected = false,
                        affectedSlots = res.affectedSlots.orEmpty(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loadingAffected = false, error = humanMessage(e))
                }
            }
        }
    }

    fun onAffectedSlotSelected(slot: AffectedSlotDto) {
        _uiState.update {
            it.copy(
                selectedSlot = slot,
                suggestedReplacement = null,
                alternatives = emptyList(),
                unavailable = emptyList(),
                replacementOverride = null,
            )
        }
        loadSuggestion(slot.day, slot.slot)
    }

    private fun loadSuggestion(day: String, slot: String) {
        val batch = _uiState.value.selectedBatch ?: return
        val absent = _uiState.value.absentFaculty ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSuggestion = true, error = null) }
            try {
                val res = repository.substitute(
                    SubstituteRequest(
                        batch = batch,
                        absentFaculty = absent,
                        day = day,
                        slot = slot,
                    ),
                )
                _uiState.update {
                    it.copy(
                        loadingSuggestion = false,
                        suggestedReplacement = res.suggestedReplacement,
                        alternatives = res.alternatives.orEmpty(),
                        replacementOverride = res.suggestedReplacement,
                        unavailable = res.unavailable.orEmpty(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingSuggestion = false,
                        unavailable = emptyList(),
                        error = humanMessage(e),
                    )
                }
            }
        }
    }

    fun onReplacementOverrideSelected(name: String?) {
        _uiState.update { it.copy(replacementOverride = name) }
    }

    fun applySubstitution() {
        val batch = _uiState.value.selectedBatch ?: return
        val absent = _uiState.value.absentFaculty ?: return
        val sel = _uiState.value.selectedSlot ?: return
        val rep = _uiState.value.replacementOverride ?: _uiState.value.suggestedReplacement
        if (rep.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Choose a replacement faculty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(applying = true, error = null) }
            try {
                repository.substitute(
                    SubstituteRequest(
                        batch = batch,
                        absentFaculty = absent,
                        day = sel.day,
                        slot = sel.slot,
                        apply = true,
                        replacementFaculty = rep,
                    ),
                )
                _uiState.update {
                    it.copy(
                        applying = false,
                        selectedSlot = null,
                        suggestedReplacement = null,
                        alternatives = emptyList(),
                        unavailable = emptyList(),
                        replacementOverride = null,
                    )
                }
                loadAffectedSlots()
            } catch (e: Exception) {
                _uiState.update { it.copy(applying = false, error = humanMessage(e)) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
