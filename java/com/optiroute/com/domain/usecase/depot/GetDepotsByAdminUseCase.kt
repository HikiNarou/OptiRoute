package com.optiroute.com.domain.usecase.depot

import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.repository.DepotRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDepotsByAdminUseCase @Inject constructor(
    private val depotRepository: DepotRepository
) {
    operator fun invoke(adminId: String): Flow<List<Depot>> {
        return depotRepository.getDepotsByAdmin(adminId)
    }
}