package com.remindflow.app.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Dashboard : NavRoutes("dashboard")
    data object AddReminder : NavRoutes("add_reminder")
    data object EditReminder : NavRoutes("edit_reminder/{reminderId}") {
        fun createRoute(reminderId: Long) = "edit_reminder/$reminderId"
    }
    data object Notifications : NavRoutes("notifications")
    data object Inventory : NavRoutes("inventory")
    data object Analytics : NavRoutes("analytics")
    data object Settings : NavRoutes("settings")
}
