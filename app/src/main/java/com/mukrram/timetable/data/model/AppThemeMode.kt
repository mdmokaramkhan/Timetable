package com.mukrram.timetable.data.model

enum class AppThemeMode(
    val storageValue: String,
    val label: String,
    val description: String,
) {
    Light(
        storageValue = "light",
        label = "Light",
        description = "Always use light appearance",
    ),
    Dark(
        storageValue = "dark",
        label = "Dark",
        description = "Always use dark appearance",
    ),
    System(
        storageValue = "system",
        label = "System default",
        description = "Match battery saver and display settings",
    ),
    ;

    companion object {
        fun fromStorage(value: String?): AppThemeMode =
            entries.find { it.storageValue == value } ?: System
    }

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        Light -> false
        Dark -> true
        System -> systemIsDark
    }

    fun summaryLabel(): String = label
}
