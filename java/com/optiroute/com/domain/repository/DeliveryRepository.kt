package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.DeliveryTask
import com.optiroute.com.domain.models.DeliveryStatus
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun createDeliveryTask(deliveryTask: DeliveryTask): Resource<DeliveryTask>
    suspend fun updateDeliveryTask(deliveryTask: DeliveryTask): Resource<DeliveryTask>
    suspend fun getDeliveryTaskById(taskId: String): Resource<DeliveryTask>
    fun getDeliveryTasksByKurir(kurirId: String): Flow<List<DeliveryTask>>
    fun getDeliveryTasksByCustomer(customerId: String): Flow<List<DeliveryTask>>
    fun getDeliveryTasksByRoute(routeId: String): Flow<List<DeliveryTask>>
    fun getAllDeliveryTasks(): Flow<List<DeliveryTask>>
    suspend fun updateDeliveryStatus(taskId: String, status: DeliveryStatus): Resource<Boolean>
    suspend fun completeDelivery(taskId: String, notes: String): Resource<Boolean>
    suspend fun failDelivery(taskId: String, reason: String): Resource<Boolean>
    suspend fun deleteDeliveryTask(taskId: String): Resource<Boolean>
}