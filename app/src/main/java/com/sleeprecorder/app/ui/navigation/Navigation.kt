package com.sleeprecorder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sleeprecorder.app.ui.screens.*
import com.sleeprecorder.app.ui.viewmodel.SleepRecorderViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SleepSession : Screen("sleep_session")
    object SleepDetail : Screen("sleep_detail/{recordId}") {
        fun createRoute(recordId: String) = "sleep_detail/$recordId"
    }
    object Settings : Screen("settings")
    object AlarmSetting : Screen("alarm_setting")
}

@Composable
fun SleepRecorderNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: SleepRecorderViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onStartSleep = { navController.navigate(Screen.SleepSession.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onRecordClick = { recordId ->
                    navController.navigate(Screen.SleepDetail.createRoute(recordId))
                }
            )
        }
        
        composable(Screen.SleepSession.route) {
            SleepSessionScreen(
                viewModel = viewModel,
                onFinish = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.SleepDetail.route,
            arguments = listOf(navArgument("recordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
            SleepDetailScreen(
                recordId = recordId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}