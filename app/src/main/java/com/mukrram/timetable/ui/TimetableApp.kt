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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mukrram.timetable.ui.theme.AppSpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.rememberTopAppBarState
import androidx.navigation.compose.rememberNavController
import com.mukrram.timetable.R
import com.mukrram.timetable.data.connectivity.rememberIsOnline
import com.mukrram.timetable.data.di.ServiceLocator
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState
import com.mukrram.timetable.navigation.AuthRoutes
import com.mukrram.timetable.navigation.ExtraRoutes
import com.mukrram.timetable.navigation.MainDestination
import com.mukrram.timetable.navigation.isOverlayDestination
import com.mukrram.timetable.navigation.overlayScreenTitle
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

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val currentRoute = currentDestination?.route
    val bottomDestinations = MainDestination.bottomBarDestinations(sessionState)
    val isOnline = rememberIsOnline()
    val topAppBarScrollState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarScrollState)

    val showAuthFlow = sessionState == SessionState.LoggedOut && bootstrapReady
    val loggedInSession = sessionState as? SessionState.LoggedIn
    val showMainChrome = loggedInSession != null && bootstrapReady
    val isTabRoute =
        loggedInSession != null && MainDestination.isBottomBarRoute(currentRoute, sessionState)

    var manageTopBarSearchPulse by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (showMainChrome) {
                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    Modifier
                },
            ),
        topBar = {
            when {
                !bootstrapReady -> { }
                showAuthFlow -> { }
                loggedInSession == null -> { }
                isTabRoute -> {
                    val isAdmin = loggedInSession.role == UserRole.Admin
                    TimetableTopAppBar(
                        titleText = "",
                        navigationIcon = {
                            IconButton(onClick = {
                                navController.navigate(MainDestination.Profile.route) {
                                    when (loggedInSession.role) {
                                        UserRole.Admin ->
                                            popUpTo(MainDestination.Dashboard.route) { saveState = true }
                                        UserRole.Faculty ->
                                            popUpTo(MainDestination.Timetable.route) { saveState = true }
                                    }
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
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
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
                            IconButton(
                                onClick = {
                                    if (currentRoute == MainDestination.Manage.route) {
                                        manageTopBarSearchPulse++
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isAdmin) {
                                IconButton(onClick = { navController.navigate(ExtraRoutes.Substitution) }) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Substitution")
                                }
                                IconButton(onClick = { navController.navigate(ExtraRoutes.Notifications) }) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
                isOverlayDestination(currentRoute) -> {
                    TimetableTopAppBar(
                        titleText = overlayScreenTitle(currentRoute).orEmpty(),
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        actions = { },
                        scrollBehavior = scrollBehavior,
                    )
                }
                else -> { }
            }
        },
        bottomBar = {
            if (loggedInSession != null &&
                bottomDestinations.isNotEmpty() &&
                MainDestination.isBottomBarRoute(currentRoute, sessionState)
            ) {
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
                            icon = destination.icon,
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
                                        ManageScreen(
                                            navController = navController,
                                            topBarSearchPulse = manageTopBarSearchPulse,
                                        )
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
                                        SubstitutionScreen()
                                    }
                                    composable(ExtraRoutes.Analytics) {
                                        AnalyticsScreen()
                                    }
                                    composable(ExtraRoutes.Export) {
                                        ExportScreen()
                                    }
                                    composable(ExtraRoutes.Notifications) {
                                        NotificationsScreen()
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
