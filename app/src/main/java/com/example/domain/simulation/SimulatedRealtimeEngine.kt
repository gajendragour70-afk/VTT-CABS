package com.example.domain.simulation

import com.example.data.model.BookingStatus
import com.example.data.repository.VttRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SimulatedRealtimeEngine(private val repository: VttRepository) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var trackingJob: Job? = null

    fun startTrackingBooking(bookingId: String) {
        trackingJob?.cancel()
        trackingJob = scope.launch {
            var currentBooking = repository.getBookingById(bookingId) ?: return@launch
            
            // Smooth live GPS tracking simulation when active
            while (true) {
                delay(3000)
                currentBooking = repository.getBookingById(bookingId) ?: break

                when (currentBooking.bookingStatus) {
                    BookingStatus.ACCEPTED, BookingStatus.DRIVER_ARRIVING, BookingStatus.ARRIVED, BookingStatus.ASSIGNED -> {
                        // Move driver towards pickup
                        val dLat = currentBooking.driverLat
                        val dLng = currentBooking.driverLng
                        val pLat = currentBooking.pickupLat
                        val pLng = currentBooking.pickupLng

                        val stepLat = dLat + (pLat - dLat) * 0.25
                        val stepLng = dLng + (pLng - dLng) * 0.25

                        val updated = currentBooking.copy(driverLat = stepLat, driverLng = stepLng)
                        repository.updateBooking(updated)
                        if (currentBooking.driverId != null) {
                            repository.updateDriverLocation(currentBooking.driverId, stepLat, stepLng)
                        }
                    }

                    BookingStatus.TRIP_STARTED, BookingStatus.IN_PROGRESS -> {
                        // Move driver from pickup to drop
                        val dLat = currentBooking.driverLat
                        val dLng = currentBooking.driverLng
                        val targetLat = currentBooking.dropLat
                        val targetLng = currentBooking.dropLng

                        val stepLat = dLat + (targetLat - dLat) * 0.2
                        val stepLng = dLng + (targetLng - dLng) * 0.2

                        val updated = currentBooking.copy(driverLat = stepLat, driverLng = stepLng)
                        repository.updateBooking(updated)
                        if (currentBooking.driverId != null) {
                            repository.updateDriverLocation(currentBooking.driverId, stepLat, stepLng)
                        }
                    }

                    BookingStatus.TRIP_COMPLETED, BookingStatus.COMPLETED, BookingStatus.CANCELLED -> {
                        break
                    }

                    else -> {}
                }
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
    }
}
