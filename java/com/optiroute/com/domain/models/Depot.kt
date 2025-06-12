package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Depot(
    val id: String = "",
    val name: String = "",
    val location: Location = Location(),
    val capacity: Double = 0.0,
    val operationalHours: String = "",
    val adminId: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long
) : Parcelable