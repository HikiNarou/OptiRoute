package com.optiroute.com.domain.usecase.customer

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class CreateCustomerUseCase @Inject constructor(
    private val customerRepository: CustomerRepository
) {
    suspend operator fun invoke(customer: Customer): Resource<Customer> {
        return when {
            customer.name.isBlank() -> Resource.Error("Customer name cannot be empty")
            customer.location.address.isBlank() -> Resource.Error("Customer address cannot be empty")
            customer.itemType.isBlank() -> Resource.Error("Item type cannot be empty")
            customer.itemWeight <= 0 -> Resource.Error("Item weight must be greater than 0")
            else -> customerRepository.createCustomer(customer)
        }
    }
}