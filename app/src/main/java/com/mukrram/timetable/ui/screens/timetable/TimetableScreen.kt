package com.mukrram.timetable.ui.screens.timetable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.FeedbackTone
import com.mukrram.timetable.ui.components.InlineFeedbackCard
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.timetable.DefaultTimetableDays
import com.mukrram.timetable.ui.timetable.DefaultTimetableSlots
import com.mukrram.timetable.ui.timetable.cellAt
import com.mukrram.timetable.ui.timetable.computeConflictKeysOnly
import com.mukrram.timetable.ui.viewmodel.TimetableViewerTab
import com.mukrram.timetable.ui.viewmodel.TimetableViewerViewModel
import kotlin.math.abs

private data class CellEditTarget(
    val day: String,
    val slot: String,
    val cell: ScheduleCellDto?,
)

private val SubjectPaletteLight = listOf(
    Color(0xFFFFE8E0),
    Color(0xFFFFE3D9),
    Color(0xFFFFDDD1),
    Color(0xFFFFD8C9),
    Color(0xFFFFD2C2),
    Color(0xFFFFCCBA),
    Color(0xFFFFC7B2),
    Color(0xFFFFC1AB),
)

private val SubjectPaletteDark = listOf(
    Color(0xFF4A2416),
    Color(0xFF53291A),
    Color(0xFF5C2E1E),
    Color(0xFF653422),
    Color(0xFF6E3926),
    Color(0xFF773E2A),
    Color(0xFF80442E),
    Color(0xFF894932),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    modifier: Modifier = Modifier,
    onNavigateToSubstitution: () -> Unit = {},
    viewModel: TimetableViewerViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var batchMenuExpanded by remember { mutableStateOf(false) }
    var facultyMenuExpanded by remember { mutableStateOf(false) }
    var roomMenuExpanded by remember { mutableStateOf(false) }
    var showTimetableDialog by remember { mutableStateOf(false) }

    val sessionState by ServiceLocator.repository.sessionState.collectAsStateWithLifecycle(
        initialValue = SessionState.LoggedOut,
    )
    LaunchedEffect(sessionState) {
        val s = sessionState
        if (s is SessionState.LoggedIn && s.role == UserRole.Admin) {
            viewModel.loadMasterData()
        }
    }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    val displaySchedule = uiState.draftSchedule ?: uiState.timetable?.schedule.orEmpty()
    val conflictKeys = remember(displaySchedule) {
        computeConflictKeysOnly(displaySchedule)
    }

    var cellEdit by remember { mutableStateOf<CellEditTarget?>(null) }

    val tabIndex = when (uiState.tab) {
        TimetableViewerTab.ByBatch -> 0
        TimetableViewerTab.ByFaculty -> 1
        TimetableViewerTab.ByRoom -> 2
    }

    if (!uiState.isFacultyViewer) {
        cellEdit?.let { target ->
            CellEditDialog(
                target = target,
                subjects = uiState.subjects.map { it.name }.sorted(),
                faculties = uiState.faculties.map { it.name }.sorted(),
                rooms = uiState.rooms.map { it.name }.sorted(),
                loadingMaster = uiState.loadingMaster,
                onDismiss = { cellEdit = null },
                onSave = { subject, faculty, room ->
                    viewModel.updateCell(target.day, target.slot, subject, faculty, room)
                    cellEdit = null
                },
                onDelete = {
                    viewModel.clearCell(target.day, target.slot)
                    cellEdit = null
                },
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
//        topBar = {
//            TimetableTopAppBar(
//                titleText = "Timetable",
//                actions = {
//                    if (!uiState.isFacultyViewer) {
//                        IconButton(onClick = onNavigateToSubstitution) {
//                            Icon(Icons.Filled.SwapHoriz, contentDescription = "Substitution")
//                        }
//                    }
//                    if (!uiState.isFacultyViewer && uiState.draftSchedule != null) {
//                        IconButton(
//                            onClick = { viewModel.discardDraft() },
//                            enabled = !uiState.saving,
//                        ) {
//                            Icon(Icons.Filled.Undo, contentDescription = "Discard edits")
//                        }
//                        IconButton(
//                            onClick = { viewModel.saveDraft() },
//                            enabled = !uiState.saving && uiState.selectedBatchName != null,
//                        ) {
//                            Icon(Icons.Filled.Save, contentDescription = "Save timetable")
//                        }
//                    }
//                    IconButton(
//                        onClick = { viewModel.refreshTimetable() },
//                        enabled = when {
//                            uiState.loadingTimetable -> false
//                            uiState.isFacultyViewer -> true
//                            else -> uiState.selectedBatchName != null
//                        },
//                    ) {
//                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh timetable")
//                    }
//                },
//            )
//        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val batchNames = if (uiState.isFacultyViewer) {
            uiState.facultyBatchSlices.map { it.batch }
        } else {
            uiState.batches.map { it.name }
        }
        val showInitialLoading = uiState.loadingBatches && batchNames.isEmpty()
        AppPullToRefreshBox(
            isRefreshing = uiState.loadingTimetable && !showInitialLoading && batchNames.isNotEmpty(),
            onRefresh = { viewModel.refreshTimetable() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
            when {
                showInitialLoading -> {
                    CenteredLoading(
                        message = "Loading timetable data…",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    )
                }

                else -> {
                    Text(
                        text = "Academic Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = AppSpacing.sm),
                    )
                    Text(
                        text = "Viewing active semester curriculum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ExposedDropdownMenuBox(
                        expanded = batchMenuExpanded,
                        onExpandedChange = { batchMenuExpanded = !batchMenuExpanded },
                    ) {
                        AppOutlinedTextField(
                            value = uiState.selectedBatchName.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Batch") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppSpacing.xs)
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = batchMenuExpanded,
                            onDismissRequest = { batchMenuExpanded = false },
                        ) {
                            batchNames.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        viewModel.onBatchSelected(name)
                                        batchMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        TimetableTabPill(
                            text = "By batch",
                            selected = tabIndex == 0,
                            onClick = { viewModel.onTabSelected(TimetableViewerTab.ByBatch) },
                        )
                        TimetableTabPill(
                            text = "By faculty",
                            selected = tabIndex == 1,
                            onClick = { viewModel.onTabSelected(TimetableViewerTab.ByFaculty) },
                        )
                        TimetableTabPill(
                            text = "By room",
                            selected = tabIndex == 2,
                            onClick = { viewModel.onTabSelected(TimetableViewerTab.ByRoom) },
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Weekly grid",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }

                    val schedule = displaySchedule
                    val faculties = remember(schedule) {
                        schedule.values.flatten().map { it.faculty }.distinct().sorted()
                    }
                    val rooms = remember(schedule) {
                        schedule.values.flatten().map { it.room }.distinct().sorted()
                    }

                    when (uiState.tab) {
                        TimetableViewerTab.ByFaculty -> {
                            if (uiState.isFacultyViewer) {
                                AppOutlinedTextField(
                                    value = uiState.filterFaculty.orEmpty(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Faculty") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppSpacing.xs),
                                )
                            } else {
                                ExposedDropdownMenuBox(
                                    expanded = facultyMenuExpanded,
                                    onExpandedChange = { facultyMenuExpanded = !facultyMenuExpanded },
                                ) {
                                    AppOutlinedTextField(
                                        value = uiState.filterFaculty.orEmpty(),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Faculty") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyMenuExpanded)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = AppSpacing.xs)
                                            .menuAnchor(),
                                        placeholder = { Text("Select faculty") },
                                    )
                                    ExposedDropdownMenu(
                                        expanded = facultyMenuExpanded,
                                        onDismissRequest = { facultyMenuExpanded = false },
                                    ) {
                                        faculties.forEach { name ->
                                            DropdownMenuItem(
                                                text = { Text(name) },
                                                onClick = {
                                                    viewModel.onFacultyFilterSelected(name)
                                                    facultyMenuExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        TimetableViewerTab.ByRoom -> {
                            ExposedDropdownMenuBox(
                                expanded = roomMenuExpanded,
                                onExpandedChange = { roomMenuExpanded = !roomMenuExpanded },
                            ) {
                                AppOutlinedTextField(
                                    value = uiState.filterRoom.orEmpty(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Room") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AppSpacing.xs)
                                        .menuAnchor(),
                                    placeholder = { Text("Select room") },
                                )
                                ExposedDropdownMenu(
                                    expanded = roomMenuExpanded,
                                    onDismissRequest = { roomMenuExpanded = false },
                                ) {
                                    rooms.forEach { name ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.onRoomFilterSelected(name)
                                                roomMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        TimetableViewerTab.ByBatch -> { /* no extra filter */ }
                    }

                    if (uiState.loadingTimetable) {
                        CenteredLoading(
                            message = "Refreshing timetable…",
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                        )
                    } else if (!uiState.isFacultyViewer &&
                        uiState.tab == TimetableViewerTab.ByFaculty &&
                        uiState.filterFaculty == null
                    ) {
                        EmptyState(
                            title = "Pick a faculty",
                            message = "Select a faculty to focus the weekly view.",
                            tone = FeedbackTone.Info,
                            modifier = Modifier.padding(top = AppSpacing.xl),
                        )
                    } else if (!uiState.isFacultyViewer &&
                        uiState.tab == TimetableViewerTab.ByRoom &&
                        uiState.filterRoom == null
                    ) {
                        EmptyState(
                            title = "Pick a room",
                            message = "Select a room to view only its occupied slots.",
                            tone = FeedbackTone.Info,
                            modifier = Modifier.padding(top = AppSpacing.xl),
                        )
                    } else if (uiState.timetable != null || uiState.draftSchedule != null) {
                        if (uiState.draftSchedule != null) {
                            InlineFeedbackCard(
                                title = "Unsaved edits",
                                message = "Review your changes, then tap save when ready.",
                                tone = FeedbackTone.Success,
                            )
                        }
                        val days = schedule.keys.let { keys ->
                            DefaultTimetableDays.filter { keys.contains(it) }
                        }.takeIf { it.isNotEmpty() } ?: DefaultTimetableDays
                        val slots = DefaultTimetableSlots

                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.md),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(
                                    text = "Timetable overview",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = buildString {
                                        append("Mode: ")
                                        append(
                                            when (uiState.tab) {
                                                TimetableViewerTab.ByBatch -> "By batch"
                                                TimetableViewerTab.ByFaculty -> "By faculty"
                                                TimetableViewerTab.ByRoom -> "By room"
                                            },
                                        )
                                        uiState.selectedBatchName?.let { append(" • Batch: $it") }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Open Gridview Timetable to inspect slots in detail.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        AppButton(
                            onClick = { showTimetableDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Gridview Timetable")
                        }
                        TimetableInsightsCards(schedule = schedule)

                        if (showTimetableDialog) {
                            Dialog(onDismissRequest = { showTimetableDialog = false }) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(640.dp)
                                            .padding(AppSpacing.md),
                                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "Timetable",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            TextButton(onClick = { showTimetableDialog = false }) {
                                                Text("Close")
                                            }
                                        }
                                        TimetableGrid(
                                            days = days,
                                            slots = slots,
                                            schedule = schedule,
                                            tab = uiState.tab,
                                            filterFaculty = uiState.filterFaculty,
                                            filterRoom = uiState.filterRoom,
                                            conflictKeys = conflictKeys,
                                            readOnly = uiState.isFacultyViewer,
                                            onCellClick = { day, slot, cell ->
                                                if (!uiState.isFacultyViewer) {
                                                    cellEdit = CellEditTarget(day, slot, cell)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    } else if (uiState.isFacultyViewer && uiState.facultyBatchSlices.isEmpty() && !uiState.loadingTimetable) {
                        EmptyState(
                            title = "No classes scheduled",
                            message = "You have no classes in any batch yet. When admins assign you, they will appear here.",
                            icon = Icons.Outlined.CalendarMonth,
                            modifier = Modifier.padding(top = AppSpacing.xl),
                        )
                    } else if (
                        !uiState.isFacultyViewer &&
                        uiState.selectedBatchName != null &&
                        !uiState.loadingTimetable
                    ) {
                        EmptyState(
                            title = "No timetable loaded",
                            message = "Generate and save a timetable for this batch, or pull down to refresh.",
                            icon = Icons.Outlined.CalendarMonth,
                            modifier = Modifier.padding(top = AppSpacing.xl),
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun TimetableGrid(
    days: List<String>,
    slots: List<String>,
    schedule: Map<String, List<ScheduleCellDto>>,
    tab: TimetableViewerTab,
    filterFaculty: String?,
    filterRoom: String?,
    conflictKeys: Set<String>,
    readOnly: Boolean,
    onCellClick: (day: String, slot: String, cell: ScheduleCellDto?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hScroll = rememberScrollState()
    BoxWithConstraints(modifier = modifier.padding(top = AppSpacing.xs)) {
        val compact = maxWidth < 760.dp
        if (compact) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                days.forEach { day ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                            )
                            slots.forEach { slot ->
                                val cell = cellAt(schedule, day, slot)
                                val visible = when (tab) {
                                    TimetableViewerTab.ByBatch -> true
                                    TimetableViewerTab.ByFaculty ->
                                        cell != null && filterFaculty != null && cell.faculty == filterFaculty
                                    TimetableViewerTab.ByRoom ->
                                        cell != null && filterRoom != null && cell.room == filterRoom
                                }
                                val key = "$day|$slot"
                                val canEdit = !readOnly && (tab == TimetableViewerTab.ByBatch || visible)
                                val showConflict = conflictKeys.contains(key) && (tab == TimetableViewerTab.ByBatch || visible)
                                TimetableMobileSlotRow(
                                    slot = slot,
                                    cell = if (visible) cell else null,
                                    hasConflict = showConflict && cell != null,
                                    clickable = canEdit,
                                    onClick = { onCellClick(day, slot, cell) },
                                )
                            }
                        }
                    }
                }
            }
            return@BoxWithConstraints
        }
        val timeColumnWidth = if (compact) 76.dp else 92.dp
        val cellMinWidth = if (compact) 124.dp else 156.dp
        val cellHeight = if (compact) 116.dp else 132.dp

        AppCard(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScroll),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(timeColumnWidth)
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    days.forEach { day ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = cellMinWidth)
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (compact) day.take(3) else day,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                slots.forEachIndexed { index, slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = slot,
                            modifier = Modifier
                                .width(timeColumnWidth)
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        days.forEach { day ->
                            val cell = cellAt(schedule, day, slot)
                            val visible = when (tab) {
                                TimetableViewerTab.ByBatch -> true
                                TimetableViewerTab.ByFaculty ->
                                    cell != null && filterFaculty != null && cell.faculty == filterFaculty
                                TimetableViewerTab.ByRoom ->
                                    cell != null && filterRoom != null && cell.room == filterRoom
                            }
                            val key = "$day|$slot"
                            val canEdit = !readOnly && (tab == TimetableViewerTab.ByBatch || visible)
                            val showConflict = conflictKeys.contains(key) && (tab == TimetableViewerTab.ByBatch || visible)
                            TimetableCell(
                                cell = if (visible) cell else null,
                                hasConflict = showConflict && cell != null,
                                clickable = canEdit,
                                onClick = { onCellClick(day, slot, cell) },
                                cellHeight = cellHeight,
                                modifier = Modifier.widthIn(min = cellMinWidth),
                            )
                        }
                    }
                    if (index != slots.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableMobileSlotRow(
    slot: String,
    cell: ScheduleCellDto?,
    hasConflict: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = clickable, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        border = if (hasConflict) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = slot,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(46.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (cell == null) {
                    Text(
                        text = "No class",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = cell.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${cell.room} • ${cell.faculty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableTabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TimetableInsightsCards(schedule: Map<String, List<ScheduleCellDto>>) {
    val all = schedule.values.flatten()
    val totalHours = all.size
    val nextClass = all.firstOrNull()
    val roomCounts = all.groupingBy { it.room }.eachCount()
    val busiest = roomCounts.maxByOrNull { it.value }
    val occupancy = if (totalHours == 0 || busiest == null) 0 else ((busiest.value.toFloat() / totalHours.toFloat()) * 100).toInt()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.md),
    ) {
        val compact = maxWidth < 780.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(AppSpacing.md)) {
                        Text("Weekly Summary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$totalHours", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("Lecture slots", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Up next", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nextClass?.subject ?: "No upcoming class", style = MaterialTheme.typography.titleSmall)
                        Text(
                            nextClass?.let { "${it.room} • ${it.faculty}" } ?: "Generate or refresh timetable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                ) {
                    Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Room occupancy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(busiest?.key ?: "—", style = MaterialTheme.typography.titleSmall)
                        Text("$occupancy% utilization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(AppSpacing.md)) {
                        Text("Weekly Summary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$totalHours", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        Text("Lecture slots", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                AppCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Up next", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nextClass?.subject ?: "No upcoming class", style = MaterialTheme.typography.titleSmall)
                        Text(
                            nextClass?.let { "${it.room} • ${it.faculty}" } ?: "Generate or refresh timetable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AppCard(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                ) {
                    Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Room occupancy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(busiest?.key ?: "—", style = MaterialTheme.typography.titleSmall)
                        Text("$occupancy% utilization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableCell(
    cell: ScheduleCellDto?,
    hasConflict: Boolean,
    clickable: Boolean,
    onClick: () -> Unit,
    cellHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val bg = if (cell == null) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    } else {
        subjectColor(cell.subject)
    }
    Card(
        modifier = modifier
            .padding(4.dp)
            .height(cellHeight)
            .clickable(enabled = clickable, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (hasConflict) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
        },
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (cell == null) {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = cell.subject,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = cell.faculty,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = cell.room,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CellEditDialog(
    target: CellEditTarget,
    subjects: List<String>,
    faculties: List<String>,
    rooms: List<String>,
    loadingMaster: Boolean,
    onDismiss: () -> Unit,
    onSave: (subject: String, faculty: String, room: String) -> Unit,
    onDelete: () -> Unit,
) {
    var subject by remember(target) {
        mutableStateOf(target.cell?.subject.orEmpty())
    }
    var faculty by remember(target) {
        mutableStateOf(target.cell?.faculty.orEmpty())
    }
    var room by remember(target) {
        mutableStateOf(target.cell?.room.orEmpty())
    }
    var subjectMenu by remember { mutableStateOf(false) }
    var facultyMenu by remember { mutableStateOf(false) }
    var roomMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${target.day} · ${target.slot}") },
        text = {
            if (loadingMaster && subjects.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = subjectMenu,
                        onExpandedChange = { subjectMenu = !subjectMenu },
                    ) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = subjectMenu,
                            onDismissRequest = { subjectMenu = false },
                        ) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        subject = s
                                        subjectMenu = false
                                    },
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = facultyMenu,
                        onExpandedChange = { facultyMenu = !facultyMenu },
                    ) {
                        OutlinedTextField(
                            value = faculty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Faculty") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = facultyMenu,
                            onDismissRequest = { facultyMenu = false },
                        ) {
                            faculties.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f) },
                                    onClick = {
                                        faculty = f
                                        facultyMenu = false
                                    },
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = roomMenu,
                        onExpandedChange = { roomMenu = !roomMenu },
                    ) {
                        OutlinedTextField(
                            value = room,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Room") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = roomMenu,
                            onDismissRequest = { roomMenu = false },
                        ) {
                            rooms.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        room = r
                                        roomMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (subject.isNotBlank() && faculty.isNotBlank() && room.isNotBlank()) {
                        onSave(subject, faculty, room)
                    }
                },
                enabled = subject.isNotBlank() && faculty.isNotBlank() && room.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (target.cell != null) {
                    TextButton(onClick = onDelete) {
                        Text("Clear slot", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

@Composable
private fun subjectColor(subject: String): Color {
    val palette = if (isSystemInDarkTheme()) SubjectPaletteDark else SubjectPaletteLight
    val idx = abs(subject.hashCode()) % palette.size
    return palette[idx]
}
