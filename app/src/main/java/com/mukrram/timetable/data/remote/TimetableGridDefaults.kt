package com.mukrram.timetable.data.remote

/**
 * Canonical Mon–Fri × hourly slot grid. Must stay in sync with
 * `server/src/constants/timetable.js` (TIMETABLE_DAYS, TIMETABLE_SLOTS).
 */
object TimetableGridDefaults {
    val DAYS: List<String> = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
    )

    val SLOTS: List<String> = listOf(
        "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00",
    )
}
