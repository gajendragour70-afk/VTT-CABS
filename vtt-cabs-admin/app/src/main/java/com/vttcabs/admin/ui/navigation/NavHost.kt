package com.vttcabs.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vttcabs.admin.ui.auth.LoginScreen
import com.vttcabs.admin.ui.dashboard.DashboardScreen
import com.vttcabs.admin.ui.drivers.DriversScreen
import com.vttcabs.admin.ui.bookings.BookingsScreen
import com.vttcabs.admin.ui.analytics.AnalyticsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Drivers : Screen("drivers")
    object Bookings : Screen("bookings")
    object Analytics : Screen("analytics")
}

@Composable
fun VttNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToDrivers = {
                    navController.navigate(Screen.Drivers.route)
                },
                onNavigateToBookings = {
                    navController.navigate(Screen.Bookings.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Drivers.route) {
            DriversScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Bookings.route) {
            BookingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
