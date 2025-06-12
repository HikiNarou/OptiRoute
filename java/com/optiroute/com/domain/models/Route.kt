package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Route(
    val id: String = "",
    val customerIds: List<String> = emptyList(),
    val vehicleId: String = "",
    val depotId: String = "",
    val kurirId: String = "",
    val optimizedPath: List<Location> = emptyList(),
    val totalDistance: Double = 0.0,
    val estimatedDuration: Long = 0L,
    val totalWeight: Double = 0.0,
    val status: RouteStatus = RouteStatus.PLANNED,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class RouteStatus {
    PLANNED,        // Rute sudah direncanakan
    IN_PROGRESS,    // Rute sedang berjalan
    COMPLETED,      // Rute selesai
    CANCELLED       // Rute dibatalkan
}