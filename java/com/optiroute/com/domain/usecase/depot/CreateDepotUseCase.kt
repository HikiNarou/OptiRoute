package com.optiroute.com.domain.usecase.depot

import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class CreateDepotUseCase @Inject constructor(
    private val depotRepository: DepotRepository
) {
    suspend operator fun invoke(depot: Depot): Resource<Depot> {
        return when {
            depot.name.isBlank() -> Resource.Error("Depot name cannot be empty")
            depot.location.address.isBlank() -> Resource.Error("Depot address cannot be empty")
            depot.capacity <= 0 -> Resource.Error("Depot capacity must be greater than 0")
            else -> depotRepository.createDepot(depot)
        }
    }
}