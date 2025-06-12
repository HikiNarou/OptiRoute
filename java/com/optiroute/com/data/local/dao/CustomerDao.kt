package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE umkmId = :umkmId ORDER BY createdAt DESC")
    fun getCustomersByUmkm(umkmId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE status = :status ORDER BY priority DESC, createdAt ASC")
    fun getCustomersByStatus(status: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE kurirId = :kurirId ORDER BY createdAt DESC")
    fun getCustomersByKurir(kurirId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE priority = :priority ORDER BY createdAt DESC")
    fun getCustomersByPriority(priority: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)

    @Query("UPDATE customers SET status = :status, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun updateCustomerStatus(customerId: String, status: String, updatedAt: Long)

    @Query("UPDATE customers SET kurirId = :kurirId, status = 'ASSIGNED', updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun assignCustomerToKurir(customerId: String, kurirId: String, updatedAt: Long)

    @Query("UPDATE customers SET kurirId = '', status = 'PENDING', updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun unassignCustomerFromKurir(customerId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomersCount(): Int

    @Query("SELECT COUNT(*) FROM customers WHERE status NOT IN ('DELIVERED', 'CANCELLED')")
    suspend fun getActiveCustomersCount(): Int

    @Query("SELECT COUNT(*) FROM customers WHERE status = :status")
    suspend fun getCustomersCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM customers WHERE kurirId = :kurirId AND status = 'ASSIGNED'")
    suspend fun getAssignedCustomersCountByKurir(kurirId: String): Int

    @Query("SELECT * FROM customers WHERE itemType LIKE '%' || :itemType || '%'")
    fun searchCustomersByItemType(itemType: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :name || '%' OR address LIKE '%' || :name || '%'")
    fun searchCustomersByName(name: String): Flow<List<CustomerEntity>>
}