package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.optiroute.com.domain.models.DeliveryStatusUpdate
import com.optiroute.com.domain.models.DeliveryTracking
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.domain.models.Location

@TypeConverters(DeliveryTrackingConverters::class)
@Entity(tableName = "delivery_tracking")
data class DeliveryTrackingEntity(
    @PrimaryKey
    val id: String,
    val deliveryTaskId: String,
    val customerId: String,
    val kurirId: String,
    val currentLatitude: Double,
    val currentLongitude: Double,
    val currentAddress: String,
    val status: String,
    val estimatedArrival: Long,
    val actualArrival: Long,
    val statusHistory: List<DeliveryStatusUpdate>,
    val notes: String,
    val lastUpdated: Long,
    val createdAt: Long
) {
    fun toDomainModel(): DeliveryTracking {
        return DeliveryTracking(
            id = id,
            deliveryTaskId = deliveryTaskId,
            customerId = customerId,
            kurirId = kurirId,
            currentLocation = Location(currentLatitude, currentLongitude, currentAddress),
            status = DeliveryTrackingStatus.valueOf(status),
            estimatedArrival = estimatedArrival,
            actualArrival = actualArrival,
            statusHistory = statusHistory,
            notes = notes,
            lastUpdated = lastUpdated,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(tracking: DeliveryTracking): DeliveryTrackingEntity {
            return DeliveryTrackingEntity(
                id = tracking.id,
                deliveryTaskId = tracking.deliveryTaskId,
                customerId = tracking.customerId,
                kurirId = tracking.kurirId,
                currentLatitude = tracking.currentLocation.latitude,
                currentLongitude = tracking.currentLocation.longitude,
                currentAddress = tracking.currentLocation.address,
                status = tracking.status.name,
                estimatedArrival = tracking.estimatedArrival,
                actualArrival = tracking.actualArrival,
                statusHistory = tracking.statusHistory,
                notes = tracking.notes,
                lastUpdated = tracking.lastUpdated,
                createdAt = tracking.createdAt
            )
        }
    }
}

class DeliveryTrackingConverters {
    @TypeConverter
    fun fromStatusUpdateList(value: List<DeliveryStatusUpdate>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStatusUpdateList(value: String): List<DeliveryStatusUpdate> {
        return Gson().fromJson(value, object : TypeToken<List<DeliveryStatusUpdate>>() {}.type)
    }
}