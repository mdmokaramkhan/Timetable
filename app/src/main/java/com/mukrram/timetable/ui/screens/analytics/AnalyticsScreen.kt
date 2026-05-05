@file:OptIn(ExperimentalMaterial3Api::class)

package com.mukrram.timetable.ui.screens.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mukrram.timetable.data.remote.dto.AnalyticsGridDto
import com.mukrram.timetable.data.remote.dto.AnalyticsResponseDto
import com.mukrram.timetable.data.remote.dto.AnalyticsSummaryDto
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.analytics.ScheduleAnalytics
import com.mukrram.timetable.ui.components.AppFilterChip
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.AnalyticsViewModel

private fun formatGeneratedAt(raw: String?): String? {
    val s = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return s.replace('T', ' ').substringBefore('.').substringBefore('Z')
}

private fun percent01Label(ratio: Double): String =
    "${ratio.coerceIn(0.0, 1.0).times(100).toInt().coerceIn(0, 100)}%"

@Composable
private fun paneBorder(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

@Composable
fun AnalyticsScreen(
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
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                when {
                    uiState.batches.isEmpty() && uiState.loading && uiState.serverSnapshot == null -> {
                        CenteredLoading(message = "Loading…")
                    }

                    uiState.error != null && uiState.serverSnapshot == null && !uiState.loading -> {
                        ErrorState(
                            message = uiState.error ?: "Could not load analytics",
                            onRetry = { viewModel.retryAfterError() },
                        )
                    }

                    uiState.loading && uiState.serverSnapshot == null && uiState.error == null -> {
                        CenteredLoading(message = "Loading…")
                    }

                    uiState.serverSnapshot != null -> {
                        val snap = uiState.serverSnapshot!!
                        val summary = snap.summary ?: AnalyticsSummaryDto()
                        val grid = snap.grid ?: AnalyticsGridDto()

                        val facultyPairs =
                            snap.facultyWorkload
                                .orEmpty()
                                .filter { it.scheduledSlots > 0 && it.name.isNotBlank() }
                                .sortedByDescending { it.scheduledSlots }
                                .take(8)
                                .map { it.name to it.scheduledSlots }
                        val roomPairs =
                            snap.roomUsage.orEmpty()
                                .filter { it.scheduledSlots > 0 && it.room.isNotBlank() }
                                .take(8)
                                .map { it.room to it.scheduledSlots }

                        Text(
                            text = "Analytics",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        formatGeneratedAt(snap.generatedAt)?.let { t ->
                            Text(
                                text = "Updated $t",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        CampusSnapshotPanel(
                            summary = summary,
                            grid = grid,
                        )

                        if (!snap.unmappedFacultyInSchedules.isNullOrEmpty()) {
                            UnmappedFacultyStrip(snap)
                        }

                        Text(
                            text = "Faculty load",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "From all saved timetables vs faculty master list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (facultyPairs.isNotEmpty()) {
                            AnalyticsBarList(
                                title = "",
                                entries = facultyPairs,
                                maxItems = 8,
                                barColor = MaterialTheme.colorScheme.primary,
                                barTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            )
                        } else {
                            Text(
                                text = "No scheduled classes yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )

                        Text(
                            text = "Room usage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Share of class assignments across rooms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (roomPairs.isNotEmpty()) {
                            AnalyticsBarList(
                                title = "",
                                entries = roomPairs,
                                maxItems = 8,
                                barColor = MaterialTheme.colorScheme.tertiary,
                                barTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            )
                        } else {
                            Text(
                                text = "No room data yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )

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
                            text = "Waiting for data…",
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
private fun CampusSnapshotPanel(
    summary: AnalyticsSummaryDto,
    grid: AnalyticsGridDto,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = paneBorder(),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = "Campus snapshot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            val slotLine = when {
                grid.days > 0 && grid.slotsPerDay > 0 ->
                    "${grid.days} days × ${grid.slotsPerDay} slots = ${grid.slotsPerBatch} / batch"
                else -> "Grid dimensions from server"
            }
            Text(
                text = slotLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MetricBlock(
                    label = "Timetables",
                    value = summary.timetableCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricBlock(
                    label = "Classes",
                    value = summary.totalClassesScheduled.toString(),
                    modifier = Modifier.weight(1f),
                )
                MetricBlock(
                    label = "Free slots",
                    value = summary.totalFreeSlots.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            FractionBar(
                label = "Grid fill (all batches)",
                value = summary.overallUtilization.toFloat().coerceIn(0f, 1f),
                valueText = percent01Label(summary.overallUtilization),
                barColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun MetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UnmappedFacultyStrip(snap: AnalyticsResponseDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(AppSpacing.md)) {
            Text(
                text = "Unmatched names in grids",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(4.dp))
            snap.unmappedFacultyInSchedules?.take(6)?.forEach { u ->
                Text(
                    text = "• ${u.nameKey} · ${u.scheduledSlots} slots",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            val more = (snap.unmappedFacultyInSchedules?.size ?: 0) - 6
            if (more > 0) {
                Text(
                    text = "+ $more more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
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
        text = "Selected batch",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = "GET timetable/{batch} · slot breakdown from saved grid",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (batches.isEmpty()) {
        Text(
            text = "No batches yet.",
            style = MaterialTheme.typography.bodySmall,
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = paneBorder(),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = sel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (fromApi != null) {
                val fill =
                    if (fromApi.capacitySlots > 0) {
                        fromApi.scheduledSlots.toFloat() / fromApi.capacitySlots.toFloat()
                    } else {
                        0f
                    }
                Text(
                    text = "${fromApi.scheduledSlots} scheduled · ${fromApi.freeSlots} free · ${fromApi.capacitySlots} capacity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FractionBar(
                    label = "Batch grid fill",
                    value = fill.coerceIn(0f, 1f),
                    valueText = percent01Label(fill.toDouble()),
                    barColor = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                )
            }
            when {
                batchMissing -> {
                    Text(
                        text = "No saved timetable for this batch. Generate and save one to see faculty and room rows.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                batchAnalytics != null -> {
                    val a = batchAnalytics
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(
                        text = "This batch — top faculty",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val fPairs = a.facultyLoad.take(6)
                    if (fPairs.isNotEmpty()) {
                        AnalyticsBarList(
                            title = "",
                            entries = fPairs,
                            maxItems = 6,
                            barColor = MaterialTheme.colorScheme.primary,
                            barTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        )
                    } else {
                        Text(
                            text = "No faculty assignments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    Text(
                        text = "This batch — top rooms",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val rPairs = a.roomUsage.take(6)
                    if (rPairs.isNotEmpty()) {
                        AnalyticsBarList(
                            title = "",
                            entries = rPairs,
                            maxItems = 6,
                            barColor = MaterialTheme.colorScheme.tertiary,
                            barTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        )
                    } else {
                        Text(
                            text = "No room assignments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
