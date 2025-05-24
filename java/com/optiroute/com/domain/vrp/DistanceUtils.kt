package com.optiroute.com.domain.vrp

import com.optiroute.com.domain.model.LatLng
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.atan2

object DistanceUtils {
    private const val EARTH_RADIUS_KM = 6371.0 // Radius rata-rata Bumi dalam kilometer

    /**
     * Menghitung jarak antara dua titik koordinat LatLng menggunakan formula Haversine.
     *
     * @param point1 Titik pertama.
     * @param point2 Titik kedua.
     * @return Jarak dalam kilometer.
     */
    fun calculateHaversineDistance(point1: LatLng, point2: LatLng): Double {
        if (!point1.isValid() || !point2.isValid()) return Double.MAX_VALUE // Handle invalid coords

        val lat1Rad = Math.toRadians(point1.latitude)
        val lon1Rad = Math.toRadians(point1.longitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val lon2Rad = Math.toRadians(point2.longitude)

        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    /**
     * Menghitung jarak Euclidean (untuk area geografis terbatas seperti yang disebutkan dalam PDF).
     * Ini lebih sederhana tetapi kurang akurat untuk jarak jauh atau area yang luas.
     * Asumsikan proyeksi sederhana di mana 1 derajat lintang/bujur ~ 111km (perkiraan kasar).
     *
     * @param point1 Titik pertama.
     * @param point2 Titik kedua.
     * @return Jarak perkiraan dalam kilometer.
     */
    fun calculateEuclideanDistanceApproximation(point1: LatLng, point2: LatLng): Double {
        if (!point1.isValid() || !point2.isValid()) return Double.MAX_VALUE

        // Faktor konversi kasar: 1 derajat ~ 111 km
        // Ini sangat tidak akurat untuk bujur karena jarak antar meridian menyempit ke kutub.
        // Untuk area terbatas dan dekat ekuator, mungkin bisa diterima sebagai aproksimasi kasar.
        // Lebih baik gunakan Haversine jika memungkinkan.
        val degToKm = 111.0
        val dx = (point1.longitude - point2.longitude) * cos(Math.toRadians((point1.latitude + point2.latitude) / 2.0))
        val dy = (point1.latitude - point2.latitude)
        return sqrt(dx.pow(2) + dy.pow(2)) * degToKm
    }

    // Sesuai PDF, kita menggunakan koordinat GPS statis. Haversine lebih cocok.
    val calculateDistance: (LatLng, LatLng) -> Double = ::calculateHaversineDistance
}
