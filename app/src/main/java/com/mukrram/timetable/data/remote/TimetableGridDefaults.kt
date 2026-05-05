package com.mukrram.timetable.data.remote

/**
 * Canonical Mon–Fri × S1–S8 grid. Must stay in sync with
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
        "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
    )
}
