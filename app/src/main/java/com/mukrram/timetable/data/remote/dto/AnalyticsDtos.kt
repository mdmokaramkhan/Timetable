package com.mukrram.timetable.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnalyticsResponseDto(
    val generatedAt: String? = null,
    val grid: AnalyticsGridDto? = null,
    val summary: AnalyticsSummaryDto? = null,
    val byBatch: List<AnalyticsByBatchDto>? = emptyList(),
    val facultyWorkload: List<FacultyWorkloadEntryDto>? = emptyList(),
    val unmappedFacultyInSchedules: List<UnmappedFacultyEntryDto>? = emptyList(),
    val roomUsage: List<RoomUsageEntryDto>? = emptyList(),
)

data class AnalyticsGridDto(
    val days: Int = 0,
    val slotsPerDay: Int = 0,
    val slotsPerBatch: Int = 0,
    val daysOrder: List<String>? = emptyList(),
    val slotsOrder: List<String>? = emptyList(),
)

data class AnalyticsSummaryDto(
    val timetableCount: Int = 0,
    val batchNames: List<String>? = emptyList(),
    val totalClassesScheduled: Int = 0,
    val totalCapacitySlots: Int = 0,
    val totalFreeSlots: Int = 0,
    val overallUtilization: Double = 0.0,
)

data class AnalyticsByBatchDto(
    val batch: String = "",
    val scheduledSlots: Int = 0,
    val freeSlots: Int = 0,
    val capacitySlots: Int = 0,
)

data class FacultyWorkloadEntryDto(
    @SerializedName("facultyId") val facultyId: String? = null,
    val name: String = "",
    val scheduledSlots: Int = 0,
    val maxLoad: Int = 0,
    val utilization: Double? = null,
)

data class UnmappedFacultyEntryDto(
    val nameKey: String = "",
    val scheduledSlots: Int = 0,
)

data class RoomUsageEntryDto(
    val room: String = "",
    val scheduledSlots: Int = 0,
    val percentOfAssignments: Double = 0.0,
)
