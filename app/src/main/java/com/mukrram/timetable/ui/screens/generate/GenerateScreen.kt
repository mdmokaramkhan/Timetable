package com.mukrram.timetable.ui.screens.generate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.remote.dto.TimetableOptionDto
import com.mukrram.timetable.data.repository.DashboardCounts
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.ErrorState
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
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
    var fixedSlotsEnabled by remember { mutableStateOf(true) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "Generate Timetable",
                actions = {
                    IconButton(onClick = { viewModel.refreshSummary() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh data")
                    }
                },
            )
        },
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
                    .padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                when {
                    uiState.loadingSummary && uiState.counts == null -> {
                        CenteredLoading(message = "Loading data summary…")
                    }

                    uiState.error != null && uiState.counts == null && !uiState.loadingSummary -> {
                        ErrorState(
                            message = uiState.error ?: "Could not load data",
                            onRetry = { viewModel.refreshSummary() },
                        )
                    }

                    else -> {
                        val c = uiState.counts
                        if (c != null) {
                            HeroHeader()
                            SummaryAndAuditSection(
                                counts = c,
                                onUpdate = { viewModel.refreshSummary() },
                                isRefreshing = uiState.loadingSummary,
                            )
                        }
                        if (uiState.loadingSummary) {
                            CenteredLoading(
                                message = "Refreshing summary…",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = AppSpacing.xs),
                            )
                        }

                        ConstraintsCard(
                            selectedBatchName = uiState.selectedBatchName,
                            maxClassesPerDayText = uiState.maxClassesPerDayText,
                            batches = uiState.batches.map { it.name },
                            batchMenuExpanded = batchMenuExpanded,
                            onBatchMenuToggle = { batchMenuExpanded = !batchMenuExpanded },
                            onBatchMenuDismiss = { batchMenuExpanded = false },
                            onSelectBatch = {
                                viewModel.onBatchSelected(it)
                                batchMenuExpanded = false
                            },
                            onMaxClassesChange = viewModel::onMaxClassesPerDayChange,
                            loadingGenerate = uiState.loadingGenerate,
                            canGenerate = uiState.selectedBatchName != null,
                            onGenerate = viewModel::generate,
                            fixedSlotsEnabled = fixedSlotsEnabled,
                            onFixedSlotsChange = { fixedSlotsEnabled = it },
                        )

                        PrimaryGenerateSection(
                            loadingGenerate = uiState.loadingGenerate,
                            canGenerate = uiState.selectedBatchName != null,
                            onGenerate = viewModel::generate,
                        )

                        val result = uiState.generateResult
                        if (result != null) {
                            SectionHeader(
                                title = "Pick an option (${result.options.size})",
                                subtitle = "Choose one result and save it",
                                icon = Icons.Outlined.AutoAwesome,
                            )
                            if (result.options.isEmpty()) {
                                EmptyState(
                                    title = "No options generated",
                                    message = "Try another batch or loosen constraints, then generate again.",
                                )
                            } else {
                                result.options.forEach { opt ->
                                    TimetableOptionCard(
                                        option = opt,
                                        saving = uiState.savingOptionId == opt.id,
                                        onSelect = { viewModel.selectAndSaveOption(opt) },
                                    )
                                }
                            }
                        }
                        PreviewInsightsSection(options = uiState.generateResult?.options.orEmpty())
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = "Generate Timetable",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Choose a batch and constraints, then generate and save the best option.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryAndAuditSection(
    counts: DashboardCounts,
    onUpdate: () -> Unit,
    isRefreshing: Boolean,
) {
    val total = counts.subjectsCount + counts.facultyCount + counts.roomsCount + counts.batchesCount
    val balancedRooms = counts.roomsCount >= counts.subjectsCount
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(modifier = Modifier.padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Live Database",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Surface(
                        onClick = onUpdate,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRefreshing) "Updating" else "Update",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    SummaryStat("Subjects", counts.subjectsCount, Modifier.weight(1f))
                    SummaryStat("Faculty", counts.facultyCount, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    SummaryStat("Rooms", counts.roomsCount, Modifier.weight(1f))
                    SummaryStat("Batches", counts.batchesCount, Modifier.weight(1f))
                }
            }
        }
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Smart audit",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$total records ready",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = if (balancedRooms) "Room capacity looks healthy." else "Rooms are fewer than subjects.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
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
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ConstraintsCard(
    selectedBatchName: String?,
    maxClassesPerDayText: String,
    batches: List<String>,
    batchMenuExpanded: Boolean,
    onBatchMenuToggle: () -> Unit,
    onBatchMenuDismiss: () -> Unit,
    onSelectBatch: (String) -> Unit,
    onMaxClassesChange: (String) -> Unit,
    loadingGenerate: Boolean,
    canGenerate: Boolean,
    onGenerate: () -> Unit,
    fixedSlotsEnabled: Boolean,
    onFixedSlotsChange: (Boolean) -> Unit,
) {
    val sliderValue = maxClassesPerDayText.toFloatOrNull()?.coerceIn(1f, 10f) ?: 6f
    val slotText = maxClassesPerDayText.ifBlank { sliderValue.toInt().toString() }
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SectionHeader(
                title = "Optimization constraints",
                subtitle = "Select batch and optional class load limit",
                icon = Icons.Outlined.Tune,
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AcademicTermDropdown(
                    selectedBatchName = selectedBatchName,
                    batches = batches,
                    batchMenuExpanded = batchMenuExpanded,
                    onBatchMenuToggle = onBatchMenuToggle,
                    onBatchMenuDismiss = onBatchMenuDismiss,
                    onSelectBatch = onSelectBatch,
                )
                MaxClassesSlider(
                    sliderValue = sliderValue,
                    onMaxClassesChange = onMaxClassesChange,
                )
                SlotStrategyRow(
                    fixedSlotsEnabled = fixedSlotsEnabled,
                    onFixedSlotsChange = onFixedSlotsChange,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                ConstraintPill(
                    label = "Batch",
                    value = selectedBatchName ?: "Not selected",
                    modifier = Modifier.weight(1f),
                )
                ConstraintPill(
                    label = "Max/day",
                    value = slotText,
                    modifier = Modifier.weight(1f),
                )
            }

        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AcademicTermDropdown(
    selectedBatchName: String?,
    batches: List<String>,
    batchMenuExpanded: Boolean,
    onBatchMenuToggle: () -> Unit,
    onBatchMenuDismiss: () -> Unit,
    onSelectBatch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Academic term", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = { Text("Choose a batch") },
            )
            ExposedDropdownMenu(
                expanded = batchMenuExpanded,
                onDismissRequest = onBatchMenuDismiss,
            ) {
                if (batches.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No batches — add one in Manage") },
                        onClick = onBatchMenuDismiss,
                    )
                } else {
                    batches.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = { onSelectBatch(name) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaxClassesSlider(
    sliderValue: Float,
    onMaxClassesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Max classes/day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                Text(
                    text = sliderValue.toInt().toString().padStart(2, '0'),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Slider(
            value = sliderValue,
            onValueChange = { onMaxClassesChange(it.toInt().toString()) },
            valueRange = 1f..10f,
            steps = 8,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("01", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("05", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SlotStrategyRow(
    fixedSlotsEnabled: Boolean,
    onFixedSlotsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LockClock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Slot strategy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (fixedSlotsEnabled) "Fixed Time Slots" else "Flexible Slots",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = fixedSlotsEnabled,
                    onCheckedChange = onFixedSlotsChange,
                )
            }
        }
    }
}

@Composable
private fun PrimaryGenerateSection(
    loadingGenerate: Boolean,
    canGenerate: Boolean,
    onGenerate: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        AppButton(
            onClick = onGenerate,
            enabled = !loadingGenerate && canGenerate,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (loadingGenerate) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(22.dp)
                        .padding(end = AppSpacing.sm),
                    strokeWidth = 2.dp,
                )
            }
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Text("Generate timetable")
        }
        Text(
            text = "Estimated processing time: 4-6 seconds",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConstraintPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.sm)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PreviewInsightsSection(options: List<TimetableOptionDto>) {
    val firstOption = options.firstOrNull()
    val firstDay = firstOption?.schedule?.entries?.firstOrNull()
    val previewItems = firstDay?.value?.take(2).orEmpty()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = "Curator preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = firstDay?.key?.let { "Sample from $it" } ?: "Generate to see sample slots",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (previewItems.isEmpty()) {
                    Text(
                        text = "No slot preview available yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    previewItems.forEach { cell ->
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(Modifier.padding(AppSpacing.sm)) {
                                Text(cell.slot, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(cell.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("${cell.room} • ${cell.faculty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = "Workspace insight",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                val best = options.maxByOrNull { it.stats.placed }
                Text(
                    text = best?.let { "Best current option: ${it.id.uppercase()}" } ?: "No option yet",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = best?.let { "Places ${it.stats.placed}/${it.stats.required} lectures with current constraints." }
                        ?: "Generate options to inspect optimization insights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimetableOptionCard(
    option: TimetableOptionDto,
    saving: Boolean,
    onSelect: () -> Unit,
) {
    val stats = option.stats
    val load = stats.facultyLoad
    val loadSummary = load?.entries?.joinToString(", ") { "${it.key}: ${it.value}" }
        ?: "—"

    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.id.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = option.batch,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                ConstraintPill(
                    label = "Placed",
                    value = stats.placed.toString(),
                    modifier = Modifier.weight(1f),
                )
                ConstraintPill(
                    label = "Required",
                    value = stats.required.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = "Faculty load",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = loadSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AppButton(
                onClick = onSelect,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(22.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Select & save")
            }
        }
    }
}
