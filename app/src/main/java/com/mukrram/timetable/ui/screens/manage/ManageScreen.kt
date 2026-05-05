package com.mukrram.timetable.ui.screens.manage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.remote.dto.BatchDto
import com.mukrram.timetable.data.remote.dto.FacultyDto
import com.mukrram.timetable.data.remote.dto.RoomDto
import com.mukrram.timetable.data.remote.dto.SubjectDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppCard
import com.mukrram.timetable.ui.components.AppFab
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.BatchManageViewModel
import com.mukrram.timetable.ui.viewmodel.FacultyManageViewModel
import com.mukrram.timetable.ui.viewmodel.RoomManageViewModel
import com.mukrram.timetable.ui.viewmodel.SubjectManageViewModel

private data class ManageTabStripItem(
    val label: String,
    val sectionTitle: String,
    val sectionDescription: String,
)

private val manageTabItems = listOf(
    ManageTabStripItem(
        label = "Faculty",
        sectionTitle = "Faculty Management",
        sectionDescription = "Manage faculty profiles, subject expertise, and weekly teaching capacity.",
    ),
    ManageTabStripItem(
        label = "Subjects",
        sectionTitle = "Subject Catalog",
        sectionDescription = "Define courses and lectures per week for better timetable accuracy.",
    ),
    ManageTabStripItem(
        label = "Rooms",
        sectionTitle = "Room Inventory",
        sectionDescription = "Track room details and type so classes map to the right spaces.",
    ),
    ManageTabStripItem(
        label = "Batches",
        sectionTitle = "Batch Details",
        sectionDescription = "Maintain batches and departments before generating the timetable.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    topBarSearchPulse: Int = 0,
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    val facultyVm: FacultyManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val subjectVm: SubjectManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val roomVm: RoomManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val batchVm: BatchManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)

    val facultyState by facultyVm.uiState.collectAsState()
    val subjectState by subjectVm.uiState.collectAsState()
    val roomState by roomVm.uiState.collectAsState()
    val batchState by batchVm.uiState.collectAsState()

    val tabMeta = manageTabItems[tabIndex]
    val sectionCount = when (tabIndex) {
        0 -> facultyState.items.size
        1 -> subjectState.items.size
        2 -> roomState.items.size
        else -> batchState.items.size
    }
    val sectionAccent = when (tabIndex) {
        0, 3 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp,
            ) {
                manageTabItems.forEachIndexed { index, item ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = {
                            Text(
                                text = item.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            )
            ManageSectionHeader(
                title = tabMeta.sectionTitle,
                description = tabMeta.sectionDescription,
                count = sectionCount,
                accentColor = sectionAccent,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (tabIndex) {
                    0 -> FacultyTab(facultyVm, topBarSearchPulse = topBarSearchPulse)
                    1 -> SubjectTab(subjectVm, topBarSearchPulse = topBarSearchPulse)
                    2 -> RoomTab(roomVm, topBarSearchPulse = topBarSearchPulse)
                    else -> BatchTab(batchVm, topBarSearchPulse = topBarSearchPulse)
                }
            }
        }
    }
}

@Composable
private fun FacultyTab(
    vm: FacultyManageViewModel,
    topBarSearchPulse: Int = 0,
) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(topBarSearchPulse) {
        if (topBarSearchPulse > 0) {
            searchFocusRequester.requestFocus()
        }
    }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<FacultyDto?>(null) }
    var deleteTarget by remember { mutableStateOf<FacultyDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        vm.clearError()
    }
    LaunchedEffect(state.pendingMessage) {
        state.pendingMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val filtered = remember(state.items, query) {
        state.items.filter { f ->
            f.name.contains(query, ignoreCase = true) ||
                (f.subjects?.any { it.contains(query, ignoreCase = true) } == true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = {
                editing = null
                showForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add faculty")
            }
        },
    ) { inner ->
        AppPullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = { vm.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    label = { Text("Search") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    singleLine = true,
                )
                when {
                    state.loading && state.items.isEmpty() -> {
                        CenteredLoading(message = "Loading faculty…")
                    }

                    filtered.isEmpty() && !state.loading -> {
                        EmptyState(
                            title = if (state.items.isEmpty()) "No faculty yet" else "No matches",
                            message = if (state.items.isEmpty()) {
                                "Add faculty with the + button. You can set subjects and weekly load."
                            } else {
                                "Try a different search term."
                            },
                            icon = Icons.Outlined.Person,
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            items(filtered, key = { it.id }) { item ->
                                EntityCard(
                                    title = item.name,
                                    subtitle = "Max load: ${item.maxLoad} lectures/week",
                                    supportingInfo = item.subjects?.joinToString(", ") ?: "No subjects assigned",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.Person,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    badge = "Faculty",
                                    extraTag = "ID: ${item.id.takeLast(6).uppercase()}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        FacultyFormDialog(
            initial = editing,
            onDismiss = {
                showForm = false
                editing = null
            },
            onSave = { name, subjectsCsv, maxLoad ->
                if (editing == null) {
                    vm.create(name, subjectsCsv, maxLoad)
                } else {
                    vm.update(editing!!.id, name, subjectsCsv, maxLoad)
                }
                showForm = false
                editing = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete faculty?") },
            text = {
                Text("You are about to remove \"${target.name}\" permanently. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.delete(target.id)
                        deleteTarget = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun FacultyFormDialog(
    initial: FacultyDto?,
    onDismiss: () -> Unit,
    onSave: (name: String, subjectsCsv: String, maxLoad: Int) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var subjectsCsv by remember(initial?.id) {
        mutableStateOf(initial?.subjects?.joinToString(", ").orEmpty())
    }
    var maxLoadText by remember(initial?.id) {
        mutableStateOf(initial?.maxLoad?.toString().orEmpty())
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add faculty" else "Edit faculty") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Fill in faculty details to keep assignments and load balancing accurate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppOutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                AppOutlinedTextField(
                    value = subjectsCsv,
                    onValueChange = { subjectsCsv = it },
                    label = { Text("Subjects (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                AppOutlinedTextField(
                    value = maxLoadText,
                    onValueChange = {
                        maxLoadText = it.filter { ch -> ch.isDigit() }
                        loadError = null
                    },
                    label = { Text("Max load") },
                    isError = loadError != null,
                    supportingText = {
                        loadError?.let { Text(it) }
                            ?: Text("Required — weekly teaching load cap")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var ok = true
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        ok = false
                    }
                    val load = maxLoadText.toIntOrNull()
                    if (load == null) {
                        loadError = "Enter a valid number"
                        ok = false
                    }
                    if (ok && load != null) onSave(name.trim(), subjectsCsv, load)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SubjectTab(
    vm: SubjectManageViewModel,
    topBarSearchPulse: Int = 0,
) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(topBarSearchPulse) {
        if (topBarSearchPulse > 0) {
            searchFocusRequester.requestFocus()
        }
    }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SubjectDto?>(null) }
    var deleteTarget by remember { mutableStateOf<SubjectDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        vm.clearError()
    }
    LaunchedEffect(state.pendingMessage) {
        state.pendingMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val filtered = remember(state.items, query) {
        state.items.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = {
                editing = null
                showForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add subject")
            }
        },
    ) { inner ->
        AppPullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = { vm.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                )
                when {
                    state.loading && state.items.isEmpty() -> {
                        CenteredLoading(message = "Loading subjects…")
                    }

                    filtered.isEmpty() && !state.loading -> {
                        EmptyState(
                            title = if (state.items.isEmpty()) "No subjects yet" else "No matches",
                            message = if (state.items.isEmpty()) {
                                "Add subjects with the + button. Set lectures per week for each."
                            } else {
                                "Try a different search term."
                            },
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            items(filtered, key = { it.id }) { item ->
                                EntityCard(
                                    title = item.name,
                                    subtitle = "${item.lecturesPerWeek} lectures / week",
                                    supportingInfo = "Standard weekly engagement required for this course across all assigned batches.",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    badge = "Subject",
                                    extraTag = "ID: ${item.id.takeLast(6).uppercase()}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        SubjectFormDialog(
            initial = editing,
            onDismiss = { showForm = false; editing = null },
            onSave = { name, lpw ->
                if (editing == null) vm.create(name, lpw) else vm.update(editing!!.id, name, lpw)
                showForm = false
                editing = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete subject?") },
            text = {
                Text("Delete subject \"${target.name}\" from the catalog? Related schedules may need adjustment.")
            },
            confirmButton = {
                TextButton(onClick = { vm.delete(target.id); deleteTarget = null }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SubjectFormDialog(
    initial: SubjectDto?,
    onDismiss: () -> Unit,
    onSave: (name: String, lecturesPerWeek: Int) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var lpwText by remember(initial?.id) {
        mutableStateOf(initial?.lecturesPerWeek?.toString().orEmpty())
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var lpwError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add subject" else "Edit subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Set a clear subject name and required weekly lectures.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppOutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                AppOutlinedTextField(
                    value = lpwText,
                    onValueChange = {
                        lpwText = it.filter { ch -> ch.isDigit() }
                        lpwError = null
                    },
                    label = { Text("Lectures per week") },
                    isError = lpwError != null,
                    supportingText = { lpwError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var ok = true
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        ok = false
                    }
                    val lpw = lpwText.toIntOrNull()
                    if (lpw == null) {
                        lpwError = "Enter a valid number"
                        ok = false
                    }
                    if (ok && lpw != null) onSave(name.trim(), lpw)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun RoomTab(
    vm: RoomManageViewModel,
    topBarSearchPulse: Int = 0,
) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(topBarSearchPulse) {
        if (topBarSearchPulse > 0) {
            searchFocusRequester.requestFocus()
        }
    }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RoomDto?>(null) }
    var deleteTarget by remember { mutableStateOf<RoomDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        vm.clearError()
    }
    LaunchedEffect(state.pendingMessage) {
        state.pendingMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val filtered = remember(state.items, query) {
        state.items.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.type.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = {
                editing = null
                showForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add room")
            }
        },
    ) { inner ->
        AppPullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = { vm.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                )
                when {
                    state.loading && state.items.isEmpty() -> {
                        CenteredLoading(message = "Loading rooms…")
                    }

                    filtered.isEmpty() && !state.loading -> {
                        EmptyState(
                            title = if (state.items.isEmpty()) "No rooms yet" else "No matches",
                            message = if (state.items.isEmpty()) {
                                "Add rooms with the + button (e.g. lecture halls, labs)."
                            } else {
                                "Try a different search term."
                            },
                            icon = Icons.Outlined.MeetingRoom,
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            items(filtered, key = { it.id }) { item ->
                                EntityCard(
                                    title = item.name,
                                    subtitle = item.type,
                                    supportingInfo = "Designated physical space for ${item.type.lowercase()} sessions and academic activities.",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.MeetingRoom,
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    badge = item.type,
                                    extraTag = "ID: ${item.id.takeLast(6).uppercase()}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        RoomFormDialog(
            initial = editing,
            onDismiss = { showForm = false; editing = null },
            onSave = { name, type ->
                if (editing == null) vm.create(name, type) else vm.update(editing!!.id, name, type)
                showForm = false
                editing = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete room?") },
            text = {
                Text("Delete room \"${target.name}\"? Sessions assigned to this room may be affected.")
            },
            confirmButton = {
                TextButton(onClick = { vm.delete(target.id); deleteTarget = null }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun RoomFormDialog(
    initial: RoomDto?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var typeText by remember(initial?.id) { mutableStateOf(initial?.type.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add room" else "Edit room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Provide room details so classes can be matched to the correct space type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppOutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                AppOutlinedTextField(
                    value = typeText,
                    onValueChange = { typeText = it; typeError = null },
                    label = { Text("Type") },
                    isError = typeError != null,
                    supportingText = {
                        typeError?.let { Text(it) }
                            ?: Text("e.g. Lecture, Lab, Tutorial")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var ok = true
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        ok = false
                    }
                    if (typeText.isBlank()) {
                        typeError = "Type is required"
                        ok = false
                    }
                    if (ok) onSave(name.trim(), typeText.trim())
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun BatchTab(
    vm: BatchManageViewModel,
    topBarSearchPulse: Int = 0,
) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(topBarSearchPulse) {
        if (topBarSearchPulse > 0) {
            searchFocusRequester.requestFocus()
        }
    }
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BatchDto?>(null) }
    var deleteTarget by remember { mutableStateOf<BatchDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val err = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        vm.clearError()
    }
    LaunchedEffect(state.pendingMessage) {
        state.pendingMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val filtered = remember(state.items, query) {
        state.items.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AppFab(onClick = {
                editing = null
                showForm = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add batch")
            }
        },
    ) { inner ->
        AppPullToRefreshBox(
            isRefreshing = state.loading && state.items.isNotEmpty(),
            onRefresh = { vm.load() },
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocusRequester),
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                )
                when {
                    state.loading && state.items.isEmpty() -> {
                        CenteredLoading(message = "Loading batches…")
                    }

                    filtered.isEmpty() && !state.loading -> {
                        EmptyState(
                            title = if (state.items.isEmpty()) "No batches yet" else "No matches",
                            message = if (state.items.isEmpty()) {
                                "Add batches with the + button before generating a timetable."
                            } else {
                                "Try a different search term."
                            },
                            icon = Icons.Outlined.Groups,
                        )
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            items(filtered, key = { it.id }) { item ->
                                EntityCard(
                                    title = item.name,
                                    subtitle = item.department,
                                    supportingInfo = "Official academic cohort belonging to the ${item.department} department.",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.Groups,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    badge = item.department,
                                    extraTag = "ID: ${item.id.takeLast(6).uppercase()}",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        BatchFormDialog(
            initial = editing,
            onDismiss = { showForm = false; editing = null },
            onSave = { name, dept ->
                if (editing == null) vm.create(name, dept) else vm.update(editing!!.id, name, dept)
                showForm = false
                editing = null
            },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete batch?") },
            text = {
                Text("Delete batch \"${target.name}\"? This can impact generated timetable structure.")
            },
            confirmButton = {
                TextButton(onClick = { vm.delete(target.id); deleteTarget = null }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun BatchFormDialog(
    initial: BatchDto?,
    onDismiss: () -> Unit,
    onSave: (name: String, department: String) -> Unit,
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var dept by remember(initial?.id) { mutableStateOf(initial?.department.orEmpty()) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var deptError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add batch" else "Edit batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Keep batch and department information complete for reliable generation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppOutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Name") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                AppOutlinedTextField(
                    value = dept,
                    onValueChange = { dept = it; deptError = null },
                    label = { Text("Department") },
                    isError = deptError != null,
                    supportingText = { deptError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var ok = true
                    if (name.isBlank()) {
                        nameError = "Name is required"
                        ok = false
                    }
                    if (dept.isBlank()) {
                        deptError = "Department is required"
                        ok = false
                    }
                    if (ok) onSave(name.trim(), dept.trim())
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun EntityCard(
    title: String,
    subtitle: String,
    supportingInfo: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    extraTag: String,
) {
    var expanded by remember { mutableStateOf(false) }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(0.5.dp, MaterialTheme.shapes.large),
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.2).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        val displayBadge = if (badge.length > 15) badge.take(13) + "..." else badge
                        Text(
                            text = displayBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.md)
                        .padding(start = 56.dp),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    if (extraTag.isNotBlank()) {
                        EntityInfoRow(label = "Reference", value = extraTag)
                    }
                    if (supportingInfo.isNotBlank()) {
                        val label = when (badge) {
                            "Faculty" -> "Expertise"
                            "Subject" -> "Standard Policy"
                            else -> "Description"
                        }
                        EntityInfoRow(label = label, value = supportingInfo)
                    }
                }
            }
        }
    }
}

@Composable
private fun EntityInfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ManageSectionHeader(
    title: String,
    description: String,
    count: Int,
    accentColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.4).sp
            )
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 16.sp
        )
    }
}

//@Previews of every component

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ManageScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.mukrram.timetable.ui.theme.TimetableTheme(darkTheme = false) {
        ManageScreen(navController = navController)
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun ManageScreenDarkPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.mukrram.timetable.ui.theme.TimetableTheme(darkTheme = true) {
        ManageScreen(navController = navController)
    }
}

@Preview(showBackground = true, name = "Faculty Card Light")
@Composable
fun FacultyCardPreview() {
    com.mukrram.timetable.ui.theme.TimetableTheme(darkTheme = false) {
        EntityCard(
            title = "Dr. Sharma",
            subtitle = "Max load: 12 lectures/week",
            supportingInfo = "Subjects: DSA, OS, DBMS",
            onEdit = {},
            onDelete = {},
            icon = Icons.Outlined.Person,
            accentColor = MaterialTheme.colorScheme.primary,
            badge = "Faculty",
            extraTag = "ID 1234"
        )
    }
}

@Preview(showBackground = true, name = "Faculty Card Dark")
@Composable
fun FacultyCardDarkPreview() {
    com.mukrram.timetable.ui.theme.TimetableTheme(darkTheme = true) {
        EntityCard(
            title = "Dr. Sharma",
            subtitle = "Max load: 12 lectures/week",
            supportingInfo = "Subjects: DSA, OS, DBMS",
            onEdit = {},
            onDelete = {},
            icon = Icons.Outlined.Person,
            accentColor = MaterialTheme.colorScheme.primary,
            badge = "Faculty",
            extraTag = "ID 1234"
        )
    }
}

@Preview(showBackground = true, name = "Subject Card")
@Composable
fun SubjectCardPreview() {
    com.mukrram.timetable.ui.theme.TimetableTheme {
        EntityCard(
            title = "Data Structures",
            subtitle = "4 lectures per week",
            supportingInfo = "Subject ID: SUB123",
            onEdit = {},
            onDelete = {},
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            accentColor = MaterialTheme.colorScheme.tertiary,
            badge = "Subject",
            extraTag = "LPW 4"
        )
    }
}

@Preview(showBackground = true, name = "Room Card")
@Composable
fun RoomCardPreview() {
    com.mukrram.timetable.ui.theme.TimetableTheme {
        EntityCard(
            title = "Lab 204",
            subtitle = "Computer Lab",
            supportingInfo = "Room ID: RM204",
            onEdit = {},
            onDelete = {},
            icon = Icons.Outlined.MeetingRoom,
            accentColor = MaterialTheme.colorScheme.secondary,
            badge = "Lab",
            extraTag = "Room"
        )
    }
}

@Preview(showBackground = true, name = "Batch Card")
@Composable
fun BatchCardPreview() {
    com.mukrram.timetable.ui.theme.TimetableTheme {
        EntityCard(
        title = "CSE 3rd Year",
        subtitle = "Computer Science",
        supportingInfo = "Batch ID: B12345",
        onEdit = {},
        onDelete = {},
        icon = Icons.Outlined.Groups,
        accentColor = MaterialTheme.colorScheme.primary,
        badge = "CSE",
        extraTag = "Batch"
    )
}}