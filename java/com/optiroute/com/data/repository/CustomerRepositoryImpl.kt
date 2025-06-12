package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.CustomerDao
import com.optiroute.com.data.local.entities.CustomerEntity
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override suspend fun createCustomer(customer: Customer): Resource<Customer> {
        return try {
            val entity = CustomerEntity.fromDomainModel(
                customer.copy(id = generateId())
            )
            customerDao.insertCustomer(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create customer: ${e.message}")
        }
    }

    override suspend fun updateCustomer(customer: Customer): Resource<Customer> {
        return try {
            val entity = CustomerEntity.fromDomainModel(
                customer.copy(updatedAt = System.currentTimeMillis())
            )
            customerDao.updateCustomer(entity)
            Resource.Success(customer)
        } catch (e: Exception) {
            Resource.Error("Failed to update customer: ${e.message}")
        }
    }

    override suspend fun deleteCustomer(customerId: String): Resource<Boolean> {
        return try {
            customerDao.deleteCustomerById(customerId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete customer: ${e.message}")
        }
    }

    override suspend fun getCustomerById(customerId: String): Resource<Customer> {
        return try {
            val entity = customerDao.getCustomerById(customerId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Customer not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get customer: ${e.message}")
        }
    }

    override fun getCustomersByUmkm(umkmId: String): Flow<List<Customer>> {
        return customerDao.getCustomersByUmkm(umkmId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCustomersByStatus(status: CustomerStatus): Flow<List<Customer>> {
        return customerDao.getCustomersByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCustomersByKurir(kurirId: String): Flow<List<Customer>> {
        return customerDao.getCustomersByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateCustomerStatus(customerId: String, status: CustomerStatus): Resource<Boolean> {
        return try {
            customerDao.updateCustomerStatus(customerId, status.name, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update customer status: ${e.message}")
        }
    }

    override suspend fun assignCustomerToKurir(customerId: String, kurirId: String): Resource<Boolean> {
        return try {
            customerDao.assignCustomerToKurir(customerId, kurirId, System.currentTimeMillis())
            updateCustomerStatus(customerId, CustomerStatus.ASSIGNED)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to assign customer to kurir: ${e.message}")
        }
    }

    override suspend fun unassignCustomerFromKurir(customerId: String): Resource<Boolean> {
        return try {
            customerDao.unassignCustomerFromKurir(customerId, System.currentTimeMillis())
            updateCustomerStatus(customerId, CustomerStatus.PENDING)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to unassign customer from kurir: ${e.message}")
        }
    }

    override suspend fun getCustomersCount(): Int {
        return try {
            customerDao.getCustomersCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getActiveCustomersCount(): Int {
        return try {
            customerDao.getActiveCustomersCount()
        } catch (e: Exception) {
            0
        }
    }
}