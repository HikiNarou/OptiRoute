package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.VehicleDao
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementasi konkret dari VehicleRepository.
 *
 * @param vehicleDao Instance dari VehicleDao yang diinjeksi oleh Hilt.
 */
@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun getAllVehicles(): Flow<List<VehicleEntity>> {
        Timber.d("VehicleRepository: Getting all vehicles from DAO.")
        return vehicleDao.getAllVehicles()
    }

    override fun getVehicleById(vehicleId: Int): Flow<VehicleEntity?> {
        Timber.d("VehicleRepository: Getting vehicle by ID %d from DAO.", vehicleId)
        return vehicleDao.getVehicleById(vehicleId)
    }

    override suspend fun addVehicle(vehicle: VehicleEntity): AppResult<Long> = withContext(Dispatchers.IO) {
        try {
            // Validasi di Entity constructor sudah menangani name, capacity, dan capacityUnit.
            // Kita bisa menambahkan validasi lain di sini jika perlu.
            val newId = vehicleDao.insertVehicle(vehicle)
            if (newId > 0) {
                Timber.i("VehicleRepository: Vehicle added successfully with ID %d: %s", newId, vehicle.name)
                AppResult.Success(newId)
            } else {
                Timber.w("VehicleRepository: Failed to add vehicle, DAO returned ID %d.", newId)
                AppResult.Error(Exception("Gagal menambahkan kendaraan, ID tidak valid."), "Gagal menambahkan kendaraan ke database.")
            }
        } catch (e: Exception) {
            Timber.e(e, "VehicleRepository: Error adding vehicle %s.", vehicle.name)
            AppResult.Error(e, "Gagal menambahkan kendaraan: ${e.message}")
        }
    }

    override suspend fun updateVehicle(vehicle: VehicleEntity): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            // Validasi di Entity constructor sudah menangani name, capacity, dan capacityUnit.
            vehicleDao.updateVehicle(vehicle)
            Timber.i("VehicleRepository: Vehicle updated successfully: ID %d, %s", vehicle.id, vehicle.name)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "VehicleRepository: Error updating vehicle %s.", vehicle.name)
            AppResult.Error(e, "Gagal memperbarui kendaraan: ${e.message}")
        }
    }

    override suspend fun deleteVehicle(vehicle: VehicleEntity): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            vehicleDao.deleteVehicle(vehicle)
            Timber.i("VehicleRepository: Vehicle deleted successfully: ID %d, %s", vehicle.id, vehicle.name)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "VehicleRepository: Error deleting vehicle %s.", vehicle.name)
            AppResult.Error(e, "Gagal menghapus kendaraan: ${e.message}")
        }
    }

    override fun getVehiclesCount(): Flow<Int> {
        Timber.d("VehicleRepository: Getting vehicles count from DAO.")
        return vehicleDao.getVehiclesCount()
    }

    override suspend fun clearAllVehicles(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            vehicleDao.clearAllVehicles()
            Timber.i("VehicleRepository: All vehicles cleared successfully.")
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "VehicleRepository: Error clearing all vehicles.")
            AppResult.Error(e, "Gagal membersihkan data kendaraan: ${e.message}")
        }
    }

    override fun getVehiclesByIds(vehicleIds: List<Int>): Flow<List<VehicleEntity>> {
        Timber.d("VehicleRepository: Getting vehicles by IDs from DAO: %s", vehicleIds.joinToString())
        return vehicleDao.getVehiclesByIds(vehicleIds)
    }
}
