package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.VehicleDao
import com.optiroute.com.data.local.entities.VehicleEntity
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.models.VehicleType
import com.optiroute.com.domain.models.MaintenanceStatus
import com.optiroute.com.domain.repository.VehicleRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override suspend fun createVehicle(vehicle: Vehicle): Resource<Vehicle> {
        return try {
            val entity = VehicleEntity.fromDomainModel(
                vehicle.copy(id = generateId())
            )
            vehicleDao.insertVehicle(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create vehicle: ${e.message}")
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Resource<Vehicle> {
        return try {
            val entity = VehicleEntity.fromDomainModel(
                vehicle.copy(updatedAt = System.currentTimeMillis())
            )
            vehicleDao.updateVehicle(entity)
            Resource.Success(vehicle)
        } catch (e: Exception) {
            Resource.Error("Failed to update vehicle: ${e.message}")
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): Resource<Boolean> {
        return try {
            vehicleDao.deleteVehicleById(vehicleId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete vehicle: ${e.message}")
        }
    }

    override suspend fun getVehicleById(vehicleId: String): Resource<Vehicle> {
        return try {
            val entity = vehicleDao.getVehicleById(vehicleId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Vehicle not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get vehicle: ${e.message}")
        }
    }

    override fun getVehiclesByDepot(depotId: String): Flow<List<Vehicle>> {
        return vehicleDao.getVehiclesByDepot(depotId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAvailableVehiclesByDepot(depotId: String): Flow<List<Vehicle>> {
        return vehicleDao.getAvailableVehiclesByDepot(depotId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getVehiclesByKurir(kurirId: String): Flow<List<Vehicle>> {
        return vehicleDao.getVehiclesByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getVehiclesByType(vehicleType: VehicleType): Flow<List<Vehicle>> {
        return vehicleDao.getVehiclesByType(vehicleType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return vehicleDao.getAllVehicles().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun assignVehicleToKurir(vehicleId: String, kurirId: String): Resource<Boolean> {
        return try {
            vehicleDao.assignVehicleToKurir(vehicleId, kurirId, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to assign vehicle to kurir: ${e.message}")
        }
    }

    override suspend fun unassignVehicleFromKurir(vehicleId: String): Resource<Boolean> {
        return try {
            vehicleDao.unassignVehicleFromKurir(vehicleId, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to unassign vehicle from kurir: ${e.message}")
        }
    }

    override suspend fun updateVehicleAvailability(vehicleId: String, isAvailable: Boolean): Resource<Boolean> {
        return try {
            vehicleDao.updateVehicleAvailability(vehicleId, isAvailable, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update vehicle availability: ${e.message}")
        }
    }

    override suspend fun updateMaintenanceStatus(vehicleId: String, status: MaintenanceStatus): Resource<Boolean> {
        return try {
            vehicleDao.updateMaintenanceStatus(vehicleId, status.name, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update maintenance status: ${e.message}")
        }
    }

    override suspend fun getVehiclesCount(): Int {
        return try {
            vehicleDao.getVehicleCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getAvailableVehiclesCount(): Int {
        return try {
            vehicleDao.getAvailableVehicleCount()
        } catch (e: Exception) {
            0
        }
    }
}