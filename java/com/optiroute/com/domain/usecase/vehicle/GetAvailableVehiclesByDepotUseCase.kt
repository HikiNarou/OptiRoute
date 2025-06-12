package com.optiroute.com.domain.usecase.vehicle

import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAvailableVehiclesByDepotUseCase @Inject constructor(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(depotId: String): Flow<List<Vehicle>> {
        return vehicleRepository.getAvailableVehiclesByDepot(depotId)
    }
}