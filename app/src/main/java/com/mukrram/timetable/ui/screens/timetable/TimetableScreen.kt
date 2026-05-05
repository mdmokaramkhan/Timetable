package com.mukrram.timetable.ui.screens.timetable

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppOutlinedButton
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.FeedbackTone
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.timetable.DefaultTimetableDays
import com.mukrram.timetable.ui.timetable.DefaultTimetableSlots
import com.mukrram.timetable.ui.timetable.ReadOnlyTimetableGrid
import com.mukrram.timetable.ui.timetable.computeConflictKeysOnly
import com.mukrram.timetable.ui.viewmodel.TimetableViewerTab
import com.mukrram.timetable.ui.viewmodel.TimetableViewerViewModel

private data class CellEditTarget(
    val day: String,
    val slot: String,
    val cell: ScheduleCellDto?,
)

private fun formatScheduleUpdatedAt(raw: String?): String? {
    val s = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return s.replace('T', ' ').substringBefore('.').substringBefore('Z')
}

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
            val pageScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(pageScroll)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
            when {
                showInitialLoading -> {
                    CenteredLoading(
                        message = "Loading timetable data…",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xl),
                    )
                }

                else -> {
                    val schedule = displaySchedule
                    val faculties = remember(schedule) {
                        schedule.values.flatten().map { it.faculty }.distinct().sorted()
                    }
                    val rooms = remember(schedule) {
                        schedule.values.flatten().map { it.room }.distinct().sorted()
                    }
                    val serverConflicts = uiState.timetable?.conflicts
                    val formattedUpdated =
                        remember(uiState.timetable?.updatedAt) {
                            formatScheduleUpdatedAt(uiState.timetable?.updatedAt)
                        }
                    val toolbarScroll = rememberScrollState()
                    val chipScroll = rememberScrollState()
                    val isCellDisplayed: (ScheduleCellDto?) -> Boolean = remember(
                        uiState.tab,
                        uiState.filterFaculty,
                        uiState.filterRoom,
                    ) {
                        { cell ->
                            when (uiState.tab) {
                                TimetableViewerTab.ByBatch -> true
                                TimetableViewerTab.ByFaculty ->
                                    cell != null &&
                                        uiState.filterFaculty != null &&
                                        cell.faculty == uiState.filterFaculty
                                TimetableViewerTab.ByRoom ->
                                    cell != null &&
                                        uiState.filterRoom != null &&
                                        cell.room == uiState.filterRoom
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Timetable",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            if (!formattedUpdated.isNullOrBlank()) {
                                Text(
                                    text = formattedUpdated,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Text(
                            text =
                                if (uiState.isFacultyViewer) {
                                    uiState.facultyDisplayName?.let { "Logged in · $it" }
                                        ?: "Faculty · read-only"
                                } else {
                                    "Tap a cell to edit · pull down to refresh"
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (!uiState.isFacultyViewer) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(toolbarScroll),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (uiState.draftSchedule != null) {
                                    TextButton(onClick = { viewModel.discardDraft() }) {
                                        Icon(
                                            Icons.Filled.Undo,
                                            contentDescription = null,
                                            modifier = Modifier.height(18.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Discard")
                                    }
                                    AppButton(
                                        onClick = { viewModel.saveDraft() },
                                        enabled = !uiState.saving && uiState.selectedBatchName != null,
                                        contentPadding = PaddingValues(
                                            horizontal = AppSpacing.sm,
                                            vertical = AppSpacing.xs,
                                        ),
                                    ) {
                                        if (uiState.saving) {
                                            CircularProgressIndicator(
                                                Modifier
                                                    .height(16.dp)
                                                    .width(16.dp),
                                                strokeWidth = 2.dp,
                                            )
                                            Spacer(Modifier.width(8.dp))
                                        } else {
                                            Icon(
                                                Icons.Filled.Save,
                                                contentDescription = null,
                                                modifier = Modifier.height(18.dp),
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text("Save")
                                    }
                                }
                            }
                        }

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
                                    .menuAnchor(),
                                placeholder = { Text("Cohort") },
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
                            modifier = Modifier.horizontalScroll(chipScroll),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            FilterChip(
                                selected = tabIndex == 0,
                                onClick = { viewModel.onTabSelected(TimetableViewerTab.ByBatch) },
                                label = { Text("Full grid") },
                            )
                            FilterChip(
                                selected = tabIndex == 1,
                                onClick = { viewModel.onTabSelected(TimetableViewerTab.ByFaculty) },
                                label = { Text("Faculty") },
                            )
                            FilterChip(
                                selected = tabIndex == 2,
                                onClick = { viewModel.onTabSelected(TimetableViewerTab.ByRoom) },
                                label = { Text("Room") },
                            )
                        }

                        when (uiState.tab) {
                            TimetableViewerTab.ByFaculty -> {
                                if (uiState.isFacultyViewer) {
                                    AppOutlinedTextField(
                                        value = uiState.filterFaculty.orEmpty(),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Faculty (you)") },
                                        modifier = Modifier.fillMaxWidth(),
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
                                                ExposedDropdownMenuDefaults.TrailingIcon(
                                                    expanded = facultyMenuExpanded,
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            placeholder = { Text("Select") },
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
                                            .menuAnchor(),
                                        placeholder = { Text("Select") },
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

                            TimetableViewerTab.ByBatch -> Unit
                        }

                        if (!serverConflicts.isNullOrEmpty()) {
                            Text(
                                text = "${serverConflicts.size} server validation conflict(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        if (uiState.loadingTimetable && (uiState.timetable != null || uiState.isFacultyViewer)) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        val daysPreview = schedule.keys
                            .let { keys ->
                                DefaultTimetableDays.filter { keys.contains(it) }
                            }
                            .takeIf { it.isNotEmpty() }
                            ?: DefaultTimetableDays
                        val slotsPreview = DefaultTimetableSlots

                        val onSlotTap =
                            if (uiState.isFacultyViewer) {
                                null
                            } else {
                                { day: String, slot: String, cell: ScheduleCellDto? ->
                                    cellEdit =
                                        CellEditTarget(day = day, slot = slot, cell = cell)
                                }
                            }

                        if (uiState.loadingTimetable && uiState.timetable == null && !uiState.isFacultyViewer) {
                            CenteredLoading(
                                message = "Syncing…",
                                modifier = Modifier.padding(vertical = AppSpacing.lg),
                            )
                        } else if (
                            uiState.loadingTimetable &&
                            uiState.isFacultyViewer &&
                            uiState.facultyBatchSlices.isEmpty()
                        ) {
                            CenteredLoading(
                                message = "Loading assignments…",
                                modifier = Modifier.padding(vertical = AppSpacing.lg),
                            )
                        } else if (
                            !uiState.isFacultyViewer &&
                            uiState.tab == TimetableViewerTab.ByFaculty &&
                            uiState.filterFaculty == null
                        ) {
                            EmptyState(
                                title = "Pick a faculty",
                                message = "Choose a faculty to filter the grid.",
                                tone = FeedbackTone.Info,
                            )
                        } else if (
                            !uiState.isFacultyViewer &&
                            uiState.tab == TimetableViewerTab.ByRoom &&
                            uiState.filterRoom == null
                        ) {
                            EmptyState(
                                title = "Pick a room",
                                message = "Choose a room to filter the grid.",
                                tone = FeedbackTone.Info,
                            )
                        } else if (uiState.timetable != null || uiState.draftSchedule != null) {
                            if (uiState.draftSchedule != null) {
                                Text(
                                    text = "Unsaved edits · save from toolbar",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            ReadOnlyTimetableGrid(
                                days = daysPreview,
                                slots = slotsPreview,
                                schedule = schedule,
                                modifier = Modifier.fillMaxWidth(),
                                useNestedVerticalScroll = false,
                                conflictingSlotKeys = conflictKeys,
                                isCellDisplayed = isCellDisplayed,
                                onSlotClick = onSlotTap,
                            )
                        } else if (uiState.isFacultyViewer && uiState.facultyBatchSlices.isEmpty() && !uiState.loadingTimetable) {
                            EmptyState(
                                title = "No classes scheduled",
                                message = "You have no assignments yet.",
                                icon = Icons.Outlined.CalendarMonth,
                            )
                        } else if (
                            !uiState.isFacultyViewer &&
                            uiState.selectedBatchName != null &&
                            !uiState.loadingTimetable
                        ) {
                            EmptyState(
                                title = "No timetable loaded",
                                message = "Generate and save one, then refresh.",
                                icon = Icons.Outlined.CalendarMonth,
                            )
                        }
                    }
                }
            }
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
