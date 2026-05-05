@file:OptIn(ExperimentalMaterial3Api::class)

package com.mukrram.timetable.ui.screens.substitution

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mukrram.timetable.data.remote.dto.AffectedSlotDto
import com.mukrram.timetable.ui.LocalAppViewModelFactory
import com.mukrram.timetable.ui.components.AppFilterChip
import com.mukrram.timetable.ui.components.AppOutlinedTextField
import com.mukrram.timetable.ui.components.AppPullToRefreshBox
import com.mukrram.timetable.ui.components.CenteredLoading
import com.mukrram.timetable.ui.components.EmptyState
import com.mukrram.timetable.ui.components.FeedbackTone
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.viewmodel.SubstitutionUiState
import com.mukrram.timetable.ui.viewmodel.SubstitutionViewModel

@Composable
private fun paneBorder(): BorderStroke =
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

private fun formatUnavailableReason(raw: String?): String = when (raw) {
    "busy_at_slot_globally" -> "Busy in another batch at this time"
    "max_load_reached_in_batch" -> "Would exceed max load in this batch"
    else -> raw?.replace('_', ' ')?.trim()?.takeIf { it.isNotEmpty() } ?: "Not available"
}

@Composable
fun SubstitutionScreen(
    viewModel: SubstitutionViewModel = viewModel(factory = LocalAppViewModelFactory.current),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = AppSpacing.lg,
                            vertical = AppSpacing.md,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                Text(
                                    text = "Substitution",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                                Text(
                                    text = "Swap an absent teacher in a saved batch timetable.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        item {
                            ContextPanel(
                                uiState = uiState,
                                absentMenuExpanded = absentMenuExpanded,
                                onAbsentMenuExpandedChange = { absentMenuExpanded = it },
                                onBatchSelected = { viewModel.onBatchSelected(it) },
                                onAbsentFacultySelected = { viewModel.onAbsentFacultySelected(it) },
                            )
                        }

                        if (uiState.batches.isEmpty() && !uiState.loadingBatches) {
                            item {
                                EmptyState(
                                    title = "No batches",
                                    message = "Create a batch and save a timetable before substituting.",
                                    tone = FeedbackTone.Info,
                                )
                            }
                        }

                        if (uiState.absentFaculty != null) {
                            item {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = AppSpacing.sm),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Classes to cover (${uiState.affectedSlots.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (uiState.loadingAffected) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }

                            if (!uiState.loadingAffected && uiState.affectedSlots.isEmpty()) {
                                item {
                                    EmptyState(
                                        title = "Nothing scheduled",
                                        message = "That faculty has no classes in this batch.",
                                        tone = FeedbackTone.Info,
                                    )
                                }
                            }

                            items(
                                items = uiState.affectedSlots,
                                key = { s -> "${s.day}|${s.slot}|${s.faculty}|${s.subject}" },
                            ) { slot ->
                                AffectedSlotRow(
                                    slot = slot,
                                    selected = uiState.selectedSlot == slot,
                                    onClick = { viewModel.onAffectedSlotSelected(slot) },
                                )
                            }
                        }

                        if (uiState.selectedSlot != null) {
                            item {
                                ReplacementSection(
                                    uiState = uiState,
                                    replacementMenuExpanded = replacementMenuExpanded,
                                    onReplacementMenuExpandedChange = { replacementMenuExpanded = it },
                                    onReplacementSelected = {
                                        viewModel.onReplacementOverrideSelected(it)
                                        replacementMenuExpanded = false
                                    },
                                    onApply = { viewModel.applySubstitution() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextPanel(
    uiState: SubstitutionUiState,
    absentMenuExpanded: Boolean,
    onAbsentMenuExpandedChange: (Boolean) -> Unit,
    onBatchSelected: (String) -> Unit,
    onAbsentFacultySelected: (String?) -> Unit,
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
                text = "Batch",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.batches.isEmpty()) {
                Text(
                    text = "Pull to refresh or add batches on the server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    uiState.batches.forEach { b ->
                        AppFilterChip(
                            selected = b.name == uiState.selectedBatch,
                            onClick = { onBatchSelected(b.name) },
                            label = { Text(b.name) },
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            )

            Text(
                text = "Absent faculty",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ExposedDropdownMenuBox(
                expanded = absentMenuExpanded,
                onExpandedChange = onAbsentMenuExpandedChange,
            ) {
                AppOutlinedTextField(
                    value = uiState.absentFaculty.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Who is away") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = absentMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Select name") },
                    enabled = uiState.selectedBatch != null && uiState.facultyList.isNotEmpty(),
                )
                ExposedDropdownMenu(
                    expanded = absentMenuExpanded,
                    onDismissRequest = { onAbsentMenuExpandedChange(false) },
                ) {
                    uiState.facultyList.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f.name) },
                            onClick = {
                                onAbsentFacultySelected(f.name)
                                onAbsentMenuExpandedChange(false)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplacementSection(
    uiState: SubstitutionUiState,
    replacementMenuExpanded: Boolean,
    onReplacementMenuExpandedChange: (Boolean) -> Unit,
    onReplacementSelected: (String) -> Unit,
    onApply: () -> Unit,
) {
    val sel = uiState.selectedSlot ?: return
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
                text = "Replace ${sel.day} · ${sel.slot}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = sel.subject?.takeIf { it.isNotBlank() } ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (uiState.loadingSuggestion) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = "Finding replacements…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val suggested = uiState.suggestedReplacement
                Text(
                    text = if (suggested != null) {
                        "Suggested: $suggested"
                    } else {
                        "No automatic match. Pick someone who teaches this subject and is free."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (suggested != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            if (uiState.unavailable.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Others not eligible (${uiState.unavailable.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    uiState.unavailable.take(4).forEach { u ->
                        Text(
                            text = "• ${u.name} — ${formatUnavailableReason(u.reason)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val more = uiState.unavailable.size - 4
                    if (more > 0) {
                        Text(
                            text = "+ $more more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            val replacementOptions =
                (listOfNotNull(uiState.suggestedReplacement) + uiState.alternatives).distinct()

            if (replacementOptions.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = replacementMenuExpanded,
                    onExpandedChange = onReplacementMenuExpandedChange,
                ) {
                    AppOutlinedTextField(
                        value = uiState.replacementOverride.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Replacement faculty") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = replacementMenuExpanded,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = replacementMenuExpanded,
                        onDismissRequest = { onReplacementMenuExpandedChange(false) },
                    ) {
                        replacementOptions.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { onReplacementSelected(name) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(AppSpacing.xs))

            Button(
                onClick = onApply,
                enabled = !uiState.applying &&
                    !uiState.loadingSuggestion &&
                    (uiState.replacementOverride ?: uiState.suggestedReplacement) != null,
                modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun AffectedSlotRow(
    slot: AffectedSlotDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        paneBorder()
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${slot.day} · ${slot.slot}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = slot.subject?.takeIf { it.isNotBlank() } ?: "—",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text(
                    text = slot.faculty,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = slot.room.ifBlank { "—" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
