package com.optiroute.com.domain.usecase.customer

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCustomersByUmkmUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    operator fun invoke(umkmId: String): Flow<List<Customer>> {
        return customerRepository.getCustomersByUmkm(umkmId)
    }
}