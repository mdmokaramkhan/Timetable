package com.mukrram.timetable.ui.timetable

import com.mukrram.timetable.data.remote.dto.ScheduleCellDto

/** Default Mon–Fri × S1–S8 grid (matches backend timetable constants). */
val DefaultTimetableDays: List<String> = listOf(
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
)

val DefaultTimetableSlots: List<String> = listOf(
    "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
)

fun cellAt(
    schedule: Map<String, List<ScheduleCellDto>>,
    day: String,
    slot: String,
): ScheduleCellDto? =
    schedule[day]?.firstOrNull { it.slot == slot }

fun uniqueFacultyNames(schedule: Map<String, List<ScheduleCellDto>>): List<String> =
    schedule.values.flatten().map { it.faculty }.distinct().sorted()

fun uniqueRoomNames(schedule: Map<String, List<ScheduleCellDto>>): List<String> =
    schedule.values.flatten().map { it.room }.distinct().sorted()
