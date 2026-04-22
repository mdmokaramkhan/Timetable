package com.mukrram.timetable.ui.timetable

import com.mukrram.timetable.data.remote.dto.ScheduleCellDto

data class ScheduleValidationResult(
    val ok: Boolean,
    val errors: List<String>,
    /** `day|slot` keys that participate in a faculty or room clash. */
    val conflictKeys: Set<String>,
)

/**
 * Mirrors server [validateScheduleShape]: duplicate faculty or duplicate room at the same day|slot.
 */
fun validateScheduleConflicts(schedule: Map<String, List<ScheduleCellDto>>): ScheduleValidationResult {
    val errors = mutableListOf<String>()
    val conflictKeys = mutableSetOf<String>()
    val daySet = DefaultTimetableDays.toSet()
    val slotSet = DefaultTimetableSlots.toSet()

    val bySlot = mutableMapOf<String, MutableList<Pair<String, String>>>()

    for ((day, entries) in schedule) {
        if (!daySet.contains(day)) {
            errors.add("Unknown day \"$day\"")
            continue
        }
        for ((idx, entry) in entries.withIndex()) {
            val slotKey = entry.slot.trim()
            if (slotKey.isEmpty()) {
                errors.add("Day \"$day\" entry $idx missing slot")
                continue
            }
            if (!slotSet.contains(slotKey)) {
                errors.add("Day \"$day\" entry $idx has unknown slot \"$slotKey\"")
            }
            val fac = entry.faculty.trim()
            val room = entry.room.trim()
            if (fac.isEmpty()) {
                errors.add("Day \"$day\" entry $idx missing faculty")
                continue
            }
            if (room.isEmpty()) {
                errors.add("Day \"$day\" entry $idx missing room")
                continue
            }
            val key = "$day|$slotKey"
            bySlot.getOrPut(key) { mutableListOf() }.add(fac to room)
        }
    }

    for ((key, list) in bySlot) {
        val faculties = list.map { it.first }
        val rooms = list.map { it.second }
        val facultyDup = faculties.filterIndexed { i, f -> faculties.indexOf(f) != i }.distinct()
        val roomDup = rooms.filterIndexed { i, r -> rooms.indexOf(r) != i }.distinct()
        if (facultyDup.isNotEmpty()) {
            errors.add("Faculty clash at $key: \"${facultyDup.joinToString(", ")}\"")
            conflictKeys.add(key)
        }
        if (roomDup.isNotEmpty()) {
            errors.add("Room clash at $key: \"${roomDup.joinToString(", ")}\"")
            conflictKeys.add(key)
        }
    }

    return ScheduleValidationResult(
        ok = errors.isEmpty(),
        errors = errors,
        conflictKeys = conflictKeys,
    )
}

fun computeConflictKeysOnly(schedule: Map<String, List<ScheduleCellDto>>): Set<String> =
    validateScheduleConflicts(schedule).conflictKeys

fun Map<String, List<ScheduleCellDto>>.deepCopy(): Map<String, List<ScheduleCellDto>> =
    mapValues { (_, cells) -> cells.map { it.copy() } }

private val slotIndex: Map<String, Int> = DefaultTimetableSlots.withIndex().associate { it.value to it.index }

fun applyCellUpdate(
    schedule: Map<String, List<ScheduleCellDto>>,
    day: String,
    slot: String,
    newCell: ScheduleCellDto?,
): Map<String, List<ScheduleCellDto>> {
    val mutable = schedule.mapValues { (_, cells) -> cells.toMutableList() }.toMutableMap()
    val list = mutable.getOrPut(day) { mutableListOf() }
    val idx = list.indexOfFirst { it.slot == slot }
    if (newCell == null) {
        if (idx >= 0) list.removeAt(idx)
        if (list.isEmpty()) mutable.remove(day)
    } else {
        if (idx >= 0) list[idx] = newCell
        else list.add(newCell)
    }
    return mutable.mapValues { (_, cells) ->
        cells.sortedBy { slotIndex[it.slot] ?: 99 }
    }
}
