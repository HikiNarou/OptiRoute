package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.models.VehicleType
import com.optiroute.com.domain.models.MaintenanceStatus

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String,
    val licensePlate: String,
    val vehicleType: String,
    val capacity: Double,
    val capacityUnit: String,
    val fuelConsumption: Double,
    val depotId: String,
    val kurirId: String,
    val isAvailable: Boolean,
    val maintenanceStatus: String,
    val lastServiceDate: Long,
    val nextServiceDate: Long,
    val currentMileage: Double,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): Vehicle {
        return Vehicle(
            id = id,
            licensePlate = licensePlate,
            vehicleType = VehicleType.valueOf(vehicleType),
            capacity = capacity,
            capacityUnit = capacityUnit,
            fuelConsumption = fuelConsumption,
            depotId = depotId,
            kurirId = kurirId,
            isAvailable = isAvailable,
            maintenanceStatus = MaintenanceStatus.valueOf(maintenanceStatus),
            lastServiceDate = lastServiceDate,
            nextServiceDate = nextServiceDate,
            currentMileage = currentMileage,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(vehicle: Vehicle): VehicleEntity {
            return VehicleEntity(
                id = vehicle.id,
                licensePlate = vehicle.licensePlate,
                vehicleType = vehicle.vehicleType.name,
                capacity = vehicle.capacity,
                capacityUnit = vehicle.capacityUnit,
                fuelConsumption = vehicle.fuelConsumption,
                depotId = vehicle.depotId,
                kurirId = vehicle.kurirId,
                isAvailable = vehicle.isAvailable,
                maintenanceStatus = vehicle.maintenanceStatus.name,
                lastServiceDate = vehicle.lastServiceDate,
                nextServiceDate = vehicle.nextServiceDate,
                currentMileage = vehicle.currentMileage,
                createdAt = vehicle.createdAt,
                updatedAt = vehicle.updatedAt
            )
        }
    }
}