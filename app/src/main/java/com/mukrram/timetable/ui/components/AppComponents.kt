package com.mukrram.timetable.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.Text
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mukrram.timetable.ui.theme.AppSpacing

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
        content = { content() },
    )
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    content: @Composable () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        contentPadding = contentPadding,
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        content = { content() },
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
    ),
    content: @Composable () -> Unit,
) {
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = border,
            content = { content() },
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = border,
            content = { content() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
        ),
        label = { label() },
    )
}

@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    ),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
    )
}

@Composable
fun AppFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        content = { content() },
    )
}

@Composable
fun AppNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = 2.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        content = content,
    )
}

@Composable
fun RowScope.AppNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "bottom_item_content_color",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        },
        label = "bottom_item_container_color",
    )
    val iconContainerSize by animateDpAsState(
        targetValue = if (selected) 38.dp else 32.dp,
        label = "bottom_item_icon_size",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(iconContainerSize)
                .background(
                    color = containerColor,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

