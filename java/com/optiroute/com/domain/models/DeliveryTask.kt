package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeliveryTask(
    val id: String = "",
    val customerId: String = "",
    val kurirId: String = "",
    val routeId: String = "",
    val status: DeliveryStatus = DeliveryStatus.ASSIGNED,
    val notes: String = "",
    val estimatedDeliveryTime: Long = 0L,
    val actualDeliveryTime: Long = 0L,
    val assignedAt: Long = System.currentTimeMillis(),
    val pickedUpAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class DeliveryStatus {
    ASSIGNED,       // Ditugaskan ke kurir
    PICKED_UP,      // Barang sudah diambil
    IN_TRANSIT,     // Dalam perjalanan
    DELIVERED,      // Berhasil dikirim
    FAILED,         // Gagal kirim
    CANCELLED       // Dibatalkan
}