package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.TaskAssignment
import com.optiroute.com.domain.models.TaskAssignmentStatus

@Entity(tableName = "task_assignments")
data class TaskAssignmentEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val umkmId: String,
    val adminId: String,
    val kurirId: String,
    val status: String,
    val priority: String,
    val description: String,
    val estimatedDuration: Long,
    val deadline: Long,
    val assignedAt: Long,
    val acceptedAt: Long,
    val rejectedAt: Long,
    val completedAt: Long,
    val rejectionReason: String,
    val completionNotes: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): TaskAssignment {
        return TaskAssignment(
            id = id,
            customerId = customerId,
            umkmId = umkmId,
            adminId = adminId,
            kurirId = kurirId,
            status = TaskAssignmentStatus.valueOf(status),
            priority = priority,
            description = description,
            estimatedDuration = estimatedDuration,
            deadline = deadline,
            assignedAt = assignedAt,
            acceptedAt = acceptedAt,
            rejectedAt = rejectedAt,
            completedAt = completedAt,
            rejectionReason = rejectionReason,
            completionNotes = completionNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(taskAssignment: TaskAssignment): TaskAssignmentEntity {
            return TaskAssignmentEntity(
                id = taskAssignment.id,
                customerId = taskAssignment.customerId,
                umkmId = taskAssignment.umkmId,
                adminId = taskAssignment.adminId,
                kurirId = taskAssignment.kurirId,
                status = taskAssignment.status.name,
                priority = taskAssignment.priority,
                description = taskAssignment.description,
                estimatedDuration = taskAssignment.estimatedDuration,
                deadline = taskAssignment.deadline,
                assignedAt = taskAssignment.assignedAt,
                acceptedAt = taskAssignment.acceptedAt,
                rejectedAt = taskAssignment.rejectedAt,
                completedAt = taskAssignment.completedAt,
                rejectionReason = taskAssignment.rejectionReason,
                completionNotes = taskAssignment.completionNotes,
                createdAt = taskAssignment.createdAt,
                updatedAt = taskAssignment.updatedAt
            )
        }
    }
}