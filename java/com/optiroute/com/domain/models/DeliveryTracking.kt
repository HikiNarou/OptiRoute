package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryTracking(
    val id: String = "",
    val deliveryTaskId: String = "",
    val customerId: String = "",
    val kurirId: String = "",
    val currentLocation: Location = Location(),
    val status: DeliveryTrackingStatus = DeliveryTrackingStatus.PREPARING,
    val estimatedArrival: Long = 0L,
    val actualArrival: Long = 0L,
    val statusHistory: List<DeliveryStatusUpdate> = emptyList(),
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class DeliveryStatusUpdate(
    val id: String = "",
    val status: DeliveryTrackingStatus = DeliveryTrackingStatus.PREPARING,
    val location: Location = Location(),
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val imageUrl: String = ""
) : Parcelable

enum class DeliveryTrackingStatus {
    PREPARING,        // Sedang mempersiapkan
    PICKED_UP,        // Barang telah diambil
    IN_TRANSIT,       // Dalam perjalanan
    NEAR_DESTINATION, // Mendekati tujuan
    ARRIVED,          // Tiba di tujuan
    DELIVERED,        // Berhasil dikirim
    FAILED,           // Gagal kirim
    RETURNED          // Dikembalikan
}