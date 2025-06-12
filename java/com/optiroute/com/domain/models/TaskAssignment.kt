package com.optiroute.com.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TaskAssignment(
    val id: String = "",
    val customerId: String = "",
    val umkmId: String = "",
    val adminId: String = "",
    val kurirId: String = "",
    val status: TaskAssignmentStatus = TaskAssignmentStatus.PENDING_ADMIN_APPROVAL,
    val priority: String = "NORMAL",
    val description: String = "",
    val estimatedDuration: Long = 0L,
    val deadline: Long = 0L,
    val assignedAt: Long = 0L,
    val acceptedAt: Long = 0L,
    val rejectedAt: Long = 0L,
    val completedAt: Long = 0L,
    val rejectionReason: String = "",
    val completionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class TaskOffer(
    val id: String = "",
    val taskAssignmentId: String = "",
    val kurirId: String = "",
    val status: TaskOfferStatus = TaskOfferStatus.PENDING,
    val offeredAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // 24 hours
    val respondedAt: Long = 0L,
    val responseMessage: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class TaskAssignmentStatus {
    PENDING_ADMIN_APPROVAL,     // Menunggu persetujuan admin
    APPROVED_BY_ADMIN,          // Disetujui admin
    REJECTED_BY_ADMIN,          // Ditolak admin
    PENDING_KURIR_ACCEPTANCE,   // Menunggu penerimaan kurir
    ACCEPTED_BY_KURIR,          // Diterima kurir
    REJECTED_BY_KURIR,          // Ditolak kurir
    ASSIGNED,                   // Ditugaskan
    IN_PROGRESS,                // Sedang dikerjakan
    COMPLETED,                  // Selesai
    CANCELLED                   // Dibatalkan
}

enum class TaskOfferStatus {
    PENDING,        // Menunggu respons
    ACCEPTED,       // Diterima
    REJECTED,       // Ditolak
    EXPIRED         // Kedaluwarsa
}