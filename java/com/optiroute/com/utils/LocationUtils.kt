package com.optiroute.com.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.optiroute.com.domain.models.Location
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

object LocationUtils {

    fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth radius in kilometers

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10.0 -> "${"%.1f".format(distanceKm)} km"
            else -> "${distanceKm.toInt()} km"
        }
    }

    fun formatDuration(durationMs: Long): String {
        val hours = durationMs / (1000 * 60 * 60)
        val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    suspend fun getCurrentLocation(context: Context): Location {
        return suspendCancellableCoroutine { continuation ->
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasLocationPermission) {
                continuation.resumeWithException(
                    SecurityException("Location permission not granted")
                )
                return@suspendCancellableCoroutine
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val address = getAddressFromCoordinates(
                            context,
                            location.latitude,
                            location.longitude
                        )

                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = address
                        )

                        continuation.resume(userLocation)
                    } else {
                        continuation.resumeWithException(
                            RuntimeException("Location not available")
                        )
                    }
                }.addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
            } catch (e: SecurityException) {
                continuation.resumeWithException(e)
            }
        }
    }

    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.thoroughfare?.let { append("$it, ") }
                    address.subLocality?.let { append("$it, ") }
                    address.locality?.let { append("$it, ") }
                    address.adminArea?.let { append(it) }
                }
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}"
        }
    }

    fun calculateBearing(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val dLng = Math.toRadians(lng2 - lng1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLng) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLng)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    fun isLocationNearDestination(
        currentLat: Double, currentLng: Double,
        destLat: Double, destLng: Double,
        thresholdMeters: Double = 50.0
    ): Boolean {
        val distance = calculateDistance(currentLat, currentLng, destLat, destLng)
        return distance * 1000 <= thresholdMeters // Convert km to meters
    }

    fun calculateETA(
        currentLat: Double, currentLng: Double,
        destLat: Double, destLng: Double,
        averageSpeedKmh: Double = 40.0
    ): Long {
        val distance = calculateDistance(currentLat, currentLng, destLat, destLng)
        val timeHours = distance / averageSpeedKmh
        return (timeHours * 60 * 60 * 1000).toLong() // Convert to milliseconds
    }

    fun generateWaypoints(
        start: Location,
        end: Location,
        numberOfWaypoints: Int = 5
    ): List<Location> {
        val waypoints = mutableListOf<Location>()

        for (i in 1 until numberOfWaypoints) {
            val ratio = i.toDouble() / numberOfWaypoints
            val lat = start.latitude + (end.latitude - start.latitude) * ratio
            val lng = start.longitude + (end.longitude - start.longitude) * ratio

            waypoints.add(Location(lat, lng, "Waypoint $i"))
        }

        return waypoints
    }

    fun validateCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    fun formatCoordinates(latitude: Double, longitude: Double): String {
        return "${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}"
    }

    fun parseCoordinates(coordinateString: String): Pair<Double, Double>? {
        return try {
            val parts = coordinateString.split(",").map { it.trim() }
            if (parts.size == 2) {
                val lat = parts[0].toDouble()
                val lng = parts[1].toDouble()
                if (validateCoordinates(lat, lng)) {
                    Pair(lat, lng)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}