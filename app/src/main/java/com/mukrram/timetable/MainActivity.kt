package com.mukrram.timetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.ui.TimetableApp
import com.mukrram.timetable.ui.theme.TimetableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ServiceLocator.init(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimetableTheme {
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
    TimetableTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            TimetableApp()
        }
    }
}
