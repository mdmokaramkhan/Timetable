package com.mukrram.timetable.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.navigation.ExtraRoutes
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "The Academic Curator",
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Outlined.MenuOpen, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ExtraRoutes.Notifications) }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            ProfileHeroCard(
                displayName = uiState.displayName.ifBlank { "User" },
                username = uiState.username.ifBlank { "Signed in user" },
                roleLabel = uiState.roleLabel,
            )
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "System Settings",
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ProfileSettingRow(
                        title = "Notification Settings",
                        subtitle = "Alerts, sounds, and scheduling updates",
                        icon = Icons.Filled.NotificationsActive,
                        onClick = { navController.navigate(ExtraRoutes.Notifications) },
                    )
                    ProfileSettingRow(
                        title = "Appearance",
                        subtitle = "Switch between Light and Dark mode",
                        icon = Icons.Filled.Palette,
                        trailingLabel = "System",
                        onClick = {},
                    )
                    Text(
                        text = "Data Management",
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (uiState.isAdmin) {
                        ProfileSettingRow(
                            title = "Analytics",
                            subtitle = "Class counts, faculty load, and room usage",
                            icon = Icons.Filled.Analytics,
                            onClick = { navController.navigate(ExtraRoutes.Analytics) },
                        )
                        ProfileSettingRow(
                            title = "Export Data",
                            subtitle = "CSV and text timetable formats",
                            icon = Icons.Filled.UploadFile,
                            onClick = { navController.navigate(ExtraRoutes.Export) },
                        )
                    }
                    ProfileSettingRow(
                        title = "About",
                        subtitle = "App details and documentation",
                        icon = Icons.Filled.Info,
                        onClick = {},
                    )
                }
            }
            AppButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text("Sign Out from Curator", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    displayName: String,
    username: String,
    roleLabel: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = if (roleLabel == "Administrator") "Institutional Administrator" else roleLabel,
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProfileSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailingLabel: String? = null,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
            },
            supportingContent = {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingContent = {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    trailingLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            },
        )
    }
}
