package com.mukrram.timetable.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mukrram.timetable.ui.theme.AppSpacing

enum class FeedbackTone {
    Info,
    Success,
    Error,
}

@Composable
private fun toneColor(tone: FeedbackTone) = when (tone) {
    FeedbackTone.Info -> MaterialTheme.colorScheme.primary
    FeedbackTone.Success -> MaterialTheme.colorScheme.tertiary
    FeedbackTone.Error -> MaterialTheme.colorScheme.error
}

@Composable
private fun toneIcon(tone: FeedbackTone): ImageVector = when (tone) {
    FeedbackTone.Info -> Icons.Outlined.Info
    FeedbackTone.Success -> Icons.Outlined.CheckCircle
    FeedbackTone.Error -> Icons.Outlined.ErrorOutline
}

@Composable
fun CenteredLoading(
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        if (message != null) {
            Spacer(Modifier.height(AppSpacing.md))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: FeedbackTone = FeedbackTone.Info,
) {
    val accent = toneColor(tone)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        (icon ?: toneIcon(tone)).let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = accent.copy(alpha = 0.72f),
            )
            Spacer(Modifier.height(AppSpacing.md))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Try again",
) {
    val accent = toneColor(FeedbackTone.Error)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = accent,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.lg))
        Button(onClick = onRetry) {
            Text(retryLabel)
        }
    }
}

@Composable
fun InlineFeedbackCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    tone: FeedbackTone = FeedbackTone.Info,
) {
    val accent = toneColor(tone)
    AppCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accent.copy(alpha = 0.12f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun InlineErrorBanner(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(AppSpacing.md))
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
