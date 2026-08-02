package com.vttcabs.driver.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vttcabs.driver.ui.auth.LoginScreen
import com.vttcabs.driver.ui.auth.OtpScreen
import com.vttcabs.driver.ui.auth.RegisterScreen
import com.vttcabs.driver.ui.dashboard.DashboardScreen
import com.vttcabs.driver.ui.booking.BookingDetailScreen
import com.vttcabs.driver.ui.profile.ProfileScreen
import com.vttcabs.driver.ui.history.HistoryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Otp : Screen("otp/{email}") {
        fun createRoute(email: String) = "otp/$email"
    }
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object BookingDetail : Screen("booking/{bookingId}") {
        fun createRoute(bookingId: String) = "booking/$bookingId"
    }
    object Profile : Screen("profile")
    object History : Screen("history")
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
                onNavigateToOtp = { email ->
                    navController.navigate(Screen.Otp.createRoute(email))
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Otp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OtpScreen(
                email = email,
                onNavigateBack = { navController.popBackStack() },
                onVerifySuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToBooking = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.BookingDetail.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailScreen(
                bookingId = bookingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBooking = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                }
            )
        }
    }
}
