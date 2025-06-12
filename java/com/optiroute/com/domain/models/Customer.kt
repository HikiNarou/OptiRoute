package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    val id: String = "",
    val name: String = "",
    val location: Location = Location(),
    val phoneNumber: String = "",
    val email: String = "",
    val itemType: String = "",
    val itemWeight: Double = 0.0,
    val weightUnit: String = "kg",
    val notes: String = "",
    val priority: CustomerPriority = CustomerPriority.NORMAL,
    val umkmId: String = "",
    val status: CustomerStatus = CustomerStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class CustomerPriority {
    LOW, NORMAL, HIGH, URGENT
}

enum class CustomerStatus {
    PENDING, ASSIGNED, IN_DELIVERY, DELIVERED, CANCELLED
}