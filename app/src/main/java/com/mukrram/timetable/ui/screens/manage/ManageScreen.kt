package com.mukrram.timetable.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.BatchManageViewModel
import com.mukrram.timetable.ui.viewmodel.FacultyManageViewModel
import com.mukrram.timetable.ui.viewmodel.RoomManageViewModel
import com.mukrram.timetable.ui.viewmodel.SubjectManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(modifier: Modifier = Modifier) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Faculty", "Subjects", "Rooms", "Batches")

    val facultyVm: FacultyManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val subjectVm: SubjectManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val roomVm: RoomManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)
    val batchVm: BatchManageViewModel = viewModel(factory = LocalAppViewModelFactory.current)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "The Academic Curator",
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Outlined.MenuOpen, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Manage Resources",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Keep faculty, subjects, rooms, and batches up to date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = tabIndex == index
                    Surface(
                        onClick = { tabIndex = index },
                        shape = CircleShape,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    }
                }
            }
            when (tabIndex) {
                0 -> FacultyTab(facultyVm)
                1 -> SubjectTab(subjectVm)
                2 -> RoomTab(roomVm)
                else -> BatchTab(batchVm)
            }
        }
    }
}

@Composable
private fun FacultyTab(vm: FacultyManageViewModel) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
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
                    .fillMaxSize()
                    .padding(top = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ManageSectionHeader(
                    title = "Faculty Management",
                    description = "Manage faculty profiles, subject expertise, and weekly teaching capacity.",
                    count = state.items.size,
                    icon = Icons.Outlined.Person,
                    accentColor = MaterialTheme.colorScheme.primary,
                )
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                    supportingInfo = buildString {
                                        val subs = item.subjects.orEmpty()
                                        append("Subjects: ${subs.size}")
                                        if (subs.isNotEmpty()) {
                                            append(" • ")
                                            append(subs.take(2).joinToString(", "))
                                            if (subs.size > 2) append(" +${subs.size - 2}")
                                        }
                                    },
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.Person,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    badge = "Faculty",
                                    extraTag = "ID ${item.id.takeLast(4)}",
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
private fun SubjectTab(vm: SubjectManageViewModel) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
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
                    .fillMaxSize()
                    .padding(top = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ManageSectionHeader(
                    title = "Subject Catalog",
                    description = "Define courses and lectures per week for better timetable accuracy.",
                    count = state.items.size,
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                )
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                    subtitle = "${item.lecturesPerWeek} lectures per week",
                                    supportingInfo = "Subject ID: ${item.id.takeLast(6)}",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                                    accentColor = MaterialTheme.colorScheme.tertiary,
                                    badge = "Subject",
                                    extraTag = "LPW ${item.lecturesPerWeek}",
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
private fun RoomTab(vm: RoomManageViewModel) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
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
                    .fillMaxSize()
                    .padding(top = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ManageSectionHeader(
                    title = "Room Inventory",
                    description = "Track room details and type so classes map to the right spaces.",
                    count = state.items.size,
                    icon = Icons.Outlined.MeetingRoom,
                    accentColor = MaterialTheme.colorScheme.secondary,
                )
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                    supportingInfo = "Room ID: ${item.id.takeLast(6)}",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.MeetingRoom,
                                    accentColor = MaterialTheme.colorScheme.secondary,
                                    badge = item.type,
                                    extraTag = "Room",
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
private fun BatchTab(vm: BatchManageViewModel) {
    val state by vm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
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
                    .fillMaxSize()
                    .padding(top = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                ManageSectionHeader(
                    title = "Batch Details",
                    description = "Maintain batches and departments before generating the timetable.",
                    count = state.items.size,
                    icon = Icons.Outlined.Groups,
                    accentColor = MaterialTheme.colorScheme.primary,
                )
                AppOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                    supportingInfo = "Batch ID: ${item.id.takeLast(6)}",
                                    onEdit = {
                                        editing = item
                                        showForm = true
                                    },
                                    onDelete = { deleteTarget = item },
                                    icon = Icons.Outlined.Groups,
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    badge = item.department,
                                    extraTag = "Batch",
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                    )
                }
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                text = extraTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = accentColor,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                supportingInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ManageSectionHeader(
    title: String,
    description: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
