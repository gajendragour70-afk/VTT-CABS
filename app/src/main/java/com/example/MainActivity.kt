package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.AuthDialog
import com.example.ui.components.VttHeader
import com.example.ui.screens.admin.AdminWebPanelScreen
import com.example.ui.screens.customer.CustomerBookingScreen
import com.example.ui.screens.customer.CustomerHistoryScreen
import com.example.ui.screens.driver.DriverDashboardScreen
import com.example.ui.screens.notifications.NotificationScreen
import com.example.ui.screens.role.RoleSelectionScreen
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTCabsTheme
import com.example.ui.viewmodel.VttCabViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VttCabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VTTCabsTheme {
                VttCabsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun VttCabsApp(viewModel: VttCabViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showRoleSelection by remember { mutableStateOf(true) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var customerNavTab by remember { mutableStateOf(0) } // 0: Book Ride, 1: History, 2: Notifications
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false },
            initialRole = currentRole
        )
    }

    if (showProfileDialog) {
        com.example.ui.components.CustomerProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!showRoleSelection && currentRole != UserRole.ADMIN) {
                VttHeader(
                    currentRole = currentRole,
                    currentUser = currentUser,
                    onRoleSelected = { role ->
                        showRoleSelection = false
                        viewModel.switchRole(role)
                    },
                    onOpenRoleSelection = {
                        showRoleSelection = true
                    },
                    unreadNotificationCount = notifications.size,
                    onNotificationsClick = {
                        showRoleSelection = false
                        viewModel.switchRole(UserRole.CUSTOMER)
                        customerNavTab = 2
                    },
                    onAuthClick = {
                        if (currentRole == UserRole.CUSTOMER) {
                            showProfileDialog = true
                        } else {
                            showAuthDialog = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!showRoleSelection && currentRole == UserRole.CUSTOMER) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = VTTBluePrimary
                ) {
                    NavigationBarItem(
                        selected = customerNavTab == 0,
                        onClick = { customerNavTab = 0 },
                        icon = { Icon(Icons.Default.LocalTaxi, contentDescription = "Book Ride") },
                        label = { Text("Book Ride", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VTTBluePrimary,
                            selectedTextColor = VTTBluePrimary,
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                    NavigationBarItem(
                        selected = customerNavTab == 1,
                        onClick = { customerNavTab = 1 },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VTTBluePrimary,
                            selectedTextColor = VTTBluePrimary,
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                    NavigationBarItem(
                        selected = customerNavTab == 2,
                        onClick = { customerNavTab = 2 },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Alerts") },
                        label = { Text("Alerts", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VTTBluePrimary,
                            selectedTextColor = VTTBluePrimary,
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showRoleSelection) {
                RoleSelectionScreen(
                    viewModel = viewModel,
                    onSelectRole = { role ->
                        viewModel.switchRole(role)
                        showRoleSelection = false
                        if (role == UserRole.CUSTOMER || role == UserRole.DRIVER) {
                            showAuthDialog = true
                        }
                    }
                )
            } else {
                when (currentRole) {
                    UserRole.CUSTOMER -> {
                        when (customerNavTab) {
                            0 -> CustomerBookingScreen(viewModel = viewModel)
                            1 -> CustomerHistoryScreen(viewModel = viewModel)
                            2 -> NotificationScreen(viewModel = viewModel)
                        }
                    }
                    UserRole.DRIVER -> {
                        DriverDashboardScreen(viewModel = viewModel)
                    }
                    UserRole.ADMIN -> {
                        AdminWebPanelScreen(
                            viewModel = viewModel,
                            onBackToMobileApp = {
                                showRoleSelection = true
                                viewModel.switchRole(UserRole.CUSTOMER)
                            }
                        )
                    }
                }
            }
        }
    }
}
