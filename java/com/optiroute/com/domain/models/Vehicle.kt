package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val id: String = "",
    val licensePlate: String = "",
    val vehicleType: VehicleType = VehicleType.VAN,
    val capacity: Double = 0.0,
    val capacityUnit: String = "kg",
    val fuelConsumption: Double = 0.0,
    val depotId: String = "",
    val kurirId: String = "",
    val isAvailable: Boolean = true,
    val maintenanceStatus: MaintenanceStatus = MaintenanceStatus.OPERATIONAL,
    val lastServiceDate: Long = 0L,
    val nextServiceDate: Long = 0L,
    val currentMileage: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class VehicleType {
    MOTORCYCLE,     // Motor
    CAR,           // Mobil
    VAN,           // Van
    TRUCK,         // Truk
    PICKUP         // Pickup
}

enum class MaintenanceStatus {
    OPERATIONAL,    // Beroperasi normal
    MAINTENANCE,    // Sedang maintenance
    REPAIR,         // Sedang diperbaiki
    OUT_OF_SERVICE  // Tidak dapat digunakan
}