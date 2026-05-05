package com.mukrram.timetable.ui.screens.generate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.data.remote.dto.TimetableOptionDto
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppOutlinedButton
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.timetable.ReadOnlyTimetableGrid
import com.mukrram.timetable.ui.viewmodel.GenerateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: GenerateViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var batchMenuExpanded by remember { mutableStateOf(false) }
    var timetablePreviewDialog by remember { mutableStateOf<GenerateTimetablePreviewDialogData?>(null) }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    LaunchedEffect(uiState.saveSuccessMessage) {
        val msg = uiState.saveSuccessMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = "View",
        )
        if (result == SnackbarResult.ActionPerformed) {
            navController.navigate(MainDestination.Timetable.route) {
                popUpTo(MainDestination.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        viewModel.consumeSaveMessage()
    }

    timetablePreviewDialog?.let { dialogData ->
        GenerateTimetablePreviewDialog(
            data = dialogData,
            onDismiss = { timetablePreviewDialog = null },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        AppPullToRefreshBox(
            isRefreshing = uiState.loadingSummary && uiState.counts != null,
            onRefresh = { viewModel.refreshSummary() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                when {
                    uiState.loadingSummary && uiState.counts == null -> {
                        CenteredLoading(message = "Loading workspace…")
                    }

                    uiState.error != null && uiState.counts == null && !uiState.loadingSummary -> {
                        ErrorState(
                            message = uiState.error ?: "Could not load data",
                            onRetry = { viewModel.refreshSummary() },
                        )
                    }

                    else -> {
                        val gridDayCount = uiState.generateResult?.days?.size ?: 5
                        val gridSlotCount = uiState.generateResult?.slots?.size ?: 8

                        GenerateHeroSection(
                            onRefresh = { viewModel.refreshSummary() },
                            isRefreshing = uiState.loadingSummary,
                        )

                        uiState.counts?.let { counts ->
                            WorkspaceStatsGrid(
                                counts = counts,
                                onRefresh = { viewModel.refreshSummary() },
                                isRefreshing = uiState.loadingSummary,
                            )
                            ReadinessHintCard(counts = counts)
                        }

                        if (uiState.loadingSummary && uiState.counts != null) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                            )
                        }

                        SchedulingSetupCard(
                            selectedBatchName = uiState.selectedBatchName,
                            batches = uiState.batches.map { it.name },
                            batchMenuExpanded = batchMenuExpanded,
                            onBatchMenuToggle = { batchMenuExpanded = !batchMenuExpanded },
                            onBatchMenuDismiss = { batchMenuExpanded = false },
                            onSelectBatch = {
                                viewModel.onBatchSelected(it)
                                batchMenuExpanded = false
                            },
                            maxClassesPerDayText = uiState.maxClassesPerDayText,
                            onMaxClassesChange = viewModel::onMaxClassesPerDayChange,
                            optionsCount = uiState.optionsCount,
                            onOptionsCountChange = viewModel::onOptionsCountChange,
                            gridDayCount = gridDayCount,
                            gridSlotCount = gridSlotCount,
                        )

                        GenerateCallToActionSection(
                            loading = uiState.loadingGenerate,
                            enabled = uiState.selectedBatchName != null,
                            optionCount = uiState.optionsCount,
                            onGenerate = viewModel::generate,
                        )

                        uiState.generateResult?.let { result ->
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            val bestOption =
                                result.options.maxByOrNull { it.stats.placed } ?: result.options.firstOrNull()
                            if (bestOption != null) {
                                BestOptionPreviewCard(
                                    option = bestOption,
                                    batchName = result.batch,
                                    saving = uiState.savingOptionId == bestOption.id,
                                    onSave = { viewModel.selectAndSaveOption(bestOption) },
                                    onPreview = {
                                        timetablePreviewDialog = GenerateTimetablePreviewDialogData(
                                            title = "Draft ${bestOption.id.uppercase()}",
                                            subtitle = "${result.batch} · ${bestOption.stats.placed}/${bestOption.stats.required} placed",
                                            days = result.days,
                                            slots = result.slots,
                                            schedule = bestOption.schedule,
                                        )
                                    },
                                )
                            }

                            Text(
                                text = "Choose a draft (${result.options.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (result.options.isEmpty()) {
                                EmptyState(
                                    title = "No schedules returned",
                                    message = "Relax the daily cap or fix data in Manage.",
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                    result.options.forEach { opt ->
                                        RichOptionCard(
                                            option = opt,
                                            saving = uiState.savingOptionId == opt.id,
                                            onSave = { viewModel.selectAndSaveOption(opt) },
                                            onPreview = {
                                                timetablePreviewDialog = GenerateTimetablePreviewDialogData(
                                                    title = "Draft ${opt.id.uppercase()}",
                                                    subtitle = "${result.batch} · placement ${opt.stats.placed}/${opt.stats.required}",
                                                    days = result.days,
                                                    slots = result.slots,
                                                    schedule = opt.schedule,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        WorkflowTipsCard()

                        Spacer(modifier = Modifier.height(AppSpacing.xxl))
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateHeroSection(
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Build timetables",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground,
                )
                Spacer(Modifier.width(5.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scheme.primaryContainer.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = "Scheduler",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = "Refresh, pick a batch, then generate and save a draft.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(24.dp)
                        .width(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh workspace")
            }
        }
    }
}

@Composable
private fun WorkspaceStatsGrid(
    counts: DashboardCounts,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Catalog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Surface(
                    onClick = onRefresh,
                    enabled = !isRefreshing,
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                ) {
                    Row(
                        Modifier.padding(horizontal = AppSpacing.md, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(18.dp).width(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.height(18.dp))
                        }
                        Text("Sync", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                StatTile("Subjects", counts.subjectsCount, Modifier.weight(1f))
                StatTile("Faculty", counts.facultyCount, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                StatTile("Rooms", counts.roomsCount, Modifier.weight(1f))
                StatTile("Batches", counts.batchesCount, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReadinessHintCard(counts: DashboardCounts) {
    val healthy = counts.roomsCount >= counts.subjectsCount && counts.facultyCount > 0
    val scheme = MaterialTheme.colorScheme
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (healthy) {
                scheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                scheme.tertiaryContainer.copy(alpha = 0.45f)
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (healthy) Icons.Outlined.Schedule else Icons.Outlined.Info,
                contentDescription = null,
                tint = if (healthy) scheme.primary else scheme.tertiary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (healthy) "Looks ready" else "Check your data",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Text(
                    text = when {
                        counts.facultyCount == 0 -> "Add faculty in Manage first."
                        !healthy -> "Fewer rooms than subjects — add rooms or expect failures."
                        else -> "Enough rooms and faculty for a typical run."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SchedulingSetupCard(
    selectedBatchName: String?,
    batches: List<String>,
    batchMenuExpanded: Boolean,
    onBatchMenuToggle: () -> Unit,
    onBatchMenuDismiss: () -> Unit,
    onSelectBatch: (String) -> Unit,
    maxClassesPerDayText: String,
    onMaxClassesChange: (String) -> Unit,
    optionsCount: Int,
    onOptionsCountChange: (Int) -> Unit,
    gridDayCount: Int,
    gridSlotCount: Int,
) {
    val sliderValue = maxClassesPerDayText.toFloatOrNull()?.coerceIn(1f, 10f) ?: 6f
    val scheme = MaterialTheme.colorScheme

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = null, tint = scheme.primary)
                Column {
                    Text(
                        text = "Constraints",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Batch, daily cap, variants · ${gridDayCount}×$gridSlotCount grid",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = batchMenuExpanded,
                onExpandedChange = { onBatchMenuToggle() },
            ) {
                AppOutlinedTextField(
                    value = selectedBatchName.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Batch") },
                    leadingIcon = {
                        Icon(Icons.Outlined.School, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Select batch") },
                )
                ExposedDropdownMenu(
                    expanded = batchMenuExpanded,
                    onDismissRequest = onBatchMenuDismiss,
                ) {
                    if (batches.isEmpty()) {
                        DropdownMenuItem(text = { Text("None — add in Manage") }, onClick = onBatchMenuDismiss)
                    } else {
                        batches.forEach { name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { onSelectBatch(name) })
                        }
                    }
                }
            }

            HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))

            Text(
                text = "Max / day",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { onMaxClassesChange(it.toInt().toString()) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scheme.primary.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = sliderValue.toInt().toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text("Variants", style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = optionsCount == 2,
                    onClick = { onOptionsCountChange(2) },
                    label = { Text("2") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = optionsCount == 3,
                    onClick = { onOptionsCountChange(3) },
                    label = { Text("3") },
                    modifier = Modifier.weight(1f),
                )
            }

        }
    }
}

@Composable
private fun GenerateCallToActionSection(
    loading: Boolean,
    enabled: Boolean,
    optionCount: Int,
    onGenerate: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
    ) {
        Column(
            Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = "Clash‑free drafts in seconds. Saving sends you to Timetable.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppButton(
                onClick = onGenerate,
                enabled = enabled && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp).width(20.dp).padding(end = AppSpacing.sm),
                        strokeWidth = 2.dp,
                    )
                }
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(AppSpacing.sm))
                Text("Generate ($optionCount)")
            }
        }
    }
}

@Composable
private fun BestOptionPreviewCard(
    option: TimetableOptionDto,
    batchName: String,
    saving: Boolean,
    onSave: () -> Unit,
    onPreview: () -> Unit,
) {
    val stats = option.stats
    val pct = remember(stats.placed, stats.required) {
        val r = stats.required.coerceAtLeast(1).toFloat()
        (stats.placed / r).coerceIn(0f, 1f)
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Strongest draft · ${option.id.uppercase()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        batchName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = "Coverage ${stats.placed} / ${stats.required}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    onClick = onSave,
                    enabled = !saving,
                    modifier = Modifier.weight(1f),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp).width(18.dp).padding(end = AppSpacing.sm),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("Save")
                }
                AppOutlinedButton(
                    onClick = onPreview,
                    enabled = !saving,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.GridView, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Preview")
                }
            }
        }
    }
}

@Composable
private fun RichOptionCard(
    option: TimetableOptionDto,
    saving: Boolean,
    onSave: () -> Unit,
    onPreview: () -> Unit,
) {
    val stats = option.stats
    val pct = remember(stats.placed, stats.required) {
        val r = stats.required.coerceAtLeast(1).toFloat()
        (stats.placed / r).coerceIn(0f, 1f)
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Column(
            Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    option.id.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        option.batch,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                "Coverage ${stats.placed} / ${stats.required}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppButton(
                    onClick = onSave,
                    enabled = !saving,
                    modifier = Modifier.weight(1f),
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp).width(18.dp).padding(end = AppSpacing.sm),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("Save")
                }
                AppOutlinedButton(
                    onClick = onPreview,
                    enabled = !saving,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.GridView, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Preview")
                }
            }
        }
    }
}

@Composable
private fun WorkflowTipsCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(
                Icons.Outlined.TipsAndUpdates,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Text(
                text = "Link subjects to faculty, keep rooms in sync with Manage, regenerate anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class GenerateTimetablePreviewDialogData(
    val title: String,
    val subtitle: String,
    val days: List<String>,
    val slots: List<String>,
    val schedule: Map<String, List<ScheduleCellDto>>,
)

@Composable
private fun GenerateTimetablePreviewDialog(
    data: GenerateTimetablePreviewDialogData,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        val screenH = LocalConfiguration.current.screenHeightDp.dp
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                Icons.Outlined.GridView,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            text = data.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                Spacer(Modifier.height(AppSpacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                Spacer(Modifier.height(AppSpacing.sm))
                ReadOnlyTimetableGrid(
                    days = data.days,
                    slots = data.slots,
                    schedule = data.schedule,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = screenH * 0.72f),
                )
            }
        }
    }
}

