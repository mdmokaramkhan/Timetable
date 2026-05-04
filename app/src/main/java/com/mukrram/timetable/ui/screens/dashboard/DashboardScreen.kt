package com.mukrram.timetable.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.data.repository.LastTimetableSummary
import com.mukrram.timetable.navigation.ExtraRoutes
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.theme.TimetableTheme
import com.mukrram.timetable.ui.viewmodel.DashboardUiState
import com.mukrram.timetable.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardContent(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onClearError = { viewModel.clearError() },
        onNavigateToNotifications = { navController.navigate(ExtraRoutes.Notifications) },
        onNavigateToGenerate = {
            navController.navigate(MainDestination.Generate.route) {
                popUpTo(MainDestination.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onNavigateToManage = {
            navController.navigate(MainDestination.Manage.route) {
                popUpTo(MainDestination.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onNavigateToTimetable = {
            navController.navigate(MainDestination.Timetable.route) {
                popUpTo(MainDestination.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        onNavigateToAnalytics = { navController.navigate(ExtraRoutes.Analytics) },
        onNavigateToExport = { navController.navigate(ExtraRoutes.Export) },
        onNavigateToProfile = {
            navController.navigate(MainDestination.Profile.route) {
                popUpTo(MainDestination.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGenerate: () -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        onClearError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AppPullToRefreshBox(
            isRefreshing = uiState.loading && uiState.counts != null,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg), // Tighter spacing between sections
            ) {
                when {
                    uiState.loading && uiState.counts == null -> {
                        CenteredLoading(message = "Synchronizing data...")
                    }

                    uiState.error != null && uiState.counts == null && !uiState.loading -> {
                        ErrorState(message = uiState.error, onRetry = onRefresh)
                    }

                    uiState.counts != null -> {
                        val counts = uiState.counts

                        DashboardGreeting(name = "Curator")

                        MetricGrid(counts = counts)

                        HeroPulseSection()

                        QuickActionGrid(
                            onGenerate = onNavigateToGenerate,
                            onManage = onNavigateToManage,
                            onAnalytics = onNavigateToAnalytics,
                            onExport = onNavigateToExport
                        )

                        ActivityFeedSection(
                            summary = uiState.lastTimetableSummary,
                            onSeeAll = onNavigateToTimetable
                        )

                        if (uiState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = AppSpacing.md),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.xl))
            }
        }
    }
}

@Composable
private fun DashboardGreeting(name: String) {
    Column(modifier = Modifier.padding(top = 30.dp)) { // Removed top padding
        Text(
            text = "Hello $name",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Welcome back to your workspace",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricGrid(counts: DashboardCounts) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            ProfessionalMetricCard(
                label = "Faculty",
                value = counts.facultyCount.toString(),
                containerColor = Color(0xFFFFF7E6),
                icon = Icons.Filled.Groups,
                accentColor = Color(0xFFFFA940),
                modifier = Modifier.weight(1f)
            )
            ProfessionalMetricCard(
                label = "Rooms",
                value = counts.roomsCount.toString(),
                containerColor = Color(0xFFF6FFED),
                icon = Icons.Filled.MeetingRoom,
                accentColor = Color(0xFF73D13D),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            ProfessionalMetricCard(
                label = "Batches",
                value = counts.batchesCount.toString(),
                containerColor = Color(0xFFE6F7FF),
                icon = Icons.Filled.School,
                accentColor = Color(0xFF1890FF),
                modifier = Modifier.weight(1f)
            )
            ProfessionalMetricCard(
                label = "Subjects",
                value = counts.subjectsCount.toString(),
                containerColor = Color(0xFFF9F0FF),
                icon = Icons.Filled.Book,
                accentColor = Color(0xFF722ED1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroPulseSection() {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(0.5.dp, MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "System Pulse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Active resource utilization",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val pulseValues = listOf(0.4f, 0.7f, 1f, 0.8f, 0.9f, 0.6f, 0.8f, 0.5f)
                val primaryColor = MaterialTheme.colorScheme.primary
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
                val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer

                pulseValues.forEachIndexed { index, value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(value)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (index == 2) {
                                        listOf(primaryColor, primaryContainer)
                                    } else {
                                        listOf(secondaryContainer, surfaceVariant)
                                    }
                                )
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "High Efficiency",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfessionalMetricCard(
    label: String,
    value: String,
    containerColor: Color,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.shadow(0.5.dp, MaterialTheme.shapes.large),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.25.dp, accentColor.copy(alpha = 0.5f)) // Slightly more visible border
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = accentColor
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickActionGrid(
    onGenerate: () -> Unit,
    onManage: () -> Unit,
    onAnalytics: () -> Unit,
    onExport: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = "Workflow Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            ModernActionButton(
                title = "Generate",
                subtitle = "Engine Run",
                icon = Icons.Filled.AutoAwesome,
                containerColor = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                accentColor = Color(0xFF4F46E5), // Vibrant Indigo
                onClick = onGenerate,
                modifier = Modifier.weight(1f)
            )
            ModernActionButton(
                title = "Manage",
                subtitle = "Data Vault",
                icon = Icons.AutoMirrored.Outlined.ViewList,
                containerColor = if (isDark) Color(0xFF2E1065) else Color(0xFFF9F0FF),
                accentColor = Color(0xFF722ED1),
                onClick = onManage,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            ModernActionButton(
                title = "Analytics",
                subtitle = "Insights",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                containerColor = if (isDark) Color(0xFF450A0A) else Color(0xFFFFF1F0),
                accentColor = Color(0xFFF5222D),
                onClick = onAnalytics,
                modifier = Modifier.weight(1f)
            )
            ModernActionButton(
                title = "Export",
                subtitle = "Reports",
                icon = Icons.Filled.FileDownload,
                containerColor = if (isDark) Color(0xFF171717) else Color(0xFFF5F5F5),
                accentColor = Color(0xFF595959),
                onClick = onExport,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModernActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.height(110.dp).shadow(0.1.dp, MaterialTheme.shapes.large),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.3f)) // Stronger border for better visibility
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun ActivityFeedSection(
    summary: LastTimetableSummary?,
    onSeeAll: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "View Archive",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        AppCard(
            modifier = Modifier.fillMaxWidth().shadow(1.dp, MaterialTheme.shapes.large),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            summary?.let {
                FeedItem(
                    title = it.batch,
                    subtitle = "Automated Timetable Generation",
                    time = it.updatedAt ?: "Moments ago",
                    icon = Icons.Filled.History,
                    iconColor = Color(0xFF1890FF)
                )
            } ?: run {
                FeedItem(
                    title = "System Standby",
                    subtitle = "No recent generations logged",
                    time = "",
                    icon = Icons.Filled.Refresh,
                    iconColor = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun FeedItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .padding(AppSpacing.lg)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DashboardPreview() {
    TimetableTheme(darkTheme = false) {
        DashboardContent(
            uiState = DashboardUiState(
                counts = DashboardCounts(
                    facultyCount = 28,
                    subjectsCount = 42,
                    roomsCount = 12,
                    batchesCount = 8
                ),
                loading = false,
                lastTimetableSummary = LastTimetableSummary(
                    batch = "Fall Semester 2024",
                    updatedAt = "2:23 PM"
                )
            ),
            onRefresh = {},
            onClearError = {},
            onNavigateToNotifications = {},
            onNavigateToGenerate = {},
            onNavigateToManage = {},
            onNavigateToTimetable = {},
            onNavigateToAnalytics = {},
            onNavigateToExport = {},
            onNavigateToProfile = {},
        )
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun DashboardDarkPreview() {
    TimetableTheme(darkTheme = true) {
        DashboardContent(
            uiState = DashboardUiState(
                counts = DashboardCounts(
                    facultyCount = 28,
                    subjectsCount = 42,
                    roomsCount = 12,
                    batchesCount = 8
                ),
                loading = false,
                lastTimetableSummary = LastTimetableSummary(
                    batch = "Fall Semester 2024",
                    updatedAt = "2:23 PM"
                )
            ),
            onRefresh = {},
            onClearError = {},
            onNavigateToNotifications = {},
            onNavigateToGenerate = {},
            onNavigateToManage = {},
            onNavigateToTimetable = {},
            onNavigateToAnalytics = {},
            onNavigateToExport = {},
            onNavigateToProfile = {},
        )
    }
}
