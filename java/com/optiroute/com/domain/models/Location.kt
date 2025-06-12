package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
) : Parcelable {

    fun distanceTo(other: Location): Double {
        val earthRadius = 6371 // km
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLng = Math.toRadians(other.longitude - longitude)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(latitude)) * kotlin.math.cos(Math.toRadians(other.latitude)) *
                kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
}