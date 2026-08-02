package com.vttcabs.common

object FareCalculator {

    fun calculateFare(
        vehicleCategory: VehicleCategory,
        bookingType: BookingType,
        distanceKm: Double,
        durationMins: Int = 0,
        pickupTimeStr: String = "",
        waitingMins: Int = 0,
        extraHours: Int = 0
    ): FareBreakdown {
        var baseFare = 0.0
        var distanceFare = 0.0
        var perKmRateText = ""
        var rateBreakdownNote = ""
        var driverAllowance = 0.0
        var minimumFareApplied = false

        // Determine Base Fare according to Vehicle Category
        val vehicleBaseFare = when (vehicleCategory) {
            VehicleCategory.HATCHBACK -> 100.0
            VehicleCategory.SEDAN -> 150.0
            VehicleCategory.ERTIGA -> 200.0
            VehicleCategory.SUV -> 250.0
            VehicleCategory.INNOVA_CRYSTA -> 300.0
            VehicleCategory.TEMPO_TRAVELLER -> 400.0
        }

        // Vehicle multiplier for tiered distance rates relative to Sedan base
        val vehicleKmMultiplier = when (vehicleCategory) {
            VehicleCategory.HATCHBACK -> 0.8
            VehicleCategory.SEDAN -> 1.0
            VehicleCategory.ERTIGA -> 1.2
            VehicleCategory.SUV -> 1.4667
            VehicleCategory.INNOVA_CRYSTA -> 1.6
            VehicleCategory.TEMPO_TRAVELLER -> 1.8667
        }

        when (bookingType) {
            BookingType.ONE_WAY -> {
                baseFare = vehicleBaseFare
                val (sedanTierRate, note) = when {
                    distanceKm <= 20.0 -> Pair(18.0, "0-20 km @ ₹${(18.0 * vehicleKmMultiplier).toInt()}/km")
                    distanceKm <= 100.0 -> Pair(16.0, "21-100 km @ ₹${(16.0 * vehicleKmMultiplier).toInt()}/km")
                    else -> Pair(15.0, "101+ km @ ₹${(15.0 * vehicleKmMultiplier).toInt()}/km")
                }
                val actualRate = Math.round((sedanTierRate * vehicleKmMultiplier) * 10.0) / 10.0
                perKmRateText = "₹${actualRate.toInt()}/km"
                rateBreakdownNote = note
                distanceFare = Math.round((distanceKm * actualRate) * 100.0) / 100.0

                val rawTotal = baseFare + distanceFare
                if (rawTotal < 350.0) {
                    minimumFareApplied = true
                }
            }

            BookingType.ROUND_TRIP, BookingType.OUTSTATION -> {
                baseFare = 250.0
                val roundTripPerKm = when (vehicleCategory) {
                    VehicleCategory.HATCHBACK -> 11.0
                    VehicleCategory.SEDAN -> 14.0
                    VehicleCategory.ERTIGA -> 15.0
                    VehicleCategory.SUV -> 18.0
                    VehicleCategory.INNOVA_CRYSTA -> 21.0
                    VehicleCategory.TEMPO_TRAVELLER -> 25.0
                }
                perKmRateText = "₹${roundTripPerKm.toInt()}/km"

                // Minimum billing 250 km/day
                val billableKm = maxOf(distanceKm, 250.0)
                if (distanceKm < 250.0) {
                    minimumFareApplied = true
                    rateBreakdownNote = "Round Trip (Min 250 km billing)"
                } else {
                    rateBreakdownNote = "Round Trip Standard"
                }

                distanceFare = Math.round((billableKm * roundTripPerKm) * 100.0) / 100.0
                driverAllowance = 300.0 // Driver allowance ₹300/day
            }

            BookingType.LOCAL, BookingType.CITY_TOUR -> {
                val localPackagePrice = when (vehicleCategory) {
                    VehicleCategory.HATCHBACK -> 1500.0
                    VehicleCategory.SEDAN -> 1800.0
                    VehicleCategory.ERTIGA -> 2200.0
                    VehicleCategory.SUV -> 2600.0
                    VehicleCategory.INNOVA_CRYSTA -> 3200.0
                    VehicleCategory.TEMPO_TRAVELLER -> 4500.0
                }
                baseFare = localPackagePrice
                perKmRateText = "8Hr/80Km Flat"
                rateBreakdownNote = "Local Package (8 Hours / 80 KM)"

                val extraKm = maxOf(0.0, distanceKm - 80.0)
                val extraKmCharge = Math.round(extraKm * 14.0 * 100.0) / 100.0
                val extraHourCharge = extraHours * 150.0

                distanceFare = extraKmCharge + extraHourCharge
            }

            BookingType.AIRPORT_TRANSFER, BookingType.AIRPORT_PICKUP, BookingType.AIRPORT_DROP -> {
                val airportFixed = when (vehicleCategory) {
                    VehicleCategory.HATCHBACK -> 950.0
                    VehicleCategory.SEDAN -> 1200.0
                    VehicleCategory.ERTIGA -> 1500.0
                    VehicleCategory.SUV -> 1800.0
                    VehicleCategory.INNOVA_CRYSTA -> 2400.0
                    VehicleCategory.TEMPO_TRAVELLER -> 3000.0
                }
                baseFare = airportFixed
                perKmRateText = "Airport Zone Fixed"
                rateBreakdownNote = "Airport Transfer Fixed Rate"
                distanceFare = 0.0
            }
        }

        // Night Charge (10 PM to 5 AM) = +10%
        val isNightTime = isNightTimePickup(pickupTimeStr)
        val nightCharge = if (isNightTime) {
            Math.round(((baseFare + distanceFare) * 0.10) * 100.0) / 100.0
        } else 0.0

        // Waiting Charge: 30 mins free for Airport, 15 mins for others. After free waiting ₹2/min
        val freeWaiting = if (bookingType == BookingType.AIRPORT_TRANSFER || bookingType == BookingType.AIRPORT_PICKUP || bookingType == BookingType.AIRPORT_DROP) 30 else 15
        val payableWaitingMins = maxOf(0, waitingMins - freeWaiting)
        val waitingCharge = payableWaitingMins * 2.0

        var subtotalBeforeMin = baseFare + distanceFare + driverAllowance + nightCharge + waitingCharge
        if (bookingType == BookingType.ONE_WAY && subtotalBeforeMin < 350.0) {
            subtotalBeforeMin = 350.0
            minimumFareApplied = true
        }

        val grandTotal = Math.round(subtotalBeforeMin * 100.0) / 100.0

        return FareBreakdown(
            baseFare = baseFare,
            distanceFare = distanceFare,
            perKmRate = 0.0,
            perKmRateText = perKmRateText,
            rateBreakdownNote = rateBreakdownNote,
            distanceKm = distanceKm,
            durationMins = durationMins,
            minimumFareApplied = minimumFareApplied,
            timeFare = waitingCharge,
            nightCharge = nightCharge,
            driverAllowance = driverAllowance,
            tollCharge = 0.0, // Toll is separate (not added automatically)
            parkingCharge = 0.0, // Parking is separate (not added automatically)
            surgeMultiplier = 1.0,
            subtotal = grandTotal,
            gstAmount = 0.0,
            grandTotal = grandTotal,
            disclaimer = "Fare excludes Toll, Parking & State Tax. These will be paid separately."
        )
    }

    private fun isNightTimePickup(pickupTimeStr: String): Boolean {
        if (pickupTimeStr.isBlank()) return false
        try {
            val cleanStr = pickupTimeStr.trim().uppercase()
            if (cleanStr.contains("PM")) {
                val hour = cleanStr.substringBefore(":").trim().toIntOrNull() ?: 0
                if (hour == 10 || hour == 11 || hour == 12) return true
            } else if (cleanStr.contains("AM")) {
                val hour = cleanStr.substringBefore(":").trim().toIntOrNull() ?: 0
                if (hour in 0..4 || hour == 12) return true
            } else if (cleanStr.contains(":")) {
                val hour = cleanStr.substringBefore(":").trim().toIntOrNull() ?: 0
                if (hour >= 22 || hour < 5) return true
            }
        } catch (e: Exception) {}
        return false
    }
}

