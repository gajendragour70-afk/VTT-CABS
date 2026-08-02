package com.vttcabs.driver.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vttcabs.driver.ui.screens.auth.LoginScreen
import com.vttcabs.driver.ui.screens.driver.DashboardScreen
import com.vttcabs.driver.ui.screens.driver.BookingDetailScreen
import com.vttcabs.driver.viewmodel.DriverViewModel

sealed class DriverScreen(val route: String) {
    object Login : DriverScreen("login")
    object Dashboard : DriverScreen("dashboard")
    object BookingDetail : DriverScreen("booking/{bookingId}") {
        fun createRoute(bookingId: String) = "booking/$bookingId"
    }
}

@Composable
fun VttDriverApp() {
    val navController = rememberNavController()
    val viewModel = DriverViewModel(LocalContext.current.applicationContext as Application)
    val uiState by viewModel.uiState.collectAsState()
    
    val startDestination = if (uiState.isLoggedIn) DriverScreen.Dashboard.route else DriverScreen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(DriverScreen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(DriverScreen.Dashboard.route) {
                        popUpTo(DriverScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(DriverScreen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onBookingClick = { bookingId ->
                    navController.navigate(DriverScreen.BookingDetail.createRoute(bookingId))
                },
                onLogout = {
                    navController.navigate(DriverScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(DriverScreen.BookingDetail.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            BookingDetailScreen(
                viewModel = viewModel,
                bookingId = bookingId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
