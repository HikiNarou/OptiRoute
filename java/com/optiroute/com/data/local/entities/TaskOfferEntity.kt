package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.TaskOffer
import com.optiroute.com.domain.models.TaskOfferStatus

@Entity(tableName = "task_offers")
data class TaskOfferEntity(
    @PrimaryKey
    val id: String,
    val taskAssignmentId: String,
    val kurirId: String,
    val status: String,
    val offeredAt: Long,
    val expiresAt: Long,
    val respondedAt: Long,
    val responseMessage: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomainModel(): TaskOffer {
        return TaskOffer(
            id = id,
            taskAssignmentId = taskAssignmentId,
            kurirId = kurirId,
            status = TaskOfferStatus.valueOf(status),
            offeredAt = offeredAt,
            expiresAt = expiresAt,
            respondedAt = respondedAt,
            responseMessage = responseMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    companion object {
        fun fromDomainModel(taskOffer: TaskOffer): TaskOfferEntity {
            return TaskOfferEntity(
                id = taskOffer.id,
                taskAssignmentId = taskOffer.taskAssignmentId,
                kurirId = taskOffer.kurirId,
                status = taskOffer.status.name,
                offeredAt = taskOffer.offeredAt,
                expiresAt = taskOffer.expiresAt,
                respondedAt = taskOffer.respondedAt,
                responseMessage = taskOffer.responseMessage,
                createdAt = taskOffer.createdAt,
                updatedAt = taskOffer.updatedAt,
            )
        }
    }
}