package com.mukrram.timetable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mukrram.timetable.ui.theme.AppSpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mukrram.timetable.R
import com.mukrram.timetable.data.connectivity.rememberIsOnline
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.navigation.AuthRoutes
import com.mukrram.timetable.navigation.ExtraRoutes
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.ui.components.AppNavigationBar
import com.mukrram.timetable.ui.components.AppNavigationItem
import com.mukrram.timetable.ui.components.TimetableTopAppBar
import com.mukrram.timetable.ui.screens.auth.LoginScreen
import com.mukrram.timetable.ui.screens.auth.RegisterScreen
import com.mukrram.timetable.ui.screens.onboarding.OnboardingScreen
import com.mukrram.timetable.ui.screens.analytics.AnalyticsScreen
import com.mukrram.timetable.ui.screens.dashboard.DashboardScreen
import com.mukrram.timetable.ui.screens.export.ExportScreen
import com.mukrram.timetable.ui.screens.generate.GenerateScreen
import com.mukrram.timetable.ui.screens.manage.ManageScreen
import com.mukrram.timetable.ui.screens.notifications.NotificationsScreen
import com.mukrram.timetable.ui.screens.profile.ProfileScreen
import com.mukrram.timetable.ui.screens.substitution.SubstitutionScreen
import com.mukrram.timetable.ui.screens.timetable.TimetableScreen
import com.mukrram.timetable.ui.viewmodel.AppViewModelFactory
import kotlinx.coroutines.flow.first

@Composable
fun TimetableApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModelFactory = remember { AppViewModelFactory(ServiceLocator.repository) }

    CompositionLocalProvider(LocalAppViewModelFactory provides viewModelFactory) {
        TimetableAppContent(
            navController = navController,
            modifier = modifier,
        )
    }
}

@Composable
private fun TimetableAppContent(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val repository = ServiceLocator.repository
    val sessionState by repository.sessionState.collectAsStateWithLifecycle(
        initialValue = SessionState.LoggedOut,
    )
    val hasCompletedOnboarding by repository.hasCompletedOnboarding.collectAsStateWithLifecycle(
        initialValue = false,
    )
    var bootstrapReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        repository.sessionState.first()
        repository.hasCompletedOnboarding.first()
        bootstrapReady = true
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomDestinations = MainDestination.bottomBarDestinations(sessionState)
    val isOnline = rememberIsOnline()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TimetableTopAppBar(
                titleText = "",
                elevation = 2.dp, // Subtle elevation
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(MainDestination.Profile.route) {
                            popUpTo(MainDestination.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(0.5.dp, CircleShape)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profile),
                                contentDescription = "Profile",
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { navController.navigate(ExtraRoutes.Substitution) }) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = "Substitution")
                    }
                    IconButton(onClick = { navController.navigate(ExtraRoutes.Notifications) },) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
            )
        },
        bottomBar = {
            if (bottomDestinations.isNotEmpty()) {
                AppNavigationBar {
                    bottomDestinations.forEach { destination ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        AppNavigationItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            label = destination.label,
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (!isOnline) {
                OfflineBanner()
            }
            if (!bootstrapReady) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                key(sessionState) {
                    NavHost(
                        navController = navController,
                        startDestination = MainDestination.startRoute(
                            sessionState,
                            hasCompletedOnboarding,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    ) {
                        when (sessionState) {
                            SessionState.LoggedOut -> {
                                composable(AuthRoutes.Onboarding) {
                                    OnboardingScreen(navController = navController)
                                }
                                composable(AuthRoutes.Login) {
                                    LoginScreen(navController = navController)
                                }
                                composable(AuthRoutes.Register) {
                                    RegisterScreen(navController = navController)
                                }
                            }
                            is SessionState.LoggedIn -> {
                                val loggedIn = sessionState as SessionState.LoggedIn
                                if (loggedIn.role == UserRole.Admin) {
                                    composable(MainDestination.Dashboard.route) {
                                        DashboardScreen(navController = navController)
                                    }
                                    composable(MainDestination.Manage.route) {
                                        ManageScreen(navController = navController)
                                    }
                                    composable(MainDestination.Generate.route) {
                                        GenerateScreen(navController = navController)
                                    }
                                }
                                composable(MainDestination.Timetable.route) {
                                    TimetableScreen(
                                        onNavigateToSubstitution = {
                                            navController.navigate(MainDestination.Substitution.route)
                                        },
                                    )
                                }
                                if (loggedIn.role == UserRole.Admin) {
                                    composable(MainDestination.Substitution.route) {
                                        SubstitutionScreen(navController = navController)
                                    }
                                    composable(ExtraRoutes.Analytics) {
                                        AnalyticsScreen(navController = navController)
                                    }
                                    composable(ExtraRoutes.Export) {
                                        ExportScreen(navController = navController)
                                    }
                                    composable(ExtraRoutes.Notifications) {
                                        NotificationsScreen(navController = navController)
                                    }
                                }
                                composable(MainDestination.Profile.route) {
                                    ProfileScreen(navController = navController)
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
private fun OfflineBanner() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "You are offline. Connect to sync with the server. Recent activity may show the last cached timetable.",
            modifier = Modifier.padding(
                horizontal = AppSpacing.lg,
                vertical = AppSpacing.md,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
