package com.vttcabs.admin.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vttcabs.admin.ui.screens.auth.LoginScreen
import com.vttcabs.admin.ui.screens.admin.DashboardScreen
import com.vttcabs.admin.viewmodel.AdminViewModel

sealed class AdminScreen(val route: String) {
    object Login : AdminScreen("login")
    object Dashboard : AdminScreen("dashboard")
}

@Composable
fun VttAdminApp() {
    val navController = rememberNavController()
    val viewModel = AdminViewModel(LocalContext.current.applicationContext as Application)
    val uiState by viewModel.uiState.collectAsState()
    
    val startDestination = if (uiState.isLoggedIn) AdminScreen.Dashboard.route else AdminScreen.Login.route
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable(AdminScreen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(AdminScreen.Dashboard.route) {
                        popUpTo(AdminScreen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(AdminScreen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(AdminScreen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
