package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.DeliveryTaskDao
import com.optiroute.com.data.local.entities.DeliveryTaskEntity
import com.optiroute.com.domain.models.DeliveryTask
import com.optiroute.com.domain.models.DeliveryStatus
import com.optiroute.com.domain.repository.DeliveryRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryRepositoryImpl @Inject constructor(
    private val deliveryTaskDao: DeliveryTaskDao
) : DeliveryRepository {

    override suspend fun createDeliveryTask(deliveryTask: DeliveryTask): Resource<DeliveryTask> {
        return try {
            val entity = DeliveryTaskEntity.fromDomainModel(
                deliveryTask.copy(id = generateId())
            )
            deliveryTaskDao.insertDeliveryTask(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create delivery task: ${e.message}")
        }
    }

    override suspend fun updateDeliveryTask(deliveryTask: DeliveryTask): Resource<DeliveryTask> {
        return try {
            val entity = DeliveryTaskEntity.fromDomainModel(
                deliveryTask.copy(updatedAt = System.currentTimeMillis())
            )
            deliveryTaskDao.updateDeliveryTask(entity)
            Resource.Success(deliveryTask)
        } catch (e: Exception) {
            Resource.Error("Failed to update delivery task: ${e.message}")
        }
    }

    override suspend fun getDeliveryTaskById(taskId: String): Resource<DeliveryTask> {
        return try {
            val entity = deliveryTaskDao.getDeliveryTaskById(taskId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Delivery task not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get delivery task: ${e.message}")
        }
    }

    override fun getDeliveryTasksByKurir(kurirId: String): Flow<List<DeliveryTask>> {
        return deliveryTaskDao.getDeliveryTasksByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDeliveryTasksByCustomer(customerId: String): Flow<List<DeliveryTask>> {
        return deliveryTaskDao.getDeliveryTasksByCustomer(customerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDeliveryTasksByRoute(routeId: String): Flow<List<DeliveryTask>> {
        return deliveryTaskDao.getDeliveryTasksByRoute(routeId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllDeliveryTasks(): Flow<List<DeliveryTask>> {
        return deliveryTaskDao.getAllDeliveryTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateDeliveryStatus(taskId: String, status: DeliveryStatus): Resource<Boolean> {
        return try {
            deliveryTaskDao.updateDeliveryStatus(taskId, status.name, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update delivery status: ${e.message}")
        }
    }

    override suspend fun completeDelivery(taskId: String, notes: String): Resource<Boolean> {
        return try {
            val currentTime = System.currentTimeMillis()
            deliveryTaskDao.completeDelivery(taskId, notes, currentTime, currentTime)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to complete delivery: ${e.message}")
        }
    }

    override suspend fun failDelivery(taskId: String, reason: String): Resource<Boolean> {
        return try {
            deliveryTaskDao.failDelivery(taskId, reason, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to mark delivery as failed: ${e.message}")
        }
    }

    override suspend fun deleteDeliveryTask(taskId: String): Resource<Boolean> {
        return try {
            deliveryTaskDao.deleteDeliveryTaskById(taskId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete delivery task: ${e.message}")
        }
    }
}