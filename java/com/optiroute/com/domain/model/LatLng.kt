package com.optiroute.com.domain.model

import android.os.Parcelable // Pastikan impor ini ada
import kotlinx.parcelize.Parcelize

/**
 * Representasi data class sederhana untuk koordinat geografis (Latitude dan Longitude).
 *
 * @property latitude Garis lintang, dalam derajat.
 * @property longitude Garis bujur, dalam derajat.
 *
 * Mengimplementasikan Parcelable agar mudah dilewatkan antar komponen Android (misalnya, antar Composable atau Activity).
 */
@Parcelize
data class LatLng(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable { // Tambahkan : Parcelable di sini
    /**
     * Memeriksa apakah koordinat valid (dalam rentang standar).
     * Latitude: -90 hingga +90
     * Longitude: -180 hingga +180
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    companion object {
        val DEFAULT = LatLng(0.0, 0.0) // Lokasi default jika tidak ada yang diatur
    }
}
