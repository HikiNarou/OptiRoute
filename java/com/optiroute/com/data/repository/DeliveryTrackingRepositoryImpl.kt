package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.DeliveryTrackingDao
import com.optiroute.com.data.local.entities.DeliveryTrackingEntity
import com.optiroute.com.domain.models.DeliveryTracking
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.repository.DeliveryTrackingRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliveryTrackingRepositoryImpl @Inject constructor(
    private val deliveryTrackingDao: DeliveryTrackingDao
) : DeliveryTrackingRepository {

    override suspend fun createDeliveryTracking(tracking: DeliveryTracking): Resource<DeliveryTracking> {
        return try {
            val entity = DeliveryTrackingEntity.fromDomainModel(
                tracking.copy(id = generateId())
            )
            deliveryTrackingDao.insertDeliveryTracking(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create delivery tracking: ${e.message}")
        }
    }

    override suspend fun updateDeliveryTracking(tracking: DeliveryTracking): Resource<DeliveryTracking> {
        return try {
            val entity = DeliveryTrackingEntity.fromDomainModel(
                tracking.copy(lastUpdated = System.currentTimeMillis())
            )
            deliveryTrackingDao.updateDeliveryTracking(entity)
            Resource.Success(tracking)
        } catch (e: Exception) {
            Resource.Error("Failed to update delivery tracking: ${e.message}")
        }
    }

    override suspend fun getDeliveryTrackingById(id: String): Resource<DeliveryTracking> {
        return try {
            val entity = deliveryTrackingDao.getDeliveryTrackingById(id)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Delivery tracking not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get delivery tracking: ${e.message}")
        }
    }

    override suspend fun getDeliveryTrackingByTaskId(taskId: String): Resource<DeliveryTracking> {
        return try {
            val entity = deliveryTrackingDao.getDeliveryTrackingByTaskId(taskId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Delivery tracking not found for task")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get delivery tracking: ${e.message}")
        }
    }

    override fun getDeliveryTrackingByCustomer(customerId: String): Flow<List<DeliveryTracking>> {
        return deliveryTrackingDao.getDeliveryTrackingByCustomer(customerId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDeliveryTrackingByKurir(kurirId: String): Flow<List<DeliveryTracking>> {
        return deliveryTrackingDao.getDeliveryTrackingByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveDeliveries(): Flow<List<DeliveryTracking>> {
        return deliveryTrackingDao.getActiveDeliveries().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllDeliveryTracking(): Flow<List<DeliveryTracking>> {
        return deliveryTrackingDao.getAllDeliveryTracking().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateDeliveryStatus(trackingId: String, status: DeliveryTrackingStatus): Resource<Boolean> {
        return try {
            deliveryTrackingDao.updateDeliveryStatus(
                id = trackingId,
                status = status.name,
                lastUpdated = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update delivery status: ${e.message}")
        }
    }

    override suspend fun updateDeliveryLocation(trackingId: String, location: Location): Resource<Boolean> {
        return try {
            deliveryTrackingDao.updateDeliveryLocation(
                id = trackingId,
                latitude = location.latitude,
                longitude = location.longitude,
                address = location.address,
                lastUpdated = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update delivery location: ${e.message}")
        }
    }

    override suspend fun deleteDeliveryTracking(trackingId: String): Resource<Boolean> {
        return try {
            val entity = deliveryTrackingDao.getDeliveryTrackingById(trackingId)
            if (entity != null) {
                deliveryTrackingDao.deleteDeliveryTracking(entity)
                Resource.Success(true)
            } else {
                Resource.Error("Delivery tracking not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to delete delivery tracking: ${e.message}")
        }
    }
}