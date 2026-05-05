package com.mukrram.timetable.navigation

fun overlayScreenTitle(route: String?): String? = when (route) {
    ExtraRoutes.Analytics -> "Analytics"
    ExtraRoutes.Export -> "Export"
    ExtraRoutes.Notifications -> "Notifications"
    MainDestination.Substitution.route,
    ExtraRoutes.Substitution,
    -> "Substitution"
    else -> null
}

fun isOverlayDestination(route: String?): Boolean = overlayScreenTitle(route) != null
