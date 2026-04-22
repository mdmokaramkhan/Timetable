package com.mukrram.timetable.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Error
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.repository.LastTimetableSummary
import com.mukrram.timetable.navigation.ExtraRoutes
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "The Academic Curator",
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh counts")
                    }
                    IconButton(onClick = { navController.navigate(ExtraRoutes.Notifications) }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AppPullToRefreshBox(
            isRefreshing = uiState.loading && uiState.counts != null,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
            ) {
                when {
                    uiState.loading && uiState.counts == null -> {
                        CenteredLoading(message = "Loading dashboard…")
                    }

                    uiState.error != null && uiState.counts == null && !uiState.loading -> {
                        ErrorState(
                            message = uiState.error ?: "Unknown error",
                            onRetry = { viewModel.refresh() },
                        )
                    }

                    else -> {
                        val c = uiState.counts
                        if (c != null) {
                            DashboardHeadline()
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val wideLayout = maxWidth > 900.dp
                                if (wideLayout) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        LeftDashboardColumn(
                                            counts = c,
                                            lastSummary = uiState.lastTimetableSummary,
                                            modifier = Modifier.weight(8f),
                                            onGenerate = {
                                                navController.navigate(MainDestination.Generate.route) {
                                                    popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            onManage = {
                                                navController.navigate(MainDestination.Manage.route) {
                                                    popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        )
                                        RightDashboardColumn(
                                            counts = c,
                                            lastSummary = uiState.lastTimetableSummary,
                                            modifier = Modifier.weight(4f),
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                                    ) {
                                        LeftDashboardColumn(
                                            counts = c,
                                            lastSummary = uiState.lastTimetableSummary,
                                            onGenerate = {
                                                navController.navigate(MainDestination.Generate.route) {
                                                    popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            onManage = {
                                                navController.navigate(MainDestination.Manage.route) {
                                                    popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        )
                                        RightDashboardColumn(
                                            counts = c,
                                            lastSummary = uiState.lastTimetableSummary,
                                        )
                                    }
                                }
                            }
                        }
                        if (uiState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = AppSpacing.sm),
                            )
                        }

                        val navigationItems = listOf(
                            HomeNavItem(
                                label = "Generate",
                                icon = Icons.Filled.AutoAwesome,
                                onClick = {
                                    navController.navigate(MainDestination.Generate.route) {
                                        popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            HomeNavItem(
                                label = "Manage",
                                icon = Icons.AutoMirrored.Outlined.ViewList,
                                onClick = {
                                    navController.navigate(MainDestination.Manage.route) {
                                        popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            HomeNavItem(
                                label = "Timetable",
                                icon = Icons.Filled.CalendarMonth,
                                onClick = {
                                    navController.navigate(MainDestination.Timetable.route) {
                                        popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            HomeNavItem(
                                label = "Analytics",
                                icon = Icons.AutoMirrored.Filled.ShowChart,
                                onClick = { navController.navigate(ExtraRoutes.Analytics) },
                            ),
                            HomeNavItem(
                                label = "Export",
                                icon = Icons.Filled.FileDownload,
                                onClick = { navController.navigate(ExtraRoutes.Export) },
                            ),
                            HomeNavItem(
                                label = "Profile",
                                icon = Icons.Filled.Person,
                                onClick = {
                                    navController.navigate(MainDestination.Profile.route) {
                                        popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                        )
                        NavigationGrid(
                            items = navigationItems,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardHeadline() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Manage your institution's resources and scheduling.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LeftDashboardColumn(
    counts: DashboardCounts,
    lastSummary: LastTimetableSummary? = null,
    modifier: Modifier = Modifier,
    onGenerate: () -> Unit,
    onManage: () -> Unit,
) {
    val c = counts
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            HeroActionCard(
                title = "Generate Timetable",
                subtitle = "Smart conflict-free classroom allocation",
                onClick = onGenerate,
                modifier = Modifier.weight(1f),
            )
            SecondaryActionCard(
                title = "Add Data",
                subtitle = "Upload faculty or room details",
                onClick = onManage,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SummaryCard("Faculty", c.facultyCount, Icons.Filled.Groups, Modifier.weight(1f))
            SummaryCard("Subjects", c.subjectsCount, Icons.AutoMirrored.Outlined.MenuBook, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SummaryCard("Rooms", c.roomsCount, Icons.Filled.MeetingRoom, Modifier.weight(1f))
            SummaryCard("Batches", c.batchesCount, Icons.Filled.School, Modifier.weight(1f))
        }
        DataSnapshotCard(counts = c, lastSummary = lastSummary)
    }
}

@Composable
private fun HeroActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.height(188.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ),
                )
                .padding(AppSpacing.lg),
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = "AUTOMATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.height(188.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                style = MaterialTheme.typography.bodySmall,
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    count: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DataSnapshotCard(
    counts: DashboardCounts,
    lastSummary: LastTimetableSummary?,
) {
    val total = counts.facultyCount + counts.subjectsCount + counts.roomsCount + counts.batchesCount
    val readinessPercent = ((total / 40f).coerceIn(0f, 1f) * 100).toInt()
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.primary,
                        ),
                    ),
                )
                .padding(AppSpacing.lg),
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = "DATA SNAPSHOT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "$total total records across core modules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = "Setup readiness: $readinessPercent%${lastSummary?.let { " • Last saved batch: ${it.batch}" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun RightDashboardColumn(
    counts: DashboardCounts,
    lastSummary: LastTimetableSummary?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        RecentActivityCard(counts = counts, lastSummary = lastSummary)
        DataHealthCard(counts = counts)
    }
}

@Composable
private fun RecentActivityCard(
    counts: DashboardCounts,
    lastSummary: LastTimetableSummary?,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = "Backend Snapshot",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            ActivityItem(
                color = MaterialTheme.colorScheme.primary,
                title = "Faculty records fetched: ${counts.facultyCount}",
                subtitle = "Subjects fetched: ${counts.subjectsCount}",
            )
            ActivityItem(
                color = MaterialTheme.colorScheme.secondary,
                title = "Rooms fetched: ${counts.roomsCount}",
                subtitle = "Batches fetched: ${counts.batchesCount}",
            )
            ActivityItem(
                color = MaterialTheme.colorScheme.tertiary,
                title = lastSummary?.let { "Last saved timetable batch: ${it.batch}" } ?: "No saved timetable snapshot",
                subtitle = lastSummary?.updatedAt?.let { "Updated at: $it" } ?: "Generate and save timetable to populate this",
            )
        }
    }
}

@Composable
private fun ActivityItem(
    color: Color,
    title: String,
    subtitle: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DataHealthCard(counts: DashboardCounts) {
    val roomsEnough = counts.roomsCount >= counts.subjectsCount
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                    text = "Data Health",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = if (roomsEnough) {
                    "Room count is currently enough for subject count. You're ready for smoother generation runs."
                } else {
                    "Rooms are fewer than subjects. Add more rooms to reduce scheduling conflicts."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun NavigationGrid(
    items: List<HomeNavItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                rowItems.forEach { item ->
                    AppCard(
                        modifier = Modifier.weight(1f),
                        onClick = item.onClick,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class HomeNavItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)
