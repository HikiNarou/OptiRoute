package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.CustomerDao
import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi konkret dari CustomerRepository.
 *
 * @param customerDao Instance dari CustomerDao yang diinjeksi oleh Hilt.
 */
@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao
) : CustomerRepository {

    override fun getAllCustomers(): Flow<List<CustomerEntity>> {
        Timber.d("CustomerRepository: Getting all customers from DAO.")
        return customerDao.getAllCustomers()
    }

    override fun getCustomerById(customerId: Int): Flow<CustomerEntity?> {
        Timber.d("CustomerRepository: Getting customer by ID %d from DAO.", customerId)
        return customerDao.getCustomerById(customerId)
    }

    override suspend fun addCustomer(customer: CustomerEntity): AppResult<Long> = withContext(Dispatchers.IO) {
        try {
            // Validasi di Entity constructor sudah menangani name dan demand.
            // Pastikan lokasi juga valid
            if (!customer.location.isValid()) {
                Timber.w("CustomerRepository: Add customer failed - location is invalid for %s.", customer.name)
                return@withContext AppResult.Error(IllegalArgumentException("Lokasi pelanggan tidak valid."))
            }

            val newId = customerDao.insertCustomer(customer)
            if (newId > 0) {
                Timber.i("CustomerRepository: Customer added successfully with ID %d: %s", newId, customer.name)
                AppResult.Success(newId)
            } else {
                Timber.w("CustomerRepository: Failed to add customer, DAO returned ID %d.", newId)
                AppResult.Error(Exception("Gagal menambahkan pelanggan, ID tidak valid."), "Gagal menambahkan pelanggan ke database.")
            }
        } catch (e: Exception) {
            Timber.e(e, "CustomerRepository: Error adding customer %s.", customer.name)
            AppResult.Error(e, "Gagal menambahkan pelanggan: ${e.message}")
        }
    }

    override suspend fun updateCustomer(customer: CustomerEntity): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validasi di Entity constructor sudah menangani name dan demand.
            if (!customer.location.isValid()) {
                Timber.w("CustomerRepository: Update customer failed - location is invalid for %s.", customer.name)
                return@withContext AppResult.Error(IllegalArgumentException("Lokasi pelanggan tidak valid."))
            }
            customerDao.updateCustomer(customer)
            Timber.i("CustomerRepository: Customer updated successfully: ID %d, %s", customer.id, customer.name)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "CustomerRepository: Error updating customer %s.", customer.name)
            AppResult.Error(e, "Gagal memperbarui pelanggan: ${e.message}")
        }
    }

    override suspend fun deleteCustomer(customer: CustomerEntity): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            customerDao.deleteCustomer(customer)
            Timber.i("CustomerRepository: Customer deleted successfully: ID %d, %s", customer.id, customer.name)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "CustomerRepository: Error deleting customer %s.", customer.name)
            AppResult.Error(e, "Gagal menghapus pelanggan: ${e.message}")
        }
    }

    override fun getCustomersCount(): Flow<Int> {
        Timber.d("CustomerRepository: Getting customers count from DAO.")
        return customerDao.getCustomersCount()
    }

    override suspend fun clearAllCustomers(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            customerDao.clearAllCustomers()
            Timber.i("CustomerRepository: All customers cleared successfully.")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "CustomerRepository: Error clearing all customers.")
            AppResult.Error(e, "Gagal membersihkan data pelanggan: ${e.message}")
        }
    }

    override fun getCustomersByIds(customerIds: List<Int>): Flow<List<CustomerEntity>> {
        Timber.d("CustomerRepository: Getting customers by IDs from DAO: %s", customerIds.joinToString())
        return customerDao.getCustomersByIds(customerIds)
    }
}
