package com.mukrram.timetable.ui.timetable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mukrram.timetable.data.remote.dto.ScheduleCellDto
import com.mukrram.timetable.ui.theme.AppSpacing
import kotlin.math.abs

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

@Composable
private fun readOnlySubjectFill(subject: String): Color {
    val palette = if (isSystemInDarkTheme()) SubjectPaletteDark else SubjectPaletteLight
    val idx = abs(subject.hashCode()) % palette.size
    return palette[idx]
}

/**
 * Strict **slots × days** matrix: first column is slot labels, header row is day labels.
 * One shared horizontal scroll state keeps columns aligned across all rows (m×n cross grid).
 * Cell content shows **subject only**.
 *
 * @param useNestedVerticalScroll When true (default), slot rows scroll inside the grid.
 * Use false when this composable sits inside an outer vertically scrolling screen.
 */
@Composable
fun ReadOnlyTimetableGrid(
    days: List<String>,
    slots: List<String>,
    schedule: Map<String, List<ScheduleCellDto>>,
    modifier: Modifier = Modifier,
    useNestedVerticalScroll: Boolean = true,
    /** Highlight keys formatted as `"$day|$slot"` for clashes in the backing schedule. */
    conflictingSlotKeys: Set<String> = emptySet(),
    /** When false for a slot, cell shows placeholder (masked lens). Usually leave default. */
    isCellDisplayed: (ScheduleCellDto?) -> Boolean = { true },
    onSlotClick: ((day: String, slot: String, cell: ScheduleCellDto?) -> Unit)? = null,
    /** Width of row header column (slot labels). */
    rowHeaderWidth: Dp = 40.dp,
    /** Uniform width of each day column. */
    dayColumnWidth: Dp = 100.dp,
    /** Row height for each slot line. */
    slotRowHeight: Dp = 64.dp,
    headerHeight: Dp = 40.dp,
) {
    val hScroll = rememberScrollState()
    val vScrollState = rememberScrollState()
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, outline),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hScroll),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GridHeaderCorner(
                    width = rowHeaderWidth,
                    height = headerHeight,
                    borderColor = outline,
                )
                days.forEach { day ->
                    GridHeaderDay(
                        label = day,
                        width = dayColumnWidth,
                        height = headerHeight,
                        borderColor = outline,
                    )
                }
            }
            HorizontalDivider(thickness = 1.dp, color = outline.copy(alpha = 0.85f))

            val rowColumnModifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (useNestedVerticalScroll) {
                            Modifier.verticalScroll(vScrollState)
                        } else {
                            Modifier
                        },
                    )

            Column(
                modifier = rowColumnModifier,
            ) {
                slots.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(hScroll),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GridRowHeaderSlot(
                            label = slot,
                            width = rowHeaderWidth,
                            height = slotRowHeight,
                            borderColor = outline,
                        )
                        days.forEach { day ->
                            val raw = cellAt(schedule, day, slot)
                            val displayed = if (isCellDisplayed(raw)) raw else null
                            val key = "$day|$slot"
                            GridMatrixCell(
                                subject = displayed?.subject,
                                width = dayColumnWidth,
                                height = slotRowHeight,
                                borderColor = outline,
                                fill = displayed?.subject?.let { readOnlySubjectFill(it) },
                                showConflictOutline =
                                    conflictingSlotKeys.contains(key) &&
                                    displayed?.subject.orEmpty().isNotEmpty(),
                                onClick =
                                    if (onSlotClick != null) {
                                        ({ onSlotClick(day, slot, raw) })
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridHeaderCorner(
    width: Dp,
    height: Dp,
    borderColor: Color,
) {
    Surface(
        modifier = Modifier
            .width(width)
            .height(height),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GridHeaderDay(
    label: String,
    width: Dp,
    height: Dp,
    borderColor: Color,
) {
    Surface(
        modifier = Modifier
            .width(width)
            .height(height),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = AppSpacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label.take(3),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GridRowHeaderSlot(
    label: String,
    width: Dp,
    height: Dp,
    borderColor: Color,
) {
    Surface(
        modifier = Modifier
            .width(width)
            .height(height),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GridMatrixCell(
    subject: String?,
    width: Dp,
    height: Dp,
    borderColor: Color,
    fill: Color?,
    showConflictOutline: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val bg = fill
        ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

    val border = when {
        showConflictOutline ->
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        else ->
            BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
    }

    val surfaceModifier =
        Modifier
            .width(width)
            .height(height)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )

    Surface(
        modifier = surfaceModifier,
        color = bg,
        border = border,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = subject ?: "—",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
