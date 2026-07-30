package com.example.domain.calculator

import android.util.Log
import com.example.data.model.PopularPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class OsrmRouteInfo(
    val distanceKm: Double,
    val durationMins: Int,
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

object LocationUtils {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Pre-mapped benchmark coordinates for major Indian cities & hubs to guarantee instant high-accuracy search
    private val KNOWN_INDIAN_LOCATIONS = listOf(
        PopularPlace("Bhopal", "Madhya Pradesh, India", 23.2599, 77.4126, "location_city"),
        PopularPlace("Indore", "Madhya Pradesh, India", 22.7196, 75.8577, "location_city"),
        PopularPlace("Ujjain", "Madhya Pradesh, India", 23.1765, 75.7885, "location_city"),
        PopularPlace("Jabalpur", "Madhya Pradesh, India", 23.1815, 79.9864, "location_city"),
        PopularPlace("Delhi", "National Capital Territory, India", 28.6139, 77.2090, "location_city"),
        PopularPlace("Agra", "Uttar Pradesh, India", 27.1767, 78.0081, "location_city"),
        PopularPlace("Gwalior", "Madhya Pradesh, India", 26.2183, 78.1828, "location_city"),
        PopularPlace("Sagar", "Madhya Pradesh, India", 23.8388, 78.7378, "location_city"),
        PopularPlace("Satna", "Madhya Pradesh, India", 24.6005, 80.8322, "location_city"),
        PopularPlace("Rewa", "Madhya Pradesh, India", 24.5362, 81.3037, "location_city"),
        PopularPlace("Mumbai", "Maharashtra, India", 19.0760, 72.8777, "location_city"),
        PopularPlace("Pune", "Maharashtra, India", 18.5204, 73.8567, "location_city"),
        PopularPlace("Jaipur", "Rajasthan, India", 26.9124, 75.7873, "location_city"),
        PopularPlace("Ahmedabad", "Gujarat, India", 23.0225, 72.5714, "location_city"),
        PopularPlace("Lucknow", "Uttar Pradesh, India", 26.8467, 80.9462, "location_city"),
        PopularPlace("Kanpur", "Uttar Pradesh, India", 26.4499, 80.3319, "location_city"),
        PopularPlace("Varanasi", "Uttar Pradesh, India", 25.3176, 82.9739, "location_city"),
        PopularPlace("Bengaluru", "Karnataka, India", 12.9716, 77.5946, "location_city"),
        PopularPlace("Hyderabad", "Telangana, India", 17.3850, 78.4867, "location_city"),
        PopularPlace("Kolkata", "West Bengal, India", 22.5726, 88.3639, "location_city"),
        PopularPlace("Chennai", "Tamil Nadu, India", 13.0827, 80.2707, "location_city")
    )

    val POPULAR_PLACES = listOf(
        PopularPlace("Bhopal Junction Railway Station", "Station Rd, Bhopal, MP", 23.2599, 77.4126, "train"),
        PopularPlace("Raja Bhoj Airport Bhopal", "Gandhi Nagar, Bhopal, MP", 23.2875, 77.3378, "flight_takeoff"),
        PopularPlace("Indore Junction Railway Station", "Chhoti Gwaltoli, Indore, MP", 22.7196, 75.8577, "train"),
        PopularPlace("Devi Ahilya Bai Holkar Airport Indore", "Depalpur Rd, Indore, MP", 22.7217, 75.8011, "flight_takeoff"),
        PopularPlace("Mahakaleshwar Temple Ujjain", "Jaisinghpura, Ujjain, MP", 23.1827, 75.7682, "place"),
        PopularPlace("Jabalpur Junction Railway Station", "South Civil Lines, Jabalpur, MP", 23.1611, 79.9497, "train"),
        PopularPlace("Indira Gandhi International Airport Delhi", "Palam, New Delhi", 28.5562, 77.1000, "flight_takeoff"),
        PopularPlace("Agra Cantonment Railway Station", "Idgah Colony, Agra, UP", 27.1585, 77.9904, "train"),
        PopularPlace("Taj Mahal Agra", "Dharmapuri, Forest Colony, Agra, UP", 27.1751, 78.0421, "place"),
        PopularPlace("Gwalior Junction Railway Station", "Padav, Gwalior, MP", 26.2183, 78.1828, "train")
    )

    suspend fun fetchOsrmRoute(
        pLat: Double,
        pLng: Double,
        dLat: Double,
        dLng: Double
    ): OsrmRouteInfo = withContext(Dispatchers.IO) {
        if (pLat == 0.0 && pLng == 0.0 || dLat == 0.0 && dLng == 0.0) {
            return@withContext OsrmRouteInfo(0.0, 0, false, "Location not found. Please select valid pickup and drop locations.")
        }
        if (Math.abs(pLat - dLat) < 0.0001 && Math.abs(pLng - dLng) < 0.0001) {
            return@withContext OsrmRouteInfo(0.0, 0, false, "Pickup and drop locations cannot be the same.")
        }

        // OSRM API expects longitude,latitude pairs: lon1,lat1;lon2,lat2
        val url = "https://router.project-osrm.org/route/v1/driving/$pLng,$pLat;$dLng,$dLat?overview=full&geometries=geojson"
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "VTTCabsApp/1.0 (contact@vttcabs.com)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val jsonObj = org.json.JSONObject(body)
                        val code = jsonObj.optString("code")
                        if (code == "Ok") {
                            val routes = jsonObj.optJSONArray("routes")
                            if (routes != null && routes.length() > 0) {
                                val route = routes.getJSONObject(0)
                                val distanceMeters = route.optDouble("distance", 0.0)
                                val durationSecs = route.optDouble("duration", 0.0)

                                val distanceKm = Math.round((distanceMeters / 1000.0) * 10.0) / 10.0
                                val durationMins = Math.max(1, Math.round(durationSecs / 60.0).toInt())

                                if (distanceKm > 0) {
                                    return@withContext OsrmRouteInfo(distanceKm, durationMins, true, null)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "OSRM Route Fetch Error: ${e.localizedMessage}")
        }

        // Fallback calculation if OSRM service is unreachable
        val haversineKm = calculateHaversineDistance(pLat, pLng, dLat, dLng)
        if (haversineKm <= 0.0) {
            return@withContext OsrmRouteInfo(0.0, 0, false, "Location not found or route unavailable.")
        }
        val roadDistKm = Math.round((haversineKm * 1.25) * 10.0) / 10.0
        val estMins = estimateTripDurationMins(roadDistKm)
        return@withContext OsrmRouteInfo(roadDistKm, estMins, true, null)
    }

    fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = r * c
        return Math.round(dist * 10.0) / 10.0
    }

    fun estimateTripDurationMins(distanceKm: Double): Int {
        return if (distanceKm > 100) {
            // Highway travel (~60 km/h)
            Math.round(distanceKm * 1.0 + 15).toInt()
        } else {
            // City travel (~30 km/h)
            Math.round(distanceKm * 2.0 + 5).toInt().coerceAtLeast(10)
        }
    }

    suspend fun searchOpenStreetMapLocations(query: String): List<PopularPlace> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext POPULAR_PLACES

        val results = mutableListOf<PopularPlace>()

        // 1. Check direct matches in pre-mapped known Indian locations
        val knownMatches = KNOWN_INDIAN_LOCATIONS.filter {
            it.name.equals(trimmedQuery, ignoreCase = true) ||
            it.name.contains(trimmedQuery, ignoreCase = true) ||
            it.address.contains(trimmedQuery, ignoreCase = true)
        }
        results.addAll(knownMatches)

        // 2. OpenStreetMap Nominatim Live Search
        try {
            // Include ", India" if not specified to refine Indian search
            val queryWithIndia = if (!trimmedQuery.contains("India", ignoreCase = true)) {
                "$trimmedQuery, India"
            } else {
                trimmedQuery
            }
            val encodedQuery = java.net.URLEncoder.encode(queryWithIndia, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=10&countrycodes=in&addressdetails=1"

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "VTTCabsApp/1.0 (contact@vttcabs.com)")
                .addHeader("Accept-Language", "en-IN,en-US,en;q=0.9")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val array = JSONArray(body)
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val displayName = item.optString("display_name", "Location")
                            val latStr = item.optString("lat")
                            val lonStr = item.optString("lon")
                            val lat = latStr.toDoubleOrNull() ?: item.optDouble("lat", 0.0)
                            val lon = lonStr.toDoubleOrNull() ?: item.optDouble("lon", 0.0)

                            val parts = displayName.split(",")
                            val shortName = parts.firstOrNull()?.trim() ?: displayName
                            val subAddress = if (parts.size > 1) parts.drop(1).joinToString(", ").trim() else displayName

                            if (lat != 0.0 && lon != 0.0) {
                                // Avoid duplicate entries
                                if (results.none { Math.abs(it.lat - lat) < 0.005 && Math.abs(it.lng - lon) < 0.005 }) {
                                    results.add(
                                        PopularPlace(
                                            name = shortName,
                                            address = subAddress,
                                            lat = lat,
                                            lng = lon,
                                            iconName = "location_on"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fallback international search if countrycodes=in yields 0 new results
            if (results.isEmpty()) {
                val fallbackEncoded = java.net.URLEncoder.encode(trimmedQuery, "UTF-8")
                val fallbackUrl = "https://nominatim.openstreetmap.org/search?q=$fallbackEncoded&format=json&limit=10&addressdetails=1"
                val fallbackRequest = Request.Builder()
                    .url(fallbackUrl)
                    .addHeader("User-Agent", "VTTCabsApp/1.0 (contact@vttcabs.com)")
                    .build()

                client.newCall(fallbackRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            val array = JSONArray(body)
                            for (i in 0 until array.length()) {
                                val item = array.getJSONObject(i)
                                val displayName = item.optString("display_name", "Location")
                                val latStr = item.optString("lat")
                                val lonStr = item.optString("lon")
                                val lat = latStr.toDoubleOrNull() ?: item.optDouble("lat", 0.0)
                                val lon = lonStr.toDoubleOrNull() ?: item.optDouble("lon", 0.0)

                                val parts = displayName.split(",")
                                val shortName = parts.firstOrNull()?.trim() ?: displayName
                                val subAddress = if (parts.size > 1) parts.drop(1).joinToString(", ").trim() else displayName

                                if (lat != 0.0 && lon != 0.0) {
                                    if (results.none { Math.abs(it.lat - lat) < 0.005 && Math.abs(it.lng - lon) < 0.005 }) {
                                        results.add(
                                            PopularPlace(
                                                name = shortName,
                                                address = subAddress,
                                                lat = lat,
                                                lng = lon,
                                                iconName = "location_on"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "OpenStreetMap Nominatim search error: ${e.localizedMessage}")
        }

        if (results.isEmpty()) {
            val matchedPopular = POPULAR_PLACES.filter {
                it.name.contains(trimmedQuery, ignoreCase = true) || it.address.contains(trimmedQuery, ignoreCase = true)
            }
            results.addAll(matchedPopular)
        }

        return@withContext results
    }
}

