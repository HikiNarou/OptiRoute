package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE depotId = :depotId ORDER BY licensePlate ASC")
    fun getVehiclesByDepot(depotId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE depotId = :depotId AND isAvailable = 1 ORDER BY licensePlate ASC")
    fun getAvailableVehiclesByDepot(depotId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE kurirId = :kurirId")
    fun getVehiclesByKurir(kurirId: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE vehicleType = :vehicleType ORDER BY licensePlate ASC")
    fun getVehiclesByType(vehicleType: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE isAvailable = :isAvailable ORDER BY licensePlate ASC")
    fun getVehiclesByAvailability(isAvailable: Boolean): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE maintenanceStatus = :status ORDER BY licensePlate ASC")
    fun getVehiclesByMaintenanceStatus(status: String): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles ORDER BY licensePlate ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :vehicleId")
    suspend fun deleteVehicleById(vehicleId: String)

    @Query("UPDATE vehicles SET kurirId = :kurirId, isAvailable = 0, updatedAt = :updatedAt WHERE id = :vehicleId")
    suspend fun assignVehicleToKurir(vehicleId: String, kurirId: String, updatedAt: Long)

    @Query("UPDATE vehicles SET kurirId = '', isAvailable = 1, updatedAt = :updatedAt WHERE id = :vehicleId")
    suspend fun unassignVehicleFromKurir(vehicleId: String, updatedAt: Long)

    @Query("UPDATE vehicles SET isAvailable = :isAvailable, updatedAt = :updatedAt WHERE id = :vehicleId")
    suspend fun updateVehicleAvailability(vehicleId: String, isAvailable: Boolean, updatedAt: Long)

    @Query("UPDATE vehicles SET maintenanceStatus = :status, updatedAt = :updatedAt WHERE id = :vehicleId")
    suspend fun updateMaintenanceStatus(vehicleId: String, status: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM vehicles")
    suspend fun getVehicleCount(): Int

    @Query("SELECT COUNT(*) FROM vehicles WHERE isAvailable = 1")
    suspend fun getAvailableVehicleCount(): Int

    @Query("SELECT COUNT(*) FROM vehicles WHERE depotId = :depotId")
    suspend fun getVehicleCountByDepot(depotId: String): Int

    @Query("SELECT COUNT(*) FROM vehicles WHERE depotId = :depotId AND isAvailable = 1")
    suspend fun getAvailableVehicleCountByDepot(depotId: String): Int
}