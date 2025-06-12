package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.TaskAssignmentDao
import com.optiroute.com.data.local.entities.TaskAssignmentEntity
import com.optiroute.com.data.local.entities.TaskOfferEntity
import com.optiroute.com.domain.models.TaskAssignment
import com.optiroute.com.domain.models.TaskAssignmentStatus
import com.optiroute.com.domain.models.TaskOffer
import com.optiroute.com.domain.models.TaskOfferStatus
import com.optiroute.com.domain.repository.TaskAssignmentRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAssignmentRepositoryImpl @Inject constructor(
    private val taskAssignmentDao: TaskAssignmentDao
) : TaskAssignmentRepository {

    override suspend fun createTaskAssignment(taskAssignment: TaskAssignment): Resource<TaskAssignment> {
        return try {
            val entity = TaskAssignmentEntity.fromDomainModel(
                taskAssignment.copy(id = generateId())
            )
            taskAssignmentDao.insertTaskAssignment(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create task assignment: ${e.message}")
        }
    }

    override suspend fun updateTaskAssignment(taskAssignment: TaskAssignment): Resource<TaskAssignment> {
        return try {
            val entity = TaskAssignmentEntity.fromDomainModel(
                taskAssignment.copy(updatedAt = System.currentTimeMillis())
            )
            taskAssignmentDao.updateTaskAssignment(entity)
            Resource.Success(taskAssignment)
        } catch (e: Exception) {
            Resource.Error("Failed to update task assignment: ${e.message}")
        }
    }

    override suspend fun getTaskAssignmentById(id: String): Resource<TaskAssignment> {
        return try {
            val entity = taskAssignmentDao.getTaskAssignmentById(id)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Task assignment not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get task assignment: ${e.message}")
        }
    }

    override fun getTaskAssignmentsByUmkm(umkmId: String): Flow<List<TaskAssignment>> {
        return taskAssignmentDao.getTaskAssignmentsByUmkm(umkmId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTaskAssignmentsByAdmin(adminId: String): Flow<List<TaskAssignment>> {
        return taskAssignmentDao.getTaskAssignmentsByAdmin(adminId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTaskAssignmentsByKurir(kurirId: String): Flow<List<TaskAssignment>> {
        return taskAssignmentDao.getTaskAssignmentsByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getPendingApprovalTasks(): Flow<List<TaskAssignment>> {
        return taskAssignmentDao.getPendingApprovalTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAvailableTasksForKurir(): Flow<List<TaskAssignment>> {
        return taskAssignmentDao.getAvailableTasksForKurir().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun approveTask(taskId: String, adminId: String): Resource<Boolean> {
        return try {
            taskAssignmentDao.updateTaskStatus(
                id = taskId,
                status = TaskAssignmentStatus.PENDING_KURIR_ACCEPTANCE.name,
                updatedAt = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to approve task: ${e.message}")
        }
    }

    override suspend fun rejectTask(taskId: String, reason: String): Resource<Boolean> {
        return try {
            taskAssignmentDao.rejectTask(
                id = taskId,
                status = TaskAssignmentStatus.REJECTED_BY_ADMIN.name,
                rejectedAt = System.currentTimeMillis(),
                reason = reason,
                updatedAt = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to reject task: ${e.message}")
        }
    }

    override suspend fun acceptTask(taskId: String, kurirId: String): Resource<Boolean> {
        return try {
            taskAssignmentDao.acceptTask(
                id = taskId,
                kurirId = kurirId,
                status = TaskAssignmentStatus.ACCEPTED_BY_KURIR.name,
                acceptedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to accept task: ${e.message}")
        }
    }

    override suspend fun rejectTaskByKurir(taskId: String, kurirId: String, reason: String): Resource<Boolean> {
        return try {
            taskAssignmentDao.rejectTask(
                id = taskId,
                status = TaskAssignmentStatus.REJECTED_BY_KURIR.name,
                rejectedAt = System.currentTimeMillis(),
                reason = reason,
                updatedAt = System.currentTimeMillis()
            )
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to reject task: ${e.message}")
        }
    }

    override suspend fun offerTaskToKurir(taskAssignmentId: String, kurirId: String): Resource<TaskOffer> {
        return try {
            val taskOffer = TaskOffer(
                id = generateId(),
                taskAssignmentId = taskAssignmentId,
                kurirId = kurirId,
                status = TaskOfferStatus.PENDING
            )
            val entity = TaskOfferEntity.fromDomainModel(taskOffer)
            taskAssignmentDao.insertTaskOffer(entity)
            Resource.Success(taskOffer)
        } catch (e: Exception) {
            Resource.Error("Failed to offer task: ${e.message}")
        }
    }

    override fun getActiveTaskOffersForKurir(kurirId: String): Flow<List<TaskOffer>> {
        return taskAssignmentDao.getActiveTaskOffersForKurir(
            kurirId = kurirId,
            currentTime = System.currentTimeMillis()
        ).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun expireOldTaskOffers(): Resource<Boolean> {
        return try {
            taskAssignmentDao.expireOldTaskOffers(System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to expire old task offers: ${e.message}")
        }
    }
}