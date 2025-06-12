package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.DeliveryTask
import com.optiroute.com.domain.models.DeliveryStatus

@Entity(tableName = "delivery_tasks")
data class DeliveryTaskEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val kurirId: String,
    val routeId: String,
    val status: String,
    val notes: String,
    val estimatedDeliveryTime: Long,
    val actualDeliveryTime: Long,
    val assignedAt: Long,
    val pickedUpAt: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): DeliveryTask {
        return DeliveryTask(
            id = id,
            customerId = customerId,
            kurirId = kurirId,
            routeId = routeId,
            status = DeliveryStatus.valueOf(status),
            notes = notes,
            estimatedDeliveryTime = estimatedDeliveryTime,
            actualDeliveryTime = actualDeliveryTime,
            assignedAt = assignedAt,
            pickedUpAt = pickedUpAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(deliveryTask: DeliveryTask): DeliveryTaskEntity {
            return DeliveryTaskEntity(
                id = deliveryTask.id,
                customerId = deliveryTask.customerId,
                kurirId = deliveryTask.kurirId,
                routeId = deliveryTask.routeId,
                status = deliveryTask.status.name,
                notes = deliveryTask.notes,
                estimatedDeliveryTime = deliveryTask.estimatedDeliveryTime,
                actualDeliveryTime = deliveryTask.actualDeliveryTime,
                assignedAt = deliveryTask.assignedAt,
                pickedUpAt = deliveryTask.pickedUpAt,
                createdAt = deliveryTask.createdAt,
                updatedAt = deliveryTask.updatedAt
            )
        }
    }
}