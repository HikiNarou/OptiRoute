package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.DeliveryTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryTaskDao {

    @Query("SELECT * FROM delivery_tasks WHERE id = :id")
    suspend fun getDeliveryTaskById(id: String): DeliveryTaskEntity?

    @Query("SELECT * FROM delivery_tasks WHERE kurirId = :kurirId ORDER BY createdAt DESC")
    fun getDeliveryTasksByKurir(kurirId: String): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getDeliveryTasksByCustomer(customerId: String): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks WHERE routeId = :routeId ORDER BY createdAt DESC")
    fun getDeliveryTasksByRoute(routeId: String): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getDeliveryTasksByStatus(status: String): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks ORDER BY createdAt DESC")
    fun getAllDeliveryTasks(): Flow<List<DeliveryTaskEntity>>

    @Query("SELECT * FROM delivery_tasks WHERE status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT') ORDER BY createdAt DESC")
    fun getActiveDeliveryTasks(): Flow<List<DeliveryTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryTask(deliveryTask: DeliveryTaskEntity)

    @Update
    suspend fun updateDeliveryTask(deliveryTask: DeliveryTaskEntity)

    @Delete
    suspend fun deleteDeliveryTask(deliveryTask: DeliveryTaskEntity)

    @Query("DELETE FROM delivery_tasks WHERE id = :taskId")
    suspend fun deleteDeliveryTaskById(taskId: String)

    @Query("UPDATE delivery_tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateDeliveryStatus(taskId: String, status: String, updatedAt: Long)

    @Query("UPDATE delivery_tasks SET status = 'DELIVERED', notes = :notes, actualDeliveryTime = :actualTime, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun completeDelivery(taskId: String, notes: String, actualTime: Long, updatedAt: Long)

    @Query("UPDATE delivery_tasks SET status = 'FAILED', notes = :reason, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun failDelivery(taskId: String, reason: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM delivery_tasks")
    suspend fun getDeliveryTasksCount(): Int

    @Query("SELECT COUNT(*) FROM delivery_tasks WHERE status = :status")
    suspend fun getDeliveryTasksCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM delivery_tasks WHERE kurirId = :kurirId AND status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')")
    suspend fun getActiveDeliveryTasksCountByKurir(kurirId: String): Int
}