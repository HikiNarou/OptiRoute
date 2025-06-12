package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val userType: UserType = UserType.UMKM,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class UserType {
    UMKM, ADMIN, KURIR
}