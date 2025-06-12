package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.DeliveryTrackingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryTrackingDao {

    @Query("SELECT * FROM delivery_tracking WHERE id = :id")
    suspend fun getDeliveryTrackingById(id: String): DeliveryTrackingEntity?

    @Query("SELECT * FROM delivery_tracking WHERE deliveryTaskId = :taskId")
    suspend fun getDeliveryTrackingByTaskId(taskId: String): DeliveryTrackingEntity?

    @Query("SELECT * FROM delivery_tracking WHERE customerId = :customerId ORDER BY lastUpdated DESC")
    fun getDeliveryTrackingByCustomer(customerId: String): Flow<List<DeliveryTrackingEntity>>

    @Query("SELECT * FROM delivery_tracking WHERE kurirId = :kurirId ORDER BY lastUpdated DESC")
    fun getDeliveryTrackingByKurir(kurirId: String): Flow<List<DeliveryTrackingEntity>>

    @Query("SELECT * FROM delivery_tracking WHERE status IN ('IN_TRANSIT', 'NEAR_DESTINATION') ORDER BY lastUpdated DESC")
    fun getActiveDeliveries(): Flow<List<DeliveryTrackingEntity>>

    @Query("SELECT * FROM delivery_tracking ORDER BY lastUpdated DESC")
    fun getAllDeliveryTracking(): Flow<List<DeliveryTrackingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryTracking(tracking: DeliveryTrackingEntity)

    @Update
    suspend fun updateDeliveryTracking(tracking: DeliveryTrackingEntity)

    @Query("UPDATE delivery_tracking SET status = :status, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateDeliveryStatus(id: String, status: String, lastUpdated: Long)

    @Query("UPDATE delivery_tracking SET currentLatitude = :latitude, currentLongitude = :longitude, currentAddress = :address, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateDeliveryLocation(id: String, latitude: Double, longitude: Double, address: String, lastUpdated: Long)

    @Delete
    suspend fun deleteDeliveryTracking(tracking: DeliveryTrackingEntity)
}