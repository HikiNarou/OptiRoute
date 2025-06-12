package com.optiroute.com.domain.usecase.vehicle

import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.repository.VehicleRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class CreateVehicleUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicle: Vehicle): Resource<Vehicle> {
        return when {
            vehicle.licensePlate.isBlank() -> Resource.Error("License plate cannot be empty")
            vehicle.capacity <= 0 -> Resource.Error("Vehicle capacity must be greater than 0")
            vehicle.depotId.isBlank() -> Resource.Error("Depot must be selected")
            else -> vehicleRepository.createVehicle(vehicle)
        }
    }
}