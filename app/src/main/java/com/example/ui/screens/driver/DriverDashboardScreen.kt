package com.example.ui.screens.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.DriverEntity
import com.example.ui.components.VttMapView
import com.example.ui.theme.VTTBlueContainer
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTDanger
import com.example.ui.theme.VTTSuccess
import com.example.ui.viewmodel.VttCabViewModel

@Composable
fun DriverDashboardScreen(
    viewModel: VttCabViewModel,
    modifier: Modifier = Modifier
) {
    val drivers by viewModel.allDrivers.collectAsState()
    val currentDriverState by viewModel.currentDriver.collectAsState()

    val driver = currentDriverState ?: drivers.find { it.id == "drv_01" } ?: drivers.firstOrNull() ?: DriverEntity(
        id = "drv_01",
        name = "Rajesh Sharma",
        phone = "+91 9811223344",
        email = "driver1@vtt.com",
        vehicleCategory = com.example.data.model.VehicleCategory.SEDAN,
        vehicleModel = "Honda City (White)",
        vehicleNumber = "KA-01-MJ-4821",
        currentLat = 12.9750,
        currentLng = 77.5850
    )

    // Check Approval Status
    if (driver.approvalStatus != com.example.data.model.DriverApprovalStatus.APPROVED) {
        DriverVerificationStatusScreen(driver = driver, viewModel = viewModel, modifier = modifier)
        return
    }

    val allBookings by viewModel.allBookings.collectAsState()
    val activeDriverBooking = allBookings.find {
        (it.driverId == driver.id || it.driverId == null) &&
                (it.bookingStatus == BookingStatus.SEARCHING || it.bookingStatus == BookingStatus.ASSIGNED ||
                        it.bookingStatus == BookingStatus.ARRIVED || it.bookingStatus == BookingStatus.IN_PROGRESS)
    }

    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Driver Top Online / Offline Status Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (driver.isOnline) VTTBlueDark else Color(0xFF334155)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (driver.isOnline) VTTSuccess else Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = if (driver.isOnline) "YOU ARE ONLINE" else "YOU ARE OFFLINE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${driver.vehicleModel} • ${driver.vehicleNumber}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = driver.isOnline,
                            onCheckedChange = { viewModel.toggleDriverOnline(driver.id, driver.isOnline) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VTTSuccess
                            ),
                            modifier = Modifier.testTag("driver_online_switch")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Log Out Driver",
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Chips Row (Earnings, Trips, Rating)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DriverStatBox(
                        title = "Today's Earnings",
                        value = "₹${driver.totalEarnings}",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DriverStatBox(
                        title = "Trips Done",
                        value = "${driver.totalTrips}",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DriverStatBox(
                        title = "Rating",
                        value = "★ ${driver.rating}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Main Content Area: Active Ride Navigation or Available Rides List
        if (activeDriverBooking != null) {
            DriverActiveNavigationCard(
                booking = activeDriverBooking,
                onAcceptClick = {
                    viewModel.driverAcceptBooking(activeDriverBooking.id, driver.id)
                },
                onRejectClick = {
                    viewModel.driverRejectBooking(activeDriverBooking.id, driver.id, "Driver declined request")
                },
                onArrivingClick = {
                    viewModel.driverSetArriving(activeDriverBooking.id)
                },
                onStartTripClick = { showOtpDialog = true },
                onCompleteTripClick = {
                    viewModel.driverCompleteTrip(
                        activeDriverBooking.id,
                        driver.id,
                        activeDriverBooking.totalFare
                    )
                }
            )
        } else {
            // Idle State: Waiting for incoming trip requests
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = VTTBluePrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (driver.isOnline) "Searching for Nearby Bookings..." else "Go Online to Receive Trips",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "You will be alerted instantly when a customer requests a ride.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal: Start Ride OTP Verification
    if (showOtpDialog && activeDriverBooking != null) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Enter Customer OTP", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ask passenger for their 4-digit start OTP:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it },
                        label = { Text("4-Digit OTP") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.driverVerifyOtpAndStartTrip(
                            activeDriverBooking.id,
                            enteredOtp,
                            activeDriverBooking.otp
                        )
                        showOtpDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess)
                ) {
                    Text("Verify & Start Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DriverActiveNavigationCard(
    booking: BookingEntity,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onArrivingClick: () -> Unit,
    onStartTripClick: () -> Unit,
    onCompleteTripClick: () -> Unit
) {
    val context = LocalContext.current
    val estMins = if (booking.durationMins > 0) booking.durationMins else com.example.domain.calculator.LocationUtils.estimateTripDurationMins(booking.distanceKm)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Status Badge, Booking ID & Total Fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VTTBlueContainer
                    ) {
                        Text(
                            text = booking.bookingStatus.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = VTTBlueDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Booking ID: #${booking.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Fare",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${booking.totalFare}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = VTTBluePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Map Container: Pickup Marker, Drop Marker & OSRM Route
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                VttMapView(
                    pickupLat = booking.pickupLat,
                    pickupLng = booking.pickupLng,
                    dropLat = booking.dropLat,
                    dropLng = booking.dropLng,
                    driverLat = booking.driverLat,
                    driverLng = booking.driverLng,
                    showRoute = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Distance & Estimated Travel Time Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = VTTBluePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("TOTAL DISTANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("${booking.distanceKm} km", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = VTTBlueDark)
                    }
                }
                Box(modifier = Modifier.height(24.dp).width(1.dp).background(Color.LightGray))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VTTSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("ESTIMATED TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("$estMins mins", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = VTTSuccess)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Box with Call Button & Pickup Schedule
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "CUSTOMER DETAILS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(text = booking.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Mobile: ${booking.customerPhone}", fontSize = 12.sp, color = VTTBlueDark, fontWeight = FontWeight.Medium)
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.customerPhone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {}
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(VTTSuccess.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Customer", tint = VTTSuccess)
                        }
                    }

                    if (booking.pickupDate.isNotBlank() || booking.pickupTime.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VTTBluePrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pickup Scheduled: ${booking.pickupDate} at ${booking.pickupTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VTTBlueDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Full Customer Pickup & Drop Locations
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp)
            ) {
                // Pickup Location
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VTTSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("CUSTOMER PICKUP LOCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VTTSuccess)
                        Text(
                            text = booking.pickupAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(10.dp))

                // Drop Location
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(VTTDanger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("CUSTOMER DROP LOCATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VTTDanger)
                        Text(
                            text = booking.dropAddress,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notice Box for Driver: Toll, Parking & State Tax separate
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fare excludes Toll, Parking & State Tax. These will be paid separately by customer.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E)
                    )
                }
            }

            // Turn-by-Turn Navigate Button
            Button(
                onClick = {
                    try {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${booking.dropLat},${booking.dropLng}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${booking.pickupLat},${booking.pickupLng}&destination=${booking.dropLat},${booking.dropLng}")
                        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                        context.startActivity(webIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("driver_navigate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = VTTBlueDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("NAVIGATE (TURN-BY-TURN)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons based on state
            when (booking.bookingStatus) {
                BookingStatus.ASSIGNED, BookingStatus.SEARCHING -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "⚡ New Booking Assigned! (2 min auto-timeout)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRejectClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("driver_reject_trip_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VTTDanger),
                                border = BorderStroke(1.dp, VTTDanger),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = onAcceptClick,
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp)
                                    .testTag("driver_accept_trip_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ACCEPT TRIP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                BookingStatus.ACCEPTED -> {
                    Button(
                        onClick = onArrivingClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("driver_arriving_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I'M ARRIVING AT PICKUP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> {
                    Button(
                        onClick = onStartTripClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("driver_start_trip_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTBlueDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ENTER OTP & START TRIP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> {
                    Button(
                        onClick = onCompleteTripClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("driver_complete_trip_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VTTSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("COMPLETE TRIP & COLLECT ₹${booking.totalFare}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun DriverStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun DriverVerificationStatusScreen(
    driver: DriverEntity,
    viewModel: VttCabViewModel,
    modifier: Modifier = Modifier
) {
    val statusColor = when (driver.approvalStatus) {
        com.example.data.model.DriverApprovalStatus.PENDING -> Color(0xFFD97706)
        com.example.data.model.DriverApprovalStatus.REJECTED -> VTTDanger
        com.example.data.model.DriverApprovalStatus.SUSPENDED -> Color.Gray
        else -> VTTSuccess
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (driver.approvalStatus) {
                            com.example.data.model.DriverApprovalStatus.PENDING -> Icons.Default.Info
                            com.example.data.model.DriverApprovalStatus.REJECTED -> Icons.Default.DirectionsCar
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (driver.approvalStatus) {
                        com.example.data.model.DriverApprovalStatus.PENDING -> "Verification Pending"
                        com.example.data.model.DriverApprovalStatus.REJECTED -> "Application Rejected"
                        com.example.data.model.DriverApprovalStatus.SUSPENDED -> "Account Suspended"
                        else -> "Status Verification"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = VTTBlueDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (driver.approvalStatus) {
                            com.example.data.model.DriverApprovalStatus.PENDING ->
                                "Your account is under verification. Please wait for admin approval."
                            com.example.data.model.DriverApprovalStatus.REJECTED ->
                                "Your driver registration was rejected. Reason: ${driver.rejectionReason.ifBlank { "Document Verification Failed" }}"
                            com.example.data.model.DriverApprovalStatus.SUSPENDED ->
                                "Your driver account has been suspended by Admin. Contact VTT support."
                            else -> ""
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Driver Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Name: ${driver.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Text("Phone: ${driver.phone}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Vehicle: ${driver.vehicleModel} (${driver.vehicleNumber})", fontSize = 12.sp, color = Color.DarkGray)
                        Text("DL No: ${driver.licenceNumber}", fontSize = 12.sp, color = Color.DarkGray)
                        Text("Aadhaar: ${driver.aadhaarNumber}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.showToast("Checking verification status with VTT Admin Server...")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Check Approval Status", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
