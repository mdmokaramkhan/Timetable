package com.mukrram.timetable.ui.screens.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing

private data class DemoNotice(
    val title: String,
    val body: String,
)

private val demoNotices = listOf(
    DemoNotice(
        title = "Welcome",
        body = "Use Manage to add faculty, subjects, rooms, and batches before generating.",
    ),
    DemoNotice(
        title = "After generate",
        body = "Pick an option, save it, then open Timetable to view or edit the grid.",
    ),
    DemoNotice(
        title = "Substitution",
        body = "From Timetable, open Substitution to handle an absent faculty member.",
    ),
    DemoNotice(
        title = "Offline",
        body = "When you are offline, the app shows a banner. Last viewed timetable is cached for recent activity on Dashboard.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "Notifications",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = "Demo notices (static list for your CA walkthrough).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            demoNotices.forEach { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    ListItem(
                        headlineContent = { Text(notice.title) },
                        supportingContent = {
                            Text(
                                notice.body,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }
        }
    }
}
