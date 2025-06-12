package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun createCustomer(customer: Customer): Resource<Customer>
    suspend fun updateCustomer(customer: Customer): Resource<Customer>
    suspend fun deleteCustomer(customerId: String): Resource<Boolean>
    suspend fun getCustomerById(customerId: String): Resource<Customer>
    fun getCustomersByUmkm(umkmId: String): Flow<List<Customer>>
    fun getCustomersByStatus(status: CustomerStatus): Flow<List<Customer>>
    fun getCustomersByKurir(kurirId: String): Flow<List<Customer>>
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun updateCustomerStatus(customerId: String, status: CustomerStatus): Resource<Boolean>
    suspend fun assignCustomerToKurir(customerId: String, kurirId: String): Resource<Boolean>
    suspend fun unassignCustomerFromKurir(customerId: String): Resource<Boolean>
    suspend fun getCustomersCount(): Int
    suspend fun getActiveCustomersCount(): Int
}