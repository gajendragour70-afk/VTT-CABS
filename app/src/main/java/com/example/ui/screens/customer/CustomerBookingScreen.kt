package com.example.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.BookingType
import com.example.data.model.FareBreakdown
import com.example.data.model.PaymentMethod
import com.example.data.model.PopularPlace
import com.example.data.model.VehicleCategory
import com.example.domain.calculator.LocationUtils
import com.example.ui.components.VttMapView
import com.example.ui.theme.VTTBlueContainer
import com.example.ui.theme.VTTBlueDark
import com.example.ui.theme.VTTBluePrimary
import com.example.ui.theme.VTTDanger
import com.example.ui.theme.VTTSuccess
import com.example.ui.viewmodel.VttCabViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerBookingScreen(
    viewModel: VttCabViewModel,
    modifier: Modifier = Modifier
) {
    val selectedBookingType by viewModel.selectedBookingType.collectAsState()
    val pickupAddress by viewModel.pickupAddress.collectAsState()
    val dropAddress by viewModel.dropAddress.collectAsState()
    val pickupLat by viewModel.pickupLat.collectAsState()
    val pickupLng by viewModel.pickupLng.collectAsState()
    val dropLat by viewModel.dropLat.collectAsState()
    val dropLng by viewModel.dropLng.collectAsState()
    val selectedCategory by viewModel.selectedVehicleCategory.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val pickupDate by viewModel.pickupDate.collectAsState()
    val pickupTime by viewModel.pickupTime.collectAsState()

    val context = LocalContext.current
    val calendar = remember { java.util.Calendar.getInstance() }

    val datePickerDialog = remember(context) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                viewModel.setPickupDate(dateStr)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val timePickerDialog = remember(context) {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(java.util.Calendar.MINUTE, minute)
                val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(cal.time)
                viewModel.setPickupTime(timeStr)
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            false
        )
    }

    val activeBookingId by viewModel.activeBookingId.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val activeBooking = allBookings.find { it.id == activeBookingId }

    val routeDistanceKm by viewModel.routeDistanceKm.collectAsState()
    val routeDurationMins by viewModel.routeDurationMins.collectAsState()
    val routeError by viewModel.routeError.collectAsState()
    val isCalculatingRoute by viewModel.isCalculatingRoute.collectAsState()

    var showFareDetailsModal by remember { mutableStateOf(false) }
    var showCancelModal by remember { mutableStateOf(false) }
    var showLocationModalFor by remember { mutableStateOf<String?>(null) } // "PICKUP" or "DROP"
    var locationSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PopularPlace>>(LocationUtils.POPULAR_PLACES) }
    var isSearchingLocations by remember { mutableStateOf(false) }

    LaunchedEffect(locationSearchQuery, showLocationModalFor) {
        if (showLocationModalFor == null) return@LaunchedEffect
        val query = locationSearchQuery.trim()
        if (query.isBlank()) {
            searchResults = LocationUtils.POPULAR_PLACES
            isSearchingLocations = false
        } else {
            isSearchingLocations = true
            kotlinx.coroutines.delay(350)
            searchResults = LocationUtils.searchOpenStreetMapLocations(query)
            isSearchingLocations = false
        }
    }

    var cancelReason by remember { mutableStateOf("Change of plans") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // If there is an active ride in progress, show Live Tracking View!
        if (activeBooking != null && activeBooking.bookingStatus != BookingStatus.COMPLETED && activeBooking.bookingStatus != BookingStatus.CANCELLED) {
            ActiveRideTrackingView(
                booking = activeBooking,
                onCancelClick = { showCancelModal = true }
            )
        } else {
            // Main Ride Booking Form Flow
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // 1. Booking Type Tabs Header
                item {
                    Text(
                        text = "Where do you want to go?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        items(BookingType.values()) { type ->
                            BookingTypeChip(
                                type = type,
                                isSelected = selectedBookingType == type,
                                onClick = { viewModel.setBookingType(type) }
                            )
                        }
                    }
                }

                // 2. Pickup & Drop Selection Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Pickup Field
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        locationSearchQuery = ""
                                        showLocationModalFor = "PICKUP"
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Pickup Location",
                                    tint = VTTSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                                    Text("PICKUP LOCATION (Tap to change)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text(pickupAddress, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = VTTSuccess.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable {
                                        viewModel.useCurrentLocationForPickup()
                                    }
                                ) {
                                    Text(
                                        text = "GPS Location",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VTTSuccess,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color(0xFFE2E8F0)
                            )

                            // Drop Field
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        locationSearchQuery = ""
                                        showLocationModalFor = "DROP"
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Drop Location",
                                    tint = VTTDanger,
                                    modifier = Modifier.size(22.dp)
                                )
                                Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                                    Text("DROP LOCATION (Tap to change)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Text(dropAddress, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Color(0xFFE2E8F0)
                            )

                            // Pickup Date & Time Section
                            Text(
                                text = "PICKUP DATE & TIME (REQUIRED)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Date Field
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { datePickerDialog.show() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = VTTBlueContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, VTTBluePrimary.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Pickup Date",
                                            tint = VTTBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text("Date", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(
                                                text = pickupDate.ifBlank { "Select Date" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VTTBlueDark
                                            )
                                        }
                                    }
                                }

                                // Time Field
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { timePickerDialog.show() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = VTTBlueContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, VTTBluePrimary.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Pickup Time",
                                            tint = VTTBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text("Time", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text(
                                                text = pickupTime.ifBlank { "Select Time" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VTTBlueDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Popular Places Quick Pickers
                            Text(
                                text = "Quick Pick Popular Locations:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VTTBluePrimary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LocationUtils.POPULAR_PLACES.take(4).forEach { place ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = VTTBlueContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.clickable {
                                            viewModel.setDrop(place.name, place.lat, place.lng)
                                        }
                                    ) {
                                        Text(
                                            text = place.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = VTTBlueDark,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Interactive Route Map Preview
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        VttMapView(
                            pickupLat = pickupLat,
                            pickupLng = pickupLng,
                            dropLat = dropLat,
                            dropLng = dropLng,
                            showRoute = true
                        )
                    }
                }

                // 3.5 Route Distance & Duration Summary or Location Error
                item {
                    if (routeError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(
                                        text = "Location Error",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = routeError!!,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = VTTBluePrimary, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.padding(start = 10.dp)) {
                                        Text(
                                            text = if (isCalculatingRoute) "Calculating OSRM Road Route..." else "OSRM Road Distance & Duration",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isCalculatingRoute) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = VTTBluePrimary)
                                                Text(" Fetching OSRM route...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VTTBlueDark)
                                            } else {
                                                Text(
                                                    text = "$routeDistanceKm km",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = VTTBlueDark
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = " ~$routeDurationMins mins",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = VTTBluePrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "OSRM Live",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VTTBluePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Select Vehicle Type Header & Cards
                item {
                    Text(
                        text = "Select Vehicle",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(VehicleCategory.values()) { category ->
                    val fareBreakdown = viewModel.calculateCurrentFareBreakdown(category)
                    val isValid = routeError == null && routeDistanceKm > 0
                    VehicleCategoryCard(
                        category = category,
                        bookingType = selectedBookingType,
                        estimatedFare = if (isValid) fareBreakdown.grandTotal else 0.0,
                        isValid = isValid,
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.selectVehicleCategory(category) }
                    )
                }

                // 5. Payment Method & Fare Action Bar
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Payment Option", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(
                                    onClick = { showFareDetailsModal = true },
                                    enabled = routeError == null && routeDistanceKm > 0
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text(" Fare Details", fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentChip(
                                    title = "UPI / QR",
                                    icon = Icons.Default.QrCodeScanner,
                                    isSelected = selectedPaymentMethod == PaymentMethod.UPI,
                                    onClick = { viewModel.setPaymentMethod(PaymentMethod.UPI) },
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentChip(
                                    title = "Cash",
                                    icon = Icons.Default.Money,
                                    isSelected = selectedPaymentMethod == PaymentMethod.CASH,
                                    onClick = { viewModel.setPaymentMethod(PaymentMethod.CASH) },
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentChip(
                                    title = "Card",
                                    icon = Icons.Default.CreditCard,
                                    isSelected = selectedPaymentMethod == PaymentMethod.CARD,
                                    onClick = { viewModel.setPaymentMethod(PaymentMethod.CARD) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val currentFare = viewModel.calculateCurrentFareBreakdown(selectedCategory)
                            val isCanBook = routeError == null && routeDistanceKm > 0.0 && !isCalculatingRoute

                            Button(
                                onClick = { viewModel.confirmBooking() },
                                enabled = isCanBook,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("book_ride_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VTTBluePrimary,
                                    disabledContainerColor = Color.LightGray
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (routeError != null) {
                                        Text("INVALID LOCATION", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                                        Text("CANNOT BOOK", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                                    } else if (isCalculatingRoute) {
                                        Text("CALCULATING ROUTE...", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = VTTBluePrimary)
                                    } else {
                                        Text("BOOK ${selectedCategory.displayName.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("₹${currentFare.grandTotal}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Location Autocomplete Picker
    if (showLocationModalFor != null) {
        val target = showLocationModalFor!!

        AlertDialog(
            onDismissRequest = { showLocationModalFor = null },
            title = {
                Text(
                    text = if (target == "PICKUP") "Select Pickup Location" else "Select Drop Location",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = { locationSearchQuery = it },
                        label = { Text("Search city, station, airport, address...") },
                        placeholder = { Text("e.g. Jaipur, Delhi Airport, Bandra, Varanasi") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = VTTBluePrimary) },
                        trailingIcon = {
                            if (isSearchingLocations) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = VTTBluePrimary)
                            } else if (locationSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { locationSearchQuery = "" }) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Clear", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (target == "PICKUP") {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = VTTSuccess.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.useCurrentLocationForPickup()
                                    showLocationModalFor = null
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = VTTSuccess)
                                Text(" Use Current GPS Location", fontWeight = FontWeight.Bold, color = VTTSuccess, fontSize = 13.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (locationSearchQuery.isBlank()) "Popular Locations:" else "OpenStreetMap Search Results (${searchResults.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        if (isSearchingLocations) {
                            Text("Searching India...", fontSize = 10.sp, color = VTTBluePrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (searchResults.isEmpty() && !isSearchingLocations) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No locations found for \"$locationSearchQuery\".\nTry searching by city name, railway station, airport, or landmark.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(searchResults) { place ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = VTTBlueContainer.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val fullTitle = if (place.name != place.address && place.address.isNotBlank()) {
                                                "${place.name}, ${place.address}"
                                            } else {
                                                place.name
                                            }
                                            val displayTitle = if (fullTitle.length > 55) fullTitle.take(52) + "..." else fullTitle
                                            if (target == "PICKUP") {
                                                viewModel.setPickup(displayTitle, place.lat, place.lng)
                                            } else {
                                                viewModel.setDrop(displayTitle, place.lat, place.lng)
                                            }
                                            showLocationModalFor = null
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = VTTBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = place.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = VTTBlueDark,
                                                maxLines = 1
                                            )
                                        }
                                        if (place.address.isNotBlank() && place.address != place.name) {
                                            Text(
                                                text = place.address,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                maxLines = 2,
                                                modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationModalFor = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Fare Breakdown Details
    if (showFareDetailsModal) {
        val fare = viewModel.calculateCurrentFareBreakdown(selectedCategory)
        AlertDialog(
            onDismissRequest = { showFareDetailsModal = false },
            title = {
                Column {
                    Text("Fare Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VTTBlueDark)
                    Text("Vehicle: ${selectedCategory.displayName}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DISTANCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${fare.distanceKm} km", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = VTTBlueDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ESTIMATED TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${fare.durationMins} mins", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = VTTSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FareRow("Trip Type", selectedBookingType.name.replace("_", " "))
                    if (fare.rateBreakdownNote.isNotBlank()) {
                        FareRow("Rate Plan", fare.rateBreakdownNote)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    FareRow("Base Fare", "₹${fare.baseFare.toInt()}")
                    if (fare.distanceFare > 0.0) {
                        FareRow("Distance Charge (${fare.perKmRateText})", "₹${fare.distanceFare}")
                    }
                    if (fare.nightCharge > 0.0) {
                        FareRow("Night Charge (10 PM-5 AM +10%)", "₹${fare.nightCharge}", isHighlight = true)
                    }
                    if (fare.driverAllowance > 0.0) {
                        FareRow("Driver Allowance (Per Day)", "₹${fare.driverAllowance.toInt()}")
                    }
                    if (fare.timeFare > 0.0) {
                        FareRow("Waiting Charge", "₹${fare.timeFare}")
                    }
                    if (fare.minimumFareApplied) {
                        FareRow("Minimum Fare Policy", "Applied")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    FareRow("Subtotal", "₹${fare.subtotal.toInt()}", isBold = true)
                    FareRow("Toll Charge", "Extra (Not Included)", isSecondary = true)
                    FareRow("Parking Fee", "Extra (Not Included)", isSecondary = true)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = VTTBluePrimary)
                        Text("₹${fare.grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = VTTBluePrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice Box as explicitly mandated
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "Fare excludes Toll, Parking & State Tax. These will be paid separately.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFareDetailsModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VTTBluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Modal: Cancel Ride Dialog
    if (showCancelModal) {
        AlertDialog(
            onDismissRequest = { showCancelModal = false },
            title = { Text("Cancel Booking?", fontWeight = FontWeight.Bold, color = VTTDanger) },
            text = {
                Column {
                    Text("Please specify why you are cancelling your ride:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelActiveRide(cancelReason)
                        showCancelModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VTTDanger)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelModal = false }) {
                    Text("Keep Ride")
                }
            }
        )
    }
}

@Composable
fun ActiveRideTrackingView(
    booking: BookingEntity,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Live Map view at top 55%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {
            VttMapView(
                pickupLat = booking.pickupLat,
                pickupLng = booking.pickupLng,
                dropLat = booking.dropLat,
                dropLng = booking.dropLng,
                driverLat = booking.driverLat,
                driverLng = booking.driverLng,
                driverName = booking.driverName,
                etaMins = if (booking.bookingStatus == BookingStatus.ASSIGNED) 4 else null,
                showRoute = true
            )
        }

        // Live Status & Driver Detail Sheet at bottom 45%
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Status Header Banner
                val (statusTitle, statusColor) = when (booking.bookingStatus) {
                    BookingStatus.PENDING, BookingStatus.SEARCHING -> "Pending Admin Approval..." to Color(0xFFD97706)
                    BookingStatus.ASSIGNED -> "Driver Assigned • Awaiting Acceptance" to VTTBluePrimary
                    BookingStatus.ACCEPTED -> "Driver Accepted & On The Way!" to VTTSuccess
                    BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED -> "Driver Has Arrived!" to VTTSuccess
                    BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> "Trip in Progress to Destination" to VTTBluePrimary
                    BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED -> "Trip Completed" to VTTSuccess
                    else -> "Active Ride" to VTTBlueDark
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = statusTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Text(
                            text = "Booking #${booking.id.take(6).uppercase()}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    // OTP Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VTTBlueContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("OTP: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VTTBlueDark)
                            Text(booking.otp, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = VTTBlueDark)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Driver & Vehicle Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(VTTBlueDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White)
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = booking.driverName ?: "Assigning Driver...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text("★ 4.9", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text(
                                    text = "${booking.vehicleModel ?: booking.vehicleCategory.displayName} • ${booking.vehicleNumber ?: "KA-01-XX-XXXX"}",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "✓ Verified VTT CABS Driver & Vehicle",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VTTSuccess
                                )
                            }
                        }

                        if (booking.driverPhone != null) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = VTTSuccess)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pickup & Drop Summary
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = VTTSuccess, modifier = Modifier.size(18.dp))
                    Text(booking.pickupAddress, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(start = 6.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = VTTDanger, modifier = Modifier.size(18.dp))
                    Text(booking.dropAddress, fontSize = 12.sp, maxLines = 1, modifier = Modifier.padding(start = 6.dp))
                }

                if (booking.pickupDate.isNotBlank() || booking.pickupTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VTTBluePrimary, modifier = Modifier.size(16.dp))
                        Text(" Scheduled: ${booking.pickupDate} at ${booking.pickupTime}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = VTTBlueDark)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Action Button: Cancel Ride
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VTTDanger),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(" Cancel Booking", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BookingTypeChip(
    type: BookingType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) VTTBluePrimary else Color.White,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = type.name.replace("_", " "),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

private fun categoryRateLabel(category: VehicleCategory): String {
    return "₹${category.defaultPerKm.toInt()}/km"
}

@Composable
fun VehicleCategoryCard(
    category: VehicleCategory,
    bookingType: BookingType,
    estimatedFare: Double,
    isValid: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, VTTBluePrimary) else null,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) VTTBluePrimary else VTTBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = category.displayName,
                        tint = if (isSelected) Color.White else VTTBlueDark,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(category.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("${category.capacity} Seats", fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(category.description, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isValid) {
                    Text(
                        text = "₹${estimatedFare.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = VTTBluePrimary
                    )
                    Text(
                        text = "Fare Est.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "Unable to calculate fare",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = VTTDanger
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) VTTBluePrimary else Color(0xFFF1F5F9),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, VTTBlueDark) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.White else Color.DarkGray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.DarkGray,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun FareRow(
    label: String,
    amount: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    isSecondary: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isHighlight -> VTTBluePrimary
                isSecondary -> Color.Gray
                else -> Color.Unspecified
            }
        )
        Text(
            text = amount,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isHighlight -> VTTBluePrimary
                isSecondary -> Color.Gray
                else -> Color.Unspecified
            }
        )
    }
}
