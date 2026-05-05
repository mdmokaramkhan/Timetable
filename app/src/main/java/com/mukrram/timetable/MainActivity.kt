package com.mukrram.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.data.model.AppThemeMode
import com.mukrram.timetable.ui.TimetableApp
import com.mukrram.timetable.ui.theme.TimetableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ServiceLocator.init(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by ServiceLocator.repository.themePreference.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.System,
            )
            val darkTheme = themeMode.isDark(isSystemInDarkTheme())
            TimetableTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TimetableApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimetableAppPreview() {
    TimetableTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            TimetableApp()
        }
    }
}
