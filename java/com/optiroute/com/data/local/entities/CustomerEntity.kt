package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.CustomerPriority
import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.domain.models.Location

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String,
    val email: String,
    val itemType: String,
    val itemWeight: Double,
    val weightUnit: String,
    val notes: String,
    val priority: String,
    val umkmId: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val kurirId: String
) {
    fun toDomainModel(): Customer {
        return Customer(
            id = id,
            name = name,
            location = Location(latitude, longitude, address),
            phoneNumber = phoneNumber,
            email = email,
            itemType = itemType,
            itemWeight = itemWeight,
            weightUnit = weightUnit,
            notes = notes,
            priority = CustomerPriority.valueOf(priority),
            umkmId = umkmId,
            status = CustomerStatus.valueOf(status),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(customer: Customer): CustomerEntity {
            return CustomerEntity(
                id = customer.id,
                name = customer.name,
                latitude = customer.location.latitude,
                longitude = customer.location.longitude,
                address = customer.location.address,
                phoneNumber = customer.phoneNumber,
                email = customer.email,
                itemType = customer.itemType,
                itemWeight = customer.itemWeight,
                weightUnit = customer.weightUnit,
                notes = customer.notes,
                priority = customer.priority.name,
                umkmId = customer.umkmId,
                status = customer.status.name,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt,
                kurirId = ""
            )
        }
    }
}