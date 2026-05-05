package com.mukrram.timetable.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onNavigateToGenerate: () -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToExport: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                when {
                    uiState.loading && uiState.counts == null -> {
                        CenteredLoading(message = "Synchronizing data...")
                    }

                    uiState.error != null && uiState.counts == null && !uiState.loading -> {
                        ErrorState(message = uiState.error, onRetry = onRefresh)
                    }

                    uiState.counts != null -> {
                        val counts = uiState.counts

                        DashboardHeroCard()

                        DashboardSummaryRow(
                            counts = counts,
                            onOpenGenerate = onNavigateToGenerate,
                        )

                        DashboardSectionHeader(title = "Resource catalog")
                        DashboardMetricGrid(counts = counts)

                        DashboardSectionHeader(title = "Catalog mix")
                        DashboardCatalogMixCard(counts = counts)

                        DashboardSectionHeader(title = "Workflow")
                        DashboardQuickActionGrid(
                            onGenerate = onNavigateToGenerate,
                            onManage = onNavigateToManage,
                            onAnalytics = onNavigateToAnalytics,
                            onExport = onNavigateToExport,
                        )

//                        DashboardWorkflowTipsCard()

                        DashboardSectionHeader(title = "Recent timetable")
                        DashboardRecentActivityCard(
                            summary = uiState.lastTimetableSummary,
                            onOpenTimetable = onNavigateToTimetable,
                        )

                        if (uiState.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = AppSpacing.md),
                                strokeWidth = 3.dp,
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
private fun DashboardSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun DashboardHeroCard() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        color = scheme.surfaceContainerLow,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            scheme.primaryContainer.copy(alpha = 0.55f),
                            scheme.surfaceContainerLow,
                        ),
                    ),
                )
                .padding(AppSpacing.xl),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = "Hello, Curator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp,
                    color = scheme.onSurface,
                )
                Text(
                    text = "Your scheduling workspace — pull down anytime to sync counts with the server.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = scheme.primary.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = "Live overview",
                        modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummaryRow(
    counts: DashboardCounts,
    onOpenGenerate: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val total = counts.run {
        facultyCount + roomsCount + batchesCount + subjectsCount
    }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DashboardHighlightCard(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 148.dp),
                title = "Entities on file",
                headline = total.toString(),
                supporting = "${counts.facultyCount} faculty · ${counts.roomsCount} rooms · ${counts.batchesCount} batches · ${counts.subjectsCount} subjects",
                icon = Icons.Filled.BarChart,
                containerColor = scheme.surfaceContainerLow,
                accentColor = scheme.primary,
                onClick = null,
            )
            DashboardHighlightCard(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 148.dp),
                title = "Next step",
                headline = if (total > 0) "Generate" else "Set up",
                supporting = if (total > 0) {
                    "Run the engine on your current catalog"
                } else {
                    "Add rows in Manage before generating"
                },
                icon = Icons.Filled.AutoAwesome,
                containerColor = scheme.tertiaryContainer,
                accentColor = scheme.tertiary,
                headlineColor = scheme.onTertiaryContainer,
                supportingColor = scheme.onTertiaryContainer.copy(alpha = 0.55f),
                onClick = onOpenGenerate,
            )
        }
        Spacer(Modifier.height(10.dp))
        AppCard(
            colors = CardDefaults.cardColors(
                containerColor = scheme.primaryContainer.copy(alpha = 0.25f),
                contentColor = scheme.onPrimaryContainer,
            ),
            border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.2f)),
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Workspace tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "After a successful generation, open Analytics for load and utilization, or Export for CSV and text reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.92f),
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHighlightCard(
    title: String,
    headline: String,
    supporting: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier,
    headlineColor: Color = MaterialTheme.colorScheme.onSurface,
    supportingColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null,
) {
    val outline = lerp(
        MaterialTheme.colorScheme.outlineVariant,
        accentColor,
        0.35f,
    ).copy(alpha = 0.45f)
    AppCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, outline),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = supportingColor.copy(alpha = 0.95f),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = headlineColor,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = supportingColor.copy(alpha = 0.9f),
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardMetricGrid(counts: DashboardCounts) {
    val scheme = MaterialTheme.colorScheme
    val base = scheme.surfaceContainerLowest
    val subjectsAccent = lerp(scheme.primary, scheme.tertiary, 0.42f)
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DashboardMetricTile(
                label = "Faculty",
                value = counts.facultyCount.toString(),
                caption = "Teaching staff",
                icon = Icons.Filled.Groups,
                accentColor = scheme.tertiary,
                baseContainer = base,
                modifier = Modifier.weight(1f),
            )
            DashboardMetricTile(
                label = "Rooms",
                value = counts.roomsCount.toString(),
                caption = "Spaces",
                icon = Icons.Filled.MeetingRoom,
                accentColor = scheme.secondary,
                baseContainer = base,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DashboardMetricTile(
                label = "Batches",
                value = counts.batchesCount.toString(),
                caption = "Cohorts",
                icon = Icons.Filled.School,
                accentColor = scheme.primary,
                baseContainer = base,
                modifier = Modifier.weight(1f),
            )
            DashboardMetricTile(
                label = "Subjects",
                value = counts.subjectsCount.toString(),
                caption = "Courses",
                icon = Icons.Filled.Book,
                accentColor = subjectsAccent,
                baseContainer = base,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardMetricTile(
    label: String,
    value: String,
    caption: String,
    icon: ImageVector,
    accentColor: Color,
    baseContainer: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val fill = lerp(baseContainer, accentColor, 0.12f)
    val borderColor = lerp(scheme.outlineVariant, accentColor, 0.4f).copy(alpha = 0.42f)
    AppCard(
        modifier = modifier.heightIn(min = 118.dp),
        colors = CardDefaults.cardColors(
            containerColor = fill,
            contentColor = scheme.onSurface,
        ),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                        tint = accentColor,
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.onSurface,
                letterSpacing = (-0.6).sp,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

private data class MixSegment(
    val label: String,
    val count: Int,
    val color: Color,
)

@Composable
private fun DashboardCatalogMixCard(counts: DashboardCounts) {
    val scheme = MaterialTheme.colorScheme
    val segments = listOf(
        MixSegment("Faculty", counts.facultyCount, scheme.tertiary),
        MixSegment("Rooms", counts.roomsCount, scheme.secondary),
        MixSegment("Batches", counts.batchesCount, scheme.primary),
        MixSegment("Subjects", counts.subjectsCount, lerp(scheme.primary, scheme.tertiary, 0.45f)),
    )
    val total = segments.sumOf { it.count }
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            scheme.outlineVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = scheme.primary,
                )
                Column {
                    Text(
                        text = "Share by record count",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = "How your catalog is distributed — useful before tightening constraints in Generate.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }
            if (total == 0) {
                Text(
                    text = "No records yet. Use Manage to add faculty, rooms, batches, and subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    segments.forEach { seg ->
                        val w = seg.count.toFloat().coerceAtLeast(0f)
                        if (w > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(w)
                                    .fillMaxSize()
                                    .background(seg.color.copy(alpha = 0.85f)),
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    segments.forEach { seg ->
                        val pct = if (total > 0) (seg.count * 100f / total) else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(seg.color),
                                )
                                Text(
                                    text = seg.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = scheme.onSurface,
                                )
                            }
                            Text(
                                text = "${seg.count} · ${"%.0f".format(pct)}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardQuickActionGrid(
    onGenerate: () -> Unit,
    onManage: () -> Unit,
    onAnalytics: () -> Unit,
    onExport: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DashboardActionCard(
                title = "Generate",
                subtitle = "Draft timetable",
                icon = Icons.Filled.AutoAwesome,
                containerColor = scheme.primaryContainer,
                titleColor = scheme.onPrimaryContainer,
                subtitleColor = scheme.primary,
                iconTint = scheme.primary,
                onClick = onGenerate,
                modifier = Modifier.weight(1f),
            )
            DashboardActionCard(
                title = "Manage",
                subtitle = "Catalog & data",
                icon = Icons.AutoMirrored.Outlined.ViewList,
                containerColor = scheme.secondaryContainer,
                titleColor = scheme.onSecondaryContainer,
                subtitleColor = scheme.secondary,
                iconTint = scheme.secondary,
                onClick = onManage,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DashboardActionCard(
                title = "Analytics",
                subtitle = "Load & usage",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                containerColor = scheme.tertiaryContainer,
                titleColor = scheme.onTertiaryContainer,
                subtitleColor = scheme.tertiary,
                iconTint = scheme.tertiary,
                onClick = onAnalytics,
                modifier = Modifier.weight(1f),
            )
            DashboardActionCard(
                title = "Export",
                subtitle = "CSV & reports",
                icon = Icons.Filled.FileDownload,
                containerColor = scheme.surfaceContainerHigh,
                titleColor = scheme.onSurface,
                subtitleColor = scheme.onSurfaceVariant,
                iconTint = scheme.outline,
                onClick = onExport,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline = lerp(
        MaterialTheme.colorScheme.outlineVariant,
        iconTint,
        0.28f,
    ).copy(alpha = 0.4f)
    AppCard(
        modifier = modifier.heightIn(min = 112.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = titleColor,
        ),
        border = BorderStroke(1.dp, outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = subtitleColor,
                )
            }
        }
    }
}

@Composable
private fun DashboardWorkflowTipsCard() {
    val scheme = MaterialTheme.colorScheme
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Filled.TipsAndUpdates,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "Good habits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
            }
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))
            DashboardTipLine(
                icon = Icons.Filled.Refresh,
                iconTint = scheme.secondary,
                title = "Stay current",
                body = "Pull down on this screen to refresh counts after changes in Manage or on the server.",
            )
            DashboardTipLine(
                icon = Icons.AutoMirrored.Outlined.ViewList,
                iconTint = scheme.tertiary,
                title = "Complete the catalog",
                body = "Accurate faculty loads, rooms, batches, and subjects produce better draft timetables.",
            )
            DashboardTipLine(
                icon = Icons.Filled.AutoAwesome,
                iconTint = scheme.primary,
                title = "Generate, then review",
                body = "Run Generate when ready, then inspect the Timetable tab and Analytics for conflicts or overload.",
            )
        }
    }
}

@Composable
private fun DashboardTipLine(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun DashboardRecentActivityCard(
    summary: LastTimetableSummary?,
    onOpenTimetable: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLowest),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.38f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Last saved snapshot",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                    Text(
                        text = "Opens the Timetable tab for the full grid",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onOpenTimetable) {
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))
            summary?.let {
                DashboardFeedItem(
                    title = it.batch,
                    subtitle = "Saved timetable",
                    time = it.updatedAt?.takeIf { t -> t.isNotBlank() } ?: "Recently",
                    icon = Icons.Filled.History,
                    accent = scheme.primary,
                )
            } ?: run {
                DashboardFeedItem(
                    title = "No snapshot yet",
                    subtitle = "Generate and save a timetable to see it here",
                    time = "",
                    icon = Icons.Filled.Refresh,
                    accent = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DashboardFeedItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    accent: Color,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        if (time.isNotBlank()) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
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
                    batchesCount = 8,
                ),
                loading = false,
                lastTimetableSummary = LastTimetableSummary(
                    batch = "Fall Semester 2024",
                    updatedAt = "2:23 PM",
                ),
            ),
            onRefresh = {},
            onClearError = {},
            onNavigateToGenerate = {},
            onNavigateToManage = {},
            onNavigateToTimetable = {},
            onNavigateToAnalytics = {},
            onNavigateToExport = {},
            modifier = Modifier,
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
                    batchesCount = 8,
                ),
                loading = false,
                lastTimetableSummary = LastTimetableSummary(
                    batch = "Fall Semester 2024",
                    updatedAt = "2:23 PM",
                ),
            ),
            onRefresh = {},
            onClearError = {},
            onNavigateToGenerate = {},
            onNavigateToManage = {},
            onNavigateToTimetable = {},
            onNavigateToAnalytics = {},
            onNavigateToExport = {},
            modifier = Modifier,
        )
    }
}
