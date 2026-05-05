package com.mukrram.timetable.ui.timetable

import com.mukrram.timetable.data.remote.TimetableGridDefaults
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto

/** Default Mon–Fri × hourly slot grid (same source as [TimetableGridDefaults]). */
val DefaultTimetableDays: List<String> = TimetableGridDefaults.DAYS

val DefaultTimetableSlots: List<String> = TimetableGridDefaults.SLOTS

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
