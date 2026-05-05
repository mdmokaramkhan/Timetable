package com.mukrram.timetable.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableTopAppBar(
    titleText: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    /** When set, elevation follows content scroll (shadow only when body scrolls under the bar). */
    scrollBehavior: TopAppBarScrollBehavior? = null,
    /** Used only when [scrollBehavior] is null (e.g. nested auth screens). */
    elevation: Dp = 0.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    val barColors = TopAppBarDefaults.topAppBarColors(
        containerColor = if (scrollBehavior != null) {
            containerColor
        } else {
            Color.Transparent
        },
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    if (scrollBehavior != null) {
        TopAppBar(
            title = {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            modifier = modifier.padding(horizontal = 4.dp),
            navigationIcon = navigationIcon,
            actions = actions,
            colors = barColors,
            scrollBehavior = scrollBehavior,
        )
    } else {
        Surface(
            modifier = modifier.shadow(elevation),
            color = containerColor,
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                navigationIcon = navigationIcon,
                actions = actions,
                colors = barColors.copy(containerColor = Color.Transparent),
                scrollBehavior = null,
            )
        }
    }
}
