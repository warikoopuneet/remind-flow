package com.remindflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.remindflow.app.RemindFlowApplication
import com.remindflow.app.ui.add_edit.AddEditReminderScreen
import com.remindflow.app.ui.add_edit.AddEditViewModel
import com.remindflow.app.ui.analytics.AnalyticsScreen
import com.remindflow.app.ui.analytics.AnalyticsViewModel
import com.remindflow.app.ui.dashboard.DashboardScreen
import com.remindflow.app.ui.dashboard.DashboardViewModel
import com.remindflow.app.ui.inventory.InventoryScreen
import com.remindflow.app.ui.inventory.InventoryViewModel
import com.remindflow.app.ui.settings.SettingsScreen

@Composable
fun RemindFlowNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as RemindFlowApplication
    val repository = app.repository
    val alarmScheduler = app.alarmScheduler

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard.route,
        modifier = modifier
    ) {
        composable(NavRoutes.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(repository, alarmScheduler)
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAdd = { navController.navigate(NavRoutes.AddReminder.route) },
                onNavigateToEdit = { reminderId ->
                    navController.navigate(NavRoutes.EditReminder.createRoute(reminderId))
                },
                onNavigateToNotifications = { navController.navigate(NavRoutes.Notifications.route) },
                onNavigateToInventory = { navController.navigate(NavRoutes.Inventory.route) },
                onNavigateToAnalytics = { navController.navigate(NavRoutes.Analytics.route) },
                onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) }
            )
        }

        composable(NavRoutes.Notifications.route) {
            com.remindflow.app.ui.notifications.NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.AddReminder.route) {
            val addViewModel: AddEditViewModel = viewModel(
                factory = AddEditViewModel.Factory(repository, alarmScheduler, null)
            )
            AddEditReminderScreen(
                viewModel = addViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditReminder.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
            val editViewModel: AddEditViewModel = viewModel(
                factory = AddEditViewModel.Factory(repository, alarmScheduler, reminderId)
            )
            AddEditReminderScreen(
                viewModel = editViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Inventory.route) {
            val inventoryViewModel: InventoryViewModel = viewModel(
                factory = InventoryViewModel.Factory(repository)
            )
            InventoryScreen(
                viewModel = inventoryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { reminderId ->
                    navController.navigate(NavRoutes.EditReminder.createRoute(reminderId))
                }
            )
        }

        composable(NavRoutes.Analytics.route) {
            val analyticsViewModel: AnalyticsViewModel = viewModel(
                factory = AnalyticsViewModel.Factory(repository)
            )
            AnalyticsScreen(
                viewModel = analyticsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
