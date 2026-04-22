package com.mukrram.timetable.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.FacultyBatchTimetableDto
import com.mukrram.timetable.data.remote.dto.FacultyDto
import com.mukrram.timetable.data.remote.dto.RoomDto
import com.mukrram.timetable.data.remote.dto.SavedTimetableResponse
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.data.remote.dto.SubjectDto
import com.mukrram.timetable.data.remote.dto.SaveTimetableRequest
import com.mukrram.timetable.data.repository.TimetableRepository
import com.mukrram.timetable.ui.timetable.applyCellUpdate
import com.mukrram.timetable.ui.timetable.deepCopy
import com.mukrram.timetable.ui.timetable.uniqueFacultyNames
import com.mukrram.timetable.ui.timetable.uniqueRoomNames
import com.mukrram.timetable.ui.timetable.validateScheduleConflicts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class TimetableViewerTab {
    ByBatch,
    ByFaculty,
    ByRoom,
}

data class TimetableViewerUiState(
    /** Faculty uses GET /timetable/faculty/me; grid is read-only. */
    val isFacultyViewer: Boolean = false,
    val facultyBatchSlices: List<FacultyBatchTimetableDto> = emptyList(),
    val facultyDisplayName: String? = null,
    val loadingBatches: Boolean = false,
    val loadingTimetable: Boolean = false,
    val loadingMaster: Boolean = false,
    val saving: Boolean = false,
    val batches: List<BatchDto> = emptyList(),
    val selectedBatchName: String? = null,
    val timetable: SavedTimetableResponse? = null,
    /** Local edits; when non-null, grid reflects this instead of [timetable]. */
    val draftSchedule: Map<String, List<ScheduleCellDto>>? = null,
    val subjects: List<SubjectDto> = emptyList(),
    val faculties: List<FacultyDto> = emptyList(),
    val rooms: List<RoomDto> = emptyList(),
    val tab: TimetableViewerTab = TimetableViewerTab.ByBatch,
    val filterFaculty: String? = null,
    val filterRoom: String? = null,
    val error: String? = null,
)

class TimetableViewerViewModel(
    private val repository: TimetableRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableViewerUiState(loadingBatches = true))
    val uiState: StateFlow<TimetableViewerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val session = repository.sessionState.first()) {
                is SessionState.LoggedIn -> {
                    if (session.role == UserRole.Faculty) {
                        loadFacultyTimetable()
                    } else {
                        loadBatches()
                    }
                }
                SessionState.LoggedOut -> {
                    _uiState.update { it.copy(loadingBatches = false) }
                }
            }
        }
    }

    fun loadMasterData() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingMaster = true) }
            try {
                val faculty = repository.getFaculty()
                val subjects = repository.getSubjects()
                val rooms = repository.getRooms()
                _uiState.update {
                    it.copy(loadingMaster = false, faculties = faculty, subjects = subjects, rooms = rooms)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loadingMaster = false, error = humanMessage(e))
                }
            }
        }
    }

    fun loadBatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingBatches = true, error = null) }
            try {
                val batches = repository.getBatches()
                val selected = _uiState.value.selectedBatchName
                    ?: batches.firstOrNull()?.name
                _uiState.update {
                    it.copy(
                        loadingBatches = false,
                        batches = batches,
                        selectedBatchName = selected,
                    )
                }
                if (selected != null) {
                    fetchTimetable(selected)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loadingBatches = false, error = humanMessage(e))
                }
            }
        }
    }

    private fun loadFacultyTimetable() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loadingBatches = true,
                    loadingTimetable = true,
                    isFacultyViewer = true,
                    error = null,
                )
            }
            try {
                val data = repository.getFacultyTimetableMe()
                val slices = data.batches
                val selected = _uiState.value.selectedBatchName
                    ?: slices.firstOrNull()?.batch
                val slice = slices.find { it.batch == selected } ?: slices.firstOrNull()
                val tt = slice?.let { s ->
                    SavedTimetableResponse(
                        batch = s.batch,
                        schedule = s.schedule,
                        updatedAt = s.updatedAt,
                    )
                }
                val schedule = tt?.schedule.orEmpty()
                _uiState.update { st ->
                    st.copy(
                        loadingBatches = false,
                        loadingTimetable = false,
                        facultyBatchSlices = slices,
                        facultyDisplayName = data.faculty.name,
                        selectedBatchName = slice?.batch,
                        timetable = tt,
                        draftSchedule = null,
                        tab = TimetableViewerTab.ByFaculty,
                        filterFaculty = data.faculty.name,
                        filterRoom = st.filterRoom?.takeIf { r ->
                            uniqueRoomNames(schedule).contains(r)
                        } ?: uniqueRoomNames(schedule).firstOrNull(),
                    )
                }
                if (tt != null) {
                    repository.cacheLastTimetable(tt)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingBatches = false,
                        loadingTimetable = false,
                        timetable = null,
                        error = humanMessage(e),
                    )
                }
            }
        }
    }

    fun onBatchSelected(name: String) {
        val st = _uiState.value
        if (st.isFacultyViewer) {
            val slice = st.facultyBatchSlices.find { it.batch == name } ?: return
            val tt = SavedTimetableResponse(
                batch = slice.batch,
                schedule = slice.schedule,
                updatedAt = slice.updatedAt,
            )
            val schedule = tt.schedule
            _uiState.update {
                it.copy(
                    selectedBatchName = name,
                    timetable = tt,
                    draftSchedule = null,
                    filterFaculty = it.facultyDisplayName,
                    filterRoom = it.filterRoom?.takeIf { r ->
                        uniqueRoomNames(schedule).contains(r)
                    } ?: uniqueRoomNames(schedule).firstOrNull(),
                )
            }
            viewModelScope.launch { repository.cacheLastTimetable(tt) }
            return
        }
        _uiState.update {
            it.copy(
                selectedBatchName = name,
                timetable = null,
                draftSchedule = null,
                filterFaculty = null,
                filterRoom = null,
            )
        }
        fetchTimetable(name)
    }

    fun onTabSelected(tab: TimetableViewerTab) {
        _uiState.update { st ->
            val schedule = displaySchedule(st)
            val nextFaculty = when (tab) {
                TimetableViewerTab.ByFaculty -> st.filterFaculty ?: uniqueFacultyNames(schedule).firstOrNull()
                else -> st.filterFaculty
            }
            val nextRoom = when (tab) {
                TimetableViewerTab.ByRoom -> st.filterRoom ?: uniqueRoomNames(schedule).firstOrNull()
                else -> st.filterRoom
            }
            st.copy(tab = tab, filterFaculty = nextFaculty, filterRoom = nextRoom)
        }
    }

    fun onFacultyFilterSelected(name: String?) {
        _uiState.update { it.copy(filterFaculty = name) }
    }

    fun onRoomFilterSelected(name: String?) {
        _uiState.update { it.copy(filterRoom = name) }
    }

    fun refreshTimetable() {
        val st = _uiState.value
        if (st.isFacultyViewer) {
            loadFacultyTimetable()
            return
        }
        val name = st.selectedBatchName ?: return
        fetchTimetable(name)
    }

    fun discardDraft() {
        _uiState.update { it.copy(draftSchedule = null) }
    }

    fun updateCell(day: String, slot: String, subject: String, faculty: String, room: String) {
        val st = _uiState.value
        if (st.isFacultyViewer) return
        val base = st.draftSchedule ?: st.timetable?.schedule?.deepCopy() ?: return
        val cell = ScheduleCellDto(
            slot = slot,
            subject = subject.trim(),
            faculty = faculty.trim(),
            room = room.trim(),
        )
        val next = applyCellUpdate(base, day, slot, cell)
        _uiState.update { it.copy(draftSchedule = next) }
    }

    fun clearCell(day: String, slot: String) {
        val st = _uiState.value
        if (st.isFacultyViewer) return
        val base = st.draftSchedule ?: st.timetable?.schedule?.deepCopy() ?: return
        val next = applyCellUpdate(base, day, slot, null)
        _uiState.update { it.copy(draftSchedule = next) }
    }

    fun saveDraft() {
        val st = _uiState.value
        if (st.isFacultyViewer) return
        val batch = st.selectedBatchName ?: return
        val schedule = displaySchedule(st)
        if (schedule.isEmpty()) {
            _uiState.update { it.copy(error = "Nothing to save") }
            return
        }
        val v = validateScheduleConflicts(schedule)
        if (!v.ok) {
            val msg = v.errors.firstOrNull() ?: "Schedule has conflicts"
            _uiState.update { it.copy(error = msg) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            try {
                repository.saveTimetable(SaveTimetableRequest(batch, schedule))
                _uiState.update { it.copy(saving = false, draftSchedule = null) }
                fetchTimetable(batch)
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, error = humanMessage(e)) }
            }
        }
    }

    private fun fetchTimetable(batchName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingTimetable = true, error = null) }
            try {
                val tt = repository.getTimetable(batchName)
                repository.cacheLastTimetable(tt)
                val schedule = tt.schedule
                _uiState.update { st ->
                    st.copy(
                        loadingTimetable = false,
                        timetable = tt,
                        draftSchedule = null,
                        filterFaculty = st.filterFaculty?.takeIf { f ->
                            uniqueFacultyNames(schedule).contains(f)
                        } ?: uniqueFacultyNames(schedule).firstOrNull(),
                        filterRoom = st.filterRoom?.takeIf { r ->
                            uniqueRoomNames(schedule).contains(r)
                        } ?: uniqueRoomNames(schedule).firstOrNull(),
                    )
                }
            } catch (e: Exception) {
                val notFound = e is HttpException && e.code() == 404
                _uiState.update {
                    it.copy(
                        loadingTimetable = false,
                        timetable = null,
                        draftSchedule = null,
                        error = if (notFound) {
                            "No saved timetable for this batch. Generate and save one first."
                        } else {
                            humanMessage(e)
                        },
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun displaySchedule(st: TimetableViewerUiState): Map<String, List<ScheduleCellDto>> =
        st.draftSchedule ?: st.timetable?.schedule ?: emptyMap()

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
