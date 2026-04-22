package com.mukrram.timetable.ui.analytics

import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.ui.timetable.DefaultTimetableDays
import com.mukrram.timetable.ui.timetable.DefaultTimetableSlots
import com.mukrram.timetable.ui.timetable.cellAt

data class ScheduleAnalytics(
    val scheduledClasses: Int,
    val freeSlots: Int,
    val totalSlots: Int,
    val facultyLoad: List<Pair<String, Int>>,
    val roomUsage: List<Pair<String, Int>>,
)

fun computeScheduleAnalytics(
    schedule: Map<String, List<ScheduleCellDto>>,
    days: List<String> = DefaultTimetableDays,
    slots: List<String> = DefaultTimetableSlots,
): ScheduleAnalytics {
    val totalSlots = days.size * slots.size
    var occupied = 0
    val facultyCounts = mutableMapOf<String, Int>()
    val roomCounts = mutableMapOf<String, Int>()
    for (day in days) {
        for (slot in slots) {
            val cell = cellAt(schedule, day, slot)
            if (cell != null && cell.subject.isNotBlank()) {
                occupied++
                val f = cell.faculty.trim()
                val r = cell.room.trim()
                if (f.isNotEmpty()) {
                    facultyCounts[f] = facultyCounts.getOrDefault(f, 0) + 1
                }
                if (r.isNotEmpty()) {
                    roomCounts[r] = roomCounts.getOrDefault(r, 0) + 1
                }
            }
        }
    }
    return ScheduleAnalytics(
        scheduledClasses = occupied,
        freeSlots = (totalSlots - occupied).coerceAtLeast(0),
        totalSlots = totalSlots,
        facultyLoad = facultyCounts.entries.map { it.key to it.value }.sortedByDescending { it.second },
        roomUsage = roomCounts.entries.map { it.key to it.value }.sortedByDescending { it.second },
    )
}

fun formatTimetableAsText(
    batch: String,
    schedule: Map<String, List<ScheduleCellDto>>,
    updatedAt: String?,
    days: List<String> = DefaultTimetableDays,
    slots: List<String> = DefaultTimetableSlots,
): String = buildString {
    appendLine("Timetable — $batch")
    if (!updatedAt.isNullOrBlank()) appendLine("Updated: $updatedAt")
    appendLine()
    for (day in days) {
        appendLine(day)
        for (slot in slots) {
            val c = cellAt(schedule, day, slot)
            if (c != null && c.subject.isNotBlank()) {
                append("  ")
                append(slot)
                append(": ")
                append(c.subject)
                append(" | ")
                append(c.faculty)
                append(" | ")
                appendLine(c.room)
            }
        }
        appendLine()
    }
}
