package com.mukrram.timetable.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import com.mukrram.timetable.data.model.UserRole
import com.mukrram.timetable.data.remote.SessionState

sealed class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Dashboard : MainDestination("dashboard", "Dashboard", Icons.Rounded.Dashboard)
    data object Manage : MainDestination("manage", "Manage", Icons.Rounded.ViewList)
    data object Generate : MainDestination("generate", "Generate", Icons.Rounded.AutoAwesome)
    data object Timetable : MainDestination("timetable", "Timetable", Icons.Rounded.CalendarMonth)
    data object Profile : MainDestination("profile", "Profile", Icons.Rounded.Person)

    /** Not shown in the bottom bar; opened from Timetable. */
    data object Substitution : MainDestination("substitution", "Substitution", Icons.Rounded.SwapHoriz)

    companion object {
        val items: List<MainDestination> = listOf(
            Dashboard,
            Manage,
            Generate,
            Timetable,
            Profile
        )

        fun bottomBarDestinations(session: SessionState): List<MainDestination> = when (session) {
            SessionState.LoggedOut -> emptyList()
            is SessionState.LoggedIn -> when (session.role) {
                UserRole.Admin -> items
                UserRole.Faculty -> listOf(Timetable, Profile)
            }
        }

        fun startRoute(session: SessionState, hasCompletedOnboarding: Boolean): String = when (session) {
            SessionState.LoggedOut ->
                if (hasCompletedOnboarding) AuthRoutes.Login else AuthRoutes.Onboarding
            is SessionState.LoggedIn -> when (session.role) {
                UserRole.Admin -> Dashboard.route
                UserRole.Faculty -> Timetable.route
            }
        }

        /** Routes that show the bottom navigation bar (main tabs). */
        fun isBottomBarRoute(route: String?, session: SessionState): Boolean {
            if (route == null) return false
            return bottomBarDestinations(session).any { it.route == route }
        }
    }
}
