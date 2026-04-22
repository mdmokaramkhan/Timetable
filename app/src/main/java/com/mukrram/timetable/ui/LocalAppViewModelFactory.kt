package com.mukrram.timetable.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.mukrram.timetable.ui.viewmodel.AppViewModelFactory

val LocalAppViewModelFactory = staticCompositionLocalOf<AppViewModelFactory> {
    error("AppViewModelFactory not provided")
}
