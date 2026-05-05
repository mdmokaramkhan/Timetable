package com.mukrram.timetable.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScheduleCellDto(
    val slot: String,
    val subject: String,
    val faculty: String,
    val room: String,
)

data class GenerateTimetableRequest(
    val batch: String,
    /** Server clamps to 2…3 distinct options. */
    val optionsCount: Int? = null,
    /** Omit or null for no per-day cap (matches generator). */
    val maxClassesPerDay: Int? = null,
    /** When set, must match server timetable constants. */
    val days: List<String>? = null,
    val slots: List<String>? = null,
)

data class GenerateStatsDto(
    val placed: Int,
    val required: Int,
    val facultyLoad: Map<String, Int>? = null,
)

data class TimetableOptionDto(
    val id: String,
    val batch: String,
    val schedule: Map<String, List<ScheduleCellDto>>,
    val stats: GenerateStatsDto,
)

data class GenerateTimetableResponse(
    val batch: String,
    val days: List<String>,
    val slots: List<String>,
    val options: List<TimetableOptionDto>,
)

data class SaveTimetableRequest(
    val batch: String,
    val schedule: Map<String, List<ScheduleCellDto>>,
)

data class SaveTimetableResponse(
    val ok: Boolean = false,
    /** Present on 200 responses from POST /timetable/save. */
    val timetable: SavedTimetableResponse? = null,
)

data class SavedTimetableResponse(
    val batch: String,
    val schedule: Map<String, List<ScheduleCellDto>>,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    val conflicts: List<Any>? = null,
)

/** GET /timetable/faculty/me — slices per batch where this faculty teaches. */
data class FacultyTimetableMeResponse(
    val faculty: FacultyTimetableProfileDto,
    val batches: List<FacultyBatchTimetableDto>,
)

data class FacultyTimetableProfileDto(
    val id: String? = null,
    val name: String,
    val subjects: List<String>? = null,
    val maxLoad: Int? = null,
)

data class FacultyBatchTimetableDto(
    val batch: String,
    val schedule: Map<String, List<ScheduleCellDto>>,
    @SerializedName("updatedAt") val updatedAt: String? = null,
)

data class SubstituteRequest(
    val batch: String,
    val absentFaculty: String,
    val day: String? = null,
    val slot: String? = null,
    val apply: Boolean? = null,
    val replacementFaculty: String? = null,
)

data class AffectedSlotDto(
    val day: String,
    val slot: String,
    val subject: String? = null,
    val faculty: String,
    val room: String,
)

data class UnavailableFacultyDto(
    val name: String,
    val reason: String? = null,
)

/** Response shape varies: list-only, suggestion, or apply result. */
data class SubstituteResponse(
    val ok: Boolean? = null,
    val applied: Boolean? = null,
    val batch: String? = null,
    val absentFaculty: String? = null,
    val affectedSlots: List<AffectedSlotDto>? = null,
    val subject: String? = null,
    val day: String? = null,
    val slot: String? = null,
    val room: String? = null,
    val suggestedReplacement: String? = null,
    val alternatives: List<String>? = null,
    val unavailable: List<UnavailableFacultyDto>? = null,
    val replacementFaculty: String? = null,
    val timetable: SavedTimetableResponse? = null,
    val error: String? = null,
)
