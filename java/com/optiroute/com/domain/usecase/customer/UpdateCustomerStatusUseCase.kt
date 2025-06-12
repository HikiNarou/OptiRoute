package com.optiroute.com.domain.usecase.customer

import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class UpdateCustomerStatusUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(customerId: String, status: CustomerStatus): Resource<Boolean> {
        return when {
            customerId.isBlank() -> Resource.Error("Customer ID cannot be empty")
            else -> customerRepository.updateCustomerStatus(customerId, status)
        }
    }
}