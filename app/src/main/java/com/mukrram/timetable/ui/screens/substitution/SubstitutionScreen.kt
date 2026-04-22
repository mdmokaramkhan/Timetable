package com.mukrram.timetable.ui.screens.substitution

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mukrram.timetable.data.remote.dto.AffectedSlotDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.FeedbackTone
import com.mukrram.timetable.ui.components.InlineFeedbackCard
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.SubstitutionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstitutionScreen(
    navController: NavController,
    viewModel: SubstitutionViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var batchMenuExpanded by remember { mutableStateOf(false) }
    var absentMenuExpanded by remember { mutableStateOf(false) }
    var replacementMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        val err = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        viewModel.clearError()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TimetableTopAppBar(
                titleText = "Substitution",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshLists() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh lists")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            uiState.loadingBatches && uiState.batches.isEmpty() -> {
                CenteredLoading(
                    message = "Loading batches and faculty…",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            else -> {
                AppPullToRefreshBox(
                    isRefreshing = (uiState.loadingBatches || uiState.loadingFaculty) &&
                        uiState.batches.isNotEmpty(),
                    onRefresh = { viewModel.refreshLists() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppSpacing.lg),
                    ) {
                    ExposedDropdownMenuBox(
                        expanded = batchMenuExpanded,
                        onExpandedChange = { batchMenuExpanded = !batchMenuExpanded },
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedBatch.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Batch") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = batchMenuExpanded,
                            onDismissRequest = { batchMenuExpanded = false },
                        ) {
                            uiState.batches.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.name) },
                                    onClick = {
                                        viewModel.onBatchSelected(b.name)
                                        batchMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = absentMenuExpanded,
                        onExpandedChange = { absentMenuExpanded = !absentMenuExpanded },
                    ) {
                        OutlinedTextField(
                            value = uiState.absentFaculty.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Absent faculty") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = absentMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .menuAnchor(),
                            placeholder = { Text("Select faculty") },
                        )
                        ExposedDropdownMenu(
                            expanded = absentMenuExpanded,
                            onDismissRequest = { absentMenuExpanded = false },
                        ) {
                            uiState.facultyList.forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f.name) },
                                    onClick = {
                                        viewModel.onAbsentFacultySelected(f.name)
                                        absentMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    if (uiState.loadingAffected) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                        )
                    } else if (uiState.absentFaculty != null) {
                        Text(
                            text = "Affected slots (${uiState.affectedSlots.size})",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                        if (uiState.affectedSlots.isEmpty() && !uiState.loadingAffected) {
                            EmptyState(
                                title = "No affected slots",
                                message = "This faculty has no scheduled classes in this batch right now.",
                                tone = FeedbackTone.Info,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                uiState.affectedSlots,
                                key = { s -> "${s.day}|${s.slot}|${s.faculty}|${s.subject}" },
                            ) { slot ->
                                AffectedSlotRow(
                                    slot = slot,
                                    selected = uiState.selectedSlot == slot,
                                    onClick = { viewModel.onAffectedSlotSelected(slot) },
                                )
                            }
                        }
                    }

                    if (uiState.loadingSuggestion) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(8.dp),
                        )
                    }

                    uiState.selectedSlot?.let { sel ->
                        Text(
                            text = "Replacement for ${sel.day} ${sel.slot}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Text(
                            text = "Suggested: ${uiState.suggestedReplacement ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        InlineFeedbackCard(
                            title = "Suggestion",
                            message = if (uiState.suggestedReplacement == null) {
                                "No automatic replacement yet. Pick one manually below."
                            } else {
                                "You can accept this suggestion or choose a different replacement."
                            },
                            tone = if (uiState.suggestedReplacement == null) {
                                FeedbackTone.Error
                            } else {
                                FeedbackTone.Success
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )

                        val replacementOptions =
                            (listOfNotNull(uiState.suggestedReplacement) + uiState.alternatives).distinct()

                        if (replacementOptions.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = replacementMenuExpanded,
                                onExpandedChange = { replacementMenuExpanded = !replacementMenuExpanded },
                            ) {
                                OutlinedTextField(
                                    value = uiState.replacementOverride.orEmpty(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Replacement faculty") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = replacementMenuExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                )
                                ExposedDropdownMenu(
                                    expanded = replacementMenuExpanded,
                                    onDismissRequest = { replacementMenuExpanded = false },
                                ) {
                                    replacementOptions.forEach { name ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.onReplacementOverrideSelected(name)
                                                replacementMenuExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.applySubstitution() },
                            enabled = !uiState.applying &&
                                (uiState.replacementOverride ?: uiState.suggestedReplacement) != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        ) {
                            if (uiState.applying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Apply substitution")
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun AffectedSlotRow(
    slot: AffectedSlotDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "${slot.day} · ${slot.slot}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = slot.subject ?: "—",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = slot.faculty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = slot.room,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
