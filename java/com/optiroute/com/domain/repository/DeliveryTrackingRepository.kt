package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.DeliveryTracking
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.domain.models.Location
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DeliveryTrackingRepository {
    suspend fun createDeliveryTracking(tracking: DeliveryTracking): Resource<DeliveryTracking>
    suspend fun updateDeliveryTracking(tracking: DeliveryTracking): Resource<DeliveryTracking>
    suspend fun getDeliveryTrackingById(id: String): Resource<DeliveryTracking>
    suspend fun getDeliveryTrackingByTaskId(taskId: String): Resource<DeliveryTracking>
    fun getDeliveryTrackingByCustomer(customerId: String): Flow<List<DeliveryTracking>>
    fun getDeliveryTrackingByKurir(kurirId: String): Flow<List<DeliveryTracking>>
    fun getActiveDeliveries(): Flow<List<DeliveryTracking>>
    fun getAllDeliveryTracking(): Flow<List<DeliveryTracking>>
    suspend fun updateDeliveryStatus(trackingId: String, status: DeliveryTrackingStatus): Resource<Boolean>
    suspend fun updateDeliveryLocation(trackingId: String, location: Location): Resource<Boolean>
    suspend fun deleteDeliveryTracking(trackingId: String): Resource<Boolean>
}