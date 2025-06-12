package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location

@Entity(tableName = "depots")
data class DepotEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val capacity: Double,
    val operationalHours: String,
    val adminId: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): Depot {
        return Depot(
            id = id,
            name = name,
            location = Location(latitude, longitude, address),
            capacity = capacity,
            operationalHours = operationalHours,
            adminId = adminId,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(depot: Depot): DepotEntity {
            return DepotEntity(
                id = depot.id,
                name = depot.name,
                latitude = depot.location.latitude,
                longitude = depot.location.longitude,
                address = depot.location.address,
                capacity = depot.capacity,
                operationalHours = depot.operationalHours,
                adminId = depot.adminId,
                isActive = depot.isActive,
                createdAt = depot.createdAt,
                updatedAt = depot.updatedAt
            )
        }
    }
}