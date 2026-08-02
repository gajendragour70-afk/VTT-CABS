package com.vttcabs.customer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vttcabs.customer.ui.screens.auth.LoginScreen
import com.vttcabs.customer.ui.screens.auth.SignupScreen
import com.vttcabs.customer.ui.screens.customer.BookingScreen
import com.vttcabs.customer.ui.screens.customer.HomeScreen
import com.vttcabs.customer.ui.screens.customer.BookingHistoryScreen
import com.vttcabs.customer.ui.screens.customer.ProfileScreen
import com.vttcabs.customer.ui.screens.map.TrackingScreen
import com.vttcabs.customer.viewmodel.CustomerViewModel

sealed class CustomerScreen(val route: String) {
    object Login : CustomerScreen("login")
    object Signup : CustomerScreen("signup")
    object Home : CustomerScreen("home")
    object Booking : CustomerScreen("booking")
    object Tracking : CustomerScreen("tracking/{bookingId}") {
        fun createRoute(bookingId: String) = "tracking/$bookingId"
    }
    object History : CustomerScreen("history")
    object Profile : CustomerScreen("profile")
}

@Composable
fun VttCustomerApp() {
    val navController = rememberNavController()
    val viewModel = viewModel<CustomerViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    
    val startDestination = if (uiState.isLoggedIn) CustomerScreen.Home.route else CustomerScreen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(CustomerScreen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(CustomerScreen.Home.route) {
                        popUpTo(CustomerScreen.Login.route) { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate(CustomerScreen.Signup.route)
                }
            )
        }
        
        composable(CustomerScreen.Signup.route) {
            SignupScreen(
                viewModel = viewModel,
                onSignupSuccess = {
                    navController.navigate(CustomerScreen.Home.route) {
                        popUpTo(CustomerScreen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(CustomerScreen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onBookClick = {
                    navController.navigate(CustomerScreen.Booking.route)
                },
                onHistoryClick = {
                    navController.navigate(CustomerScreen.History.route)
                },
                onProfileClick = {
                    navController.navigate(CustomerScreen.Profile.route)
                }
            )
        }
        
        composable(CustomerScreen.Booking.route) {
            BookingScreen(
                viewModel = viewModel,
                onBookingCreated = { bookingId ->
                    navController.navigate(CustomerScreen.Tracking.createRoute(bookingId)) {
                        popUpTo(CustomerScreen.Home.route)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(CustomerScreen.Tracking.route) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            TrackingScreen(
                viewModel = viewModel,
                bookingId = bookingId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(CustomerScreen.History.route) {
            BookingHistoryScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(CustomerScreen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(CustomerScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
