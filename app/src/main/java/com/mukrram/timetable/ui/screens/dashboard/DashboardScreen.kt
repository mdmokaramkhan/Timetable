package com.mukrram.timetable.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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

                        DashboardOverviewSection(
                            counts = counts,
                            onOpenGenerate = onNavigateToGenerate,
                        )

                        Text(
                            text = "Resources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

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
private fun DashboardOverviewSection(
    counts: DashboardCounts,
    onOpenGenerate: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val total = counts.run { facultyCount + roomsCount + batchesCount + subjectsCount }
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = "Snapshot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onBackground,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            OverviewInfoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Catalog scale",
                headline = total.toString(),
                supporting = "Faculty, rooms, batches & subjects combined",
                icon = Icons.Outlined.Inventory2,
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surfaceContainerLow,
                    contentColor = scheme.onSurface,
                ),
                accent = scheme.primary,
                headlineColor = scheme.onSurface,
                dark = dark,
            )
            OverviewInfoCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Next step",
                headline = "Generate",
                supporting = if (total > 0) {
                    "Build a draft timetable from your dataset"
                } else {
                    "Add entities under Manage first"
                },
                icon = Icons.Filled.AutoAwesome,
                colors = CardDefaults.cardColors(
                    containerColor = scheme.tertiaryContainer,
                    contentColor = scheme.onTertiaryContainer,
                ),
                accent = scheme.tertiary,
                headlineColor = scheme.onTertiaryContainer,
                dark = dark,
                onClick = onOpenGenerate,
            )
        }
        AppCard(
            colors = CardDefaults.cardColors(
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
            ),
            border = BorderStroke(1.dp, scheme.primary.copy(alpha = if (dark) 0.28f else 0.18f)),
        ) {
            Row(
                modifier = Modifier.padding(AppSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Workspace tip",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Pull down to refresh counts. After a successful run, open Analytics for utilization and Export for reports.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onPrimaryContainer.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewInfoCard(
    title: String,
    headline: String,
    supporting: String,
    icon: ImageVector,
    colors: CardColors,
    accent: Color,
    headlineColor: Color,
    dark: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val borderColor = lerp(scheme.outlineVariant, accent, if (dark) 0.45f else 0.32f).copy(
        alpha = if (dark) 0.65f else 0.55f,
    )
    AppCard(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(accent.copy(alpha = if (dark) 0.22f else 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = headlineColor,
            )
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun MetricGrid(counts: DashboardCounts) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val base = scheme.surfaceContainerLow
    val subjectsAccent = lerp(scheme.primary, scheme.tertiary, 0.42f)
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ProfessionalMetricCard(
                label = "Faculty",
                value = counts.facultyCount.toString(),
                icon = Icons.Filled.Groups,
                accentColor = scheme.tertiary,
                baseContainer = base,
                dark = dark,
                modifier = Modifier.weight(1f),
            )
            ProfessionalMetricCard(
                label = "Rooms",
                value = counts.roomsCount.toString(),
                icon = Icons.Filled.MeetingRoom,
                accentColor = scheme.secondary,
                baseContainer = base,
                dark = dark,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ProfessionalMetricCard(
                label = "Batches",
                value = counts.batchesCount.toString(),
                icon = Icons.Filled.School,
                accentColor = scheme.primary,
                baseContainer = base,
                dark = dark,
                modifier = Modifier.weight(1f),
            )
            ProfessionalMetricCard(
                label = "Subjects",
                value = counts.subjectsCount.toString(),
                icon = Icons.Filled.Book,
                accentColor = subjectsAccent,
                baseContainer = base,
                dark = dark,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HeroPulseSection() {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            lerp(scheme.outlineVariant, scheme.primary, if (dark) 0.35f else 0.2f)
                .copy(alpha = if (dark) 0.55f else 0.45f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "System pulse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                )
                Text(
                    text = "Relative load mix across the last generation window",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val pulseValues = listOf(0.4f, 0.7f, 1f, 0.8f, 0.9f, 0.6f, 0.8f, 0.5f)
                val primaryColor = scheme.primary
                val primaryContainer = scheme.primaryContainer
                val surfaceVariant = scheme.surfaceVariant
                val secondaryContainer = scheme.secondaryContainer

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
                                    },
                                ),
                            ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "High efficiency",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary,
                )
                Icon(
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProfessionalMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    baseContainer: Color,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val fill = lerp(baseContainer, accentColor, if (dark) 0.28f else 0.14f)
    val borderColor = lerp(scheme.outlineVariant, accentColor, if (dark) 0.5f else 0.38f).copy(
        alpha = if (dark) 0.6f else 0.5f,
    )
    val iconBg = lerp(scheme.surfaceVariant, accentColor, if (dark) 0.42f else 0.28f)
    AppCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = fill,
            contentColor = scheme.onSurface,
        ),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = accentColor,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
private fun QuickActionGrid(
    onGenerate: () -> Unit,
    onManage: () -> Unit,
    onAnalytics: () -> Unit,
    onExport: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Text(
            text = "Workflow Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ModernActionButton(
                title = "Generate",
                subtitle = "Engine run",
                icon = Icons.Filled.AutoAwesome,
                containerColor = scheme.primaryContainer,
                titleColor = scheme.onPrimaryContainer,
                subtitleColor = scheme.primary,
                iconBackground = scheme.primary.copy(alpha = 0.16f),
                accentColor = scheme.primary,
                onClick = onGenerate,
                modifier = Modifier.weight(1f),
            )
            ModernActionButton(
                title = "Manage",
                subtitle = "Data vault",
                icon = Icons.AutoMirrored.Outlined.ViewList,
                containerColor = scheme.secondaryContainer,
                titleColor = scheme.onSecondaryContainer,
                subtitleColor = scheme.secondary,
                iconBackground = scheme.secondary.copy(alpha = 0.16f),
                accentColor = scheme.secondary,
                onClick = onManage,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ModernActionButton(
                title = "Analytics",
                subtitle = "Insights",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                containerColor = scheme.tertiaryContainer,
                titleColor = scheme.onTertiaryContainer,
                subtitleColor = scheme.tertiary,
                iconBackground = scheme.tertiary.copy(alpha = 0.16f),
                accentColor = scheme.tertiary,
                onClick = onAnalytics,
                modifier = Modifier.weight(1f),
            )
            ModernActionButton(
                title = "Export",
                subtitle = "Reports",
                icon = Icons.Filled.FileDownload,
                containerColor = scheme.surfaceContainerHigh,
                titleColor = scheme.onSurface,
                subtitleColor = scheme.onSurfaceVariant,
                iconBackground = scheme.onSurfaceVariant.copy(alpha = 0.14f),
                accentColor = scheme.outline,
                onClick = onExport,
                modifier = Modifier.weight(1f),
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
    titleColor: Color,
    subtitleColor: Color,
    iconBackground: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    AppCard(
        modifier = modifier.height(110.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = titleColor,
        ),
        border = BorderStroke(
            1.dp,
            lerp(MaterialTheme.colorScheme.outlineVariant, accentColor, if (dark) 0.4f else 0.28f)
                .copy(alpha = if (dark) 0.55f else 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                )
            }
        }
    }
}

@Composable
private fun ActivityFeedSection(
    summary: LastTimetableSummary?,
    onSeeAll: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "View Archive",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                )
            }
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(
                1.dp,
                scheme.outlineVariant.copy(alpha = if (dark) 0.5f else 0.42f),
            ),
        ) {
            summary?.let {
                FeedItem(
                    title = it.batch,
                    subtitle = "Automated Timetable Generation",
                    time = it.updatedAt ?: "Moments ago",
                    icon = Icons.Filled.History,
                    iconColor = scheme.secondary,
                    dark = dark,
                )
            } ?: run {
                FeedItem(
                    title = "System Standby",
                    subtitle = "No recent generations logged",
                    time = "",
                    icon = Icons.Filled.Refresh,
                    iconColor = scheme.onSurfaceVariant,
                    dark = dark,
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
    iconColor: Color,
    dark: Boolean,
) {
    Row(
        modifier = Modifier
            .padding(AppSpacing.lg)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = if (dark) 0.2f else 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
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
