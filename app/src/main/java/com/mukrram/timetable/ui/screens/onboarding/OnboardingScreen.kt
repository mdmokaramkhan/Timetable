package com.mukrram.timetable.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.navigation.AuthRoutes
import com.mukrram.timetable.ui.components.AppButton
import com.mukrram.timetable.ui.theme.AppSpacing
import com.mukrram.timetable.ui.theme.TimetableTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

private val pages = listOf(
    OnboardingPage(
        title = "A joyful way to plan",
        body = "Keep batches, faculty, and rooms in one clean flow - then generate conflict-aware schedules in seconds.",
        icon = Icons.Filled.CalendarMonth,
    ),
    OnboardingPage(
        title = "See everything clearly",
        body = "Track progress from the dashboard and keep your core data tidy before each generation run.",
        icon = Icons.Filled.AutoGraph,
    ),
    OnboardingPage(
        title = "Share the right view",
        body = "Generate options, save your best fit, and browse timetables by batch, faculty, or room view.",
        icon = Icons.Filled.Groups,
    ),
    OnboardingPage(
        title = "Ready when you are",
        body = "Sign in and sync with your server. Faculty see personal schedules while admins manage the whole system.",
        icon = Icons.AutoMirrored.Filled.Login,
    ),
)

@Composable
fun OnboardingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val repository = ServiceLocator.repository
    val scope = rememberCoroutineScope()

    OnboardingScreenContent(
        onFinish = {
            scope.launch {
                repository.setHasCompletedOnboarding(true)
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(AuthRoutes.Onboarding) { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun OnboardingScreenContent(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.xl),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.lg, bottom = AppSpacing.sm),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onFinish) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                val item = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // Styled Icon Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(140.dp)
                        ) {}
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(100.dp),
                            shadowElevation = 6.dp
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(AppSpacing.xl),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xxl))

                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp),
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xl))

                    AnimatedContent(
                        targetState = item,
                        label = "onboardingPageText",
                    ) { state ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            Text(
                                text = state.body,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = AppSpacing.md)
                            )
                        }
                    }
                }
            }

            // Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.lg),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.indices.forEach { index ->
                    val selected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (selected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            ),
                    )
                }
            }

            // Bottom Button
            val lastPage = pagerState.currentPage == pages.lastIndex
            AppButton(
                onClick = {
                    if (lastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    if (lastPage) "Get Started" else "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.xxl))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    TimetableTheme {
        OnboardingScreenContent(onFinish = {})
    }
}
