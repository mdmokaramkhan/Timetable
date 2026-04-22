package com.mukrram.timetable.ui.screens.analytics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.remote.dto.AnalyticsResponseDto
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.AnalyticsSummaryDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.analytics.ScheduleAnalytics
import com.mukrram.timetable.ui.components.AppFilterChip
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.AnalyticsViewModel

@Composable
private fun analyticsCardElevation() = CardDefaults.cardElevation(defaultElevation = 0.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = viewModel(factory = LocalAppViewModelFactory.current),
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
                titleText = "Analytics",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.loading,
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AppPullToRefreshBox(
            isRefreshing = uiState.loading && uiState.serverSnapshot != null,
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
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    uiState.batches.isEmpty() && uiState.loading && uiState.serverSnapshot == null -> {
                        CenteredLoading(message = "Loading analytics…")
                    }

                    uiState.error != null && uiState.serverSnapshot == null && !uiState.loading -> {
                        ErrorState(
                            message = uiState.error ?: "Could not load analytics",
                            onRetry = { viewModel.retryAfterError() },
                        )
                    }

                    uiState.loading && uiState.serverSnapshot == null && uiState.error == null -> {
                        CenteredLoading(message = "Loading analytics…")
                    }

                    uiState.serverSnapshot != null -> {
                        val snap = uiState.serverSnapshot!!
                        val summary = snap.summary ?: AnalyticsSummaryDto()
                        val sortedFaculty = remember(snap.facultyWorkload) {
                            snap.facultyWorkload
                                .orEmpty()
                                .sortedByDescending { it.scheduledSlots }
                        }
                        val topFaculty = sortedFaculty.firstOrNull { it.name.isNotBlank() && it.scheduledSlots > 0 }
                        val topRoom = snap.roomUsage.orEmpty().maxByOrNull { it.scheduledSlots }

                        ServerOverviewSection(
                            summary = summary,
                            generatedAt = snap.generatedAt,
                            topFacultyName = topFaculty?.name.orEmpty(),
                            topFacultySlots = topFaculty?.scheduledSlots ?: 0,
                            topRoomName = topRoom?.room.orEmpty(),
                            topRoomSlots = topRoom?.scheduledSlots ?: 0,
                        )

                        if (!snap.unmappedFacultyInSchedules.isNullOrEmpty()) {
                            UnmappedFacultyCard(snap)
                        }

                        SectionHeader(
                            title = "Campus workload",
                            subtitle = "Top signals only for quick decisions",
                        )

                        val facultyChartPairs = remember(sortedFaculty) {
                            sortedFaculty
                                .filter { it.scheduledSlots > 0 }
                                .take(10)
                                .map { it.name to it.scheduledSlots }
                        }
                        AnalyticsGroupCard(
                            title = "Faculty load",
                            subtitle = "Most scheduled faculty this week",
                            accent = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            TrendHighlightRow(
                                title = "Most loaded faculty",
                                detail = if (topFaculty != null) {
                                    "${topFaculty.name} - ${topFaculty.scheduledSlots} slots"
                                } else {
                                    "No scheduled classes yet"
                                },
                            )
                            if (facultyChartPairs.isNotEmpty()) {
                                Spacer(Modifier.height(AppSpacing.md))
                                AnalyticsBarList(
                                    title = "Top faculty by scheduled slots",
                                    entries = facultyChartPairs.take(5),
                                    barColor = MaterialTheme.colorScheme.primary,
                                    barTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                )
                            } else {
                                Text(
                                    "No faculty workload data yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        val roomPairs = remember(snap.roomUsage) {
                            snap.roomUsage.orEmpty().map { it.room to it.scheduledSlots }
                        }
                        AnalyticsGroupCard(
                            title = "Room utilization",
                            subtitle = "Most used rooms at a glance",
                            accent = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            TrendHighlightRow(
                                title = "Top assigned room",
                                detail = if (topRoom != null) {
                                    "${topRoom.room} - ${topRoom.scheduledSlots} classes"
                                } else {
                                    "No room assignments yet"
                                },
                            )
                            if (roomPairs.isNotEmpty()) {
                                Spacer(Modifier.height(AppSpacing.md))
                                AnalyticsBarList(
                                    title = "Top rooms by assignments",
                                    entries = roomPairs.take(5),
                                    barColor = MaterialTheme.colorScheme.tertiary,
                                    barTrackColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                )
                            } else {
                                Text(
                                    "No room usage yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        BatchSection(
                            batches = uiState.batches,
                            selected = uiState.selectedBatchName,
                            onSelect = { viewModel.onBatchSelected(it) },
                            server = snap,
                            batchAnalytics = uiState.batchScheduleAnalytics,
                            batchMissing = uiState.batchTimetableMissing,
                        )
                    }

                    else -> {
                        Text(
                            text = "Analytics will appear after the server responds.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerOverviewSection(
    summary: AnalyticsSummaryDto?,
    generatedAt: String?,
    topFacultyName: String,
    topFacultySlots: Int,
    topRoomName: String,
    topRoomSlots: Int,
) {
    val s = summary ?: AnalyticsSummaryDto()
    SectionHeader(
        title = "Overview",
        subtitle = "Quick campus snapshot",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        StatCard(
            title = "Saved timetables",
            value = s.timetableCount.toString(),
            accent = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Classes scheduled",
            value = s.totalClassesScheduled.toString(),
            accent = MaterialTheme.colorScheme.secondary,
            background = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        StatCard(
            title = "Free slots (all batches)",
            value = s.totalFreeSlots.toString(),
            accent = MaterialTheme.colorScheme.tertiary,
            background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Grid utilization",
            value = formatPercent01(s.overallUtilization),
            accent = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MiniInsightCard(
            label = "Top faculty",
            value = if (topFacultySlots > 0) "$topFacultyName · $topFacultySlots" else "No data yet",
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        MiniInsightCard(
            label = "Top room",
            value = if (topRoomSlots > 0) "$topRoomName · $topRoomSlots" else "No data yet",
            accent = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
    if (!generatedAt.isNullOrBlank()) {
        Text(
            text = "Snapshot: $generatedAt",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AnalyticsGroupCard(
    title: String,
    subtitle: String,
    accent: Color,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        elevation = analyticsCardElevation(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accent.copy(alpha = 0.85f)),
            )
            Column(Modifier.padding(AppSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.md))
            content()
            }
        }
    }
}

@Composable
private fun TrendHighlightRow(
    title: String,
    detail: String,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        elevation = analyticsCardElevation(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MiniInsightCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        elevation = analyticsCardElevation(),
    ) {
        Column(Modifier.padding(AppSpacing.md)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun UnmappedFacultyCard(snap: AnalyticsResponseDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        elevation = analyticsCardElevation(),
    ) {
        Column(Modifier.padding(AppSpacing.lg)) {
            Text(
                "Names in timetables not matched to Faculty records",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            snap.unmappedFacultyInSchedules?.forEach { u ->
                Text(
                    "• ${u.nameKey}: ${u.scheduledSlots} slots",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun BatchSection(
    batches: List<BatchDto>,
    selected: String?,
    onSelect: (String) -> Unit,
    server: AnalyticsResponseDto,
    batchAnalytics: ScheduleAnalytics?,
    batchMissing: Boolean,
) {
    Text(
        text = "Per batch",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth(),
    )
    if (batches.isEmpty()) {
        Text(
            "No batches defined yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        batches.forEach { b ->
            AppFilterChip(
                selected = b.name == selected,
                onClick = { onSelect(b.name) },
                label = { Text(b.name) },
            )
        }
    }
    val sel = selected ?: return
    val fromApi = server.byBatch.orEmpty().find { it.batch == sel }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = analyticsCardElevation(),
    ) {
        Column(Modifier.padding(AppSpacing.lg)) {
            Text(sel, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(AppSpacing.sm))
            if (fromApi != null) {
                Text(
                    "Scheduled ${fromApi.scheduledSlots} / ${fromApi.capacitySlots} slots · " +
                        "${fromApi.freeSlots} free",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                batchMissing -> {
                    Text(
                        "No saved timetable for this batch yet. Generate and save one to see slot-level breakdown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.sm),
                    )
                }
                batchAnalytics != null -> {
                    Spacer(Modifier.height(AppSpacing.md))
                    Text("This batch — slot breakdown", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(AppSpacing.sm))
                    BatchBreakdownCards(batchAnalytics)
                }
            }
        }
    }
}

@Composable
private fun BatchBreakdownCards(analytics: ScheduleAnalytics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        StatCard(
            title = "Scheduled (this batch)",
            value = analytics.scheduledClasses.toString(),
            accent = MaterialTheme.colorScheme.primary,
            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            title = "Free (this batch)",
            value = analytics.freeSlots.toString(),
            accent = MaterialTheme.colorScheme.tertiary,
            background = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f),
        )
    }
    Text(
        text = "Grid: ${analytics.totalSlots} slots (Mon–Fri × periods)",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        text = "Faculty & room below are for this batch only (from the saved grid).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = AppSpacing.sm),
    )
    analytics.facultyLoad.forEach { (name, n) ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                n.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(4.dp))
    }
    Spacer(Modifier.height(AppSpacing.sm))
    Text("Rooms (this batch)", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(AppSpacing.xs))
    if (analytics.roomUsage.isEmpty()) {
        Text(
            "No assignments",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        analytics.roomUsage.forEach { (room, n) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(room, style = MaterialTheme.typography.bodyMedium)
                Text(
                    n.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        elevation = analyticsCardElevation(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .fillMaxWidth(0.25f)
                    .background(accent, RoundedCornerShape(50)),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatPercent01(ratio: Double): String =
    "%.0f%%".format((ratio * 100).coerceIn(0.0, 100.0))
