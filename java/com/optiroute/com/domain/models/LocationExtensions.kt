package com.optiroute.com.domain.models

import com.optiroute.com.utils.LocationUtils

fun Location.distanceTo(other: Location): Double {
    return LocationUtils.calculateDistance(
        this.latitude, this.longitude,
        other.latitude, other.longitude
    )
}

fun Location.bearingTo(other: Location): Double {
    return LocationUtils.calculateBearing(
        this.latitude, this.longitude,
        other.latitude, other.longitude
    )
}

fun Location.isNear(other: Location, thresholdKm: Double = 0.1): Boolean {
    return this.distanceTo(other) <= thresholdKm
}

fun Location.isLocationValid(): Boolean {
    return latitude in -90.0..90.0 && longitude in -180.0..180.0
}

fun Location.formatCoordinates(): String {
    return LocationUtils.formatCoordinates(latitude, longitude)
}

fun Location.toLatLng(): com.google.android.gms.maps.model.LatLng {
    return com.google.android.gms.maps.model.LatLng(latitude, longitude)
}

fun List<Location>.calculateTotalDistance(): Double {
    if (size < 2) return 0.0

    var totalDistance = 0.0
    for (i in 0 until size - 1) {
        totalDistance += this[i].distanceTo(this[i + 1])
    }
    return totalDistance
}

fun List<Location>.findCenter(): Location {
    if (isEmpty()) return Location(0.0, 0.0, "Empty")

    val avgLat = map { it.latitude }.average()
    val avgLng = map { it.longitude }.average()

    return Location(avgLat, avgLng, "Center")
}

fun List<Location>.getBounds(): Pair<Location, Location>? {
    if (isEmpty()) return null

    var minLat = first().latitude
    var maxLat = first().latitude
    var minLng = first().longitude
    var maxLng = first().longitude

    forEach { location ->
        minLat = minOf(minLat, location.latitude)
        maxLat = maxOf(maxLat, location.latitude)
        minLng = minOf(minLng, location.longitude)
        maxLng = maxOf(maxLng, location.longitude)
    }

    return Pair(
        Location(minLat, minLng, "Southwest"),
        Location(maxLat, maxLng, "Northeast")
    )
}

fun Location.offsetBy(latOffset: Double, lngOffset: Double): Location {
    return Location(
        latitude = latitude + latOffset,
        longitude = longitude + lngOffset,
        address = "$address (offset)"
    )
}

fun Location.isWithinBounds(southwest: Location, northeast: Location): Boolean {
    return latitude >= southwest.latitude && latitude <= northeast.latitude &&
            longitude >= southwest.longitude && longitude <= northeast.longitude
}