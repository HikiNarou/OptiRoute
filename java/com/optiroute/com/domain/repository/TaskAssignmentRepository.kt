package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.TaskAssignment
import com.optiroute.com.domain.models.TaskOffer
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface TaskAssignmentRepository {
    suspend fun createTaskAssignment(taskAssignment: TaskAssignment): Resource<TaskAssignment>
    suspend fun updateTaskAssignment(taskAssignment: TaskAssignment): Resource<TaskAssignment>
    suspend fun getTaskAssignmentById(id: String): Resource<TaskAssignment>
    fun getTaskAssignmentsByUmkm(umkmId: String): Flow<List<TaskAssignment>>
    fun getTaskAssignmentsByAdmin(adminId: String): Flow<List<TaskAssignment>>
    fun getTaskAssignmentsByKurir(kurirId: String): Flow<List<TaskAssignment>>
    fun getPendingApprovalTasks(): Flow<List<TaskAssignment>>
    fun getAvailableTasksForKurir(): Flow<List<TaskAssignment>>
    suspend fun approveTask(taskId: String, adminId: String): Resource<Boolean>
    suspend fun rejectTask(taskId: String, reason: String): Resource<Boolean>
    suspend fun acceptTask(taskId: String, kurirId: String): Resource<Boolean>
    suspend fun rejectTaskByKurir(taskId: String, kurirId: String, reason: String): Resource<Boolean>
    suspend fun offerTaskToKurir(taskAssignmentId: String, kurirId: String): Resource<TaskOffer>
    fun getActiveTaskOffersForKurir(kurirId: String): Flow<List<TaskOffer>>
    suspend fun expireOldTaskOffers(): Resource<Boolean>
}