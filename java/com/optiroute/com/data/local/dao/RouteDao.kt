package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: String): RouteEntity?

    @Query("SELECT * FROM routes WHERE kurirId = :kurirId ORDER BY createdAt DESC")
    fun getRoutesByKurir(kurirId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE vehicleId = :vehicleId ORDER BY createdAt DESC")
    fun getRoutesByVehicle(vehicleId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE depotId = :depotId ORDER BY createdAt DESC")
    fun getRoutesByDepot(depotId: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE status = :status ORDER BY createdAt DESC")
    fun getRoutesByStatus(status: String): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes ORDER BY createdAt DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE status IN ('PLANNED', 'IN_PROGRESS') ORDER BY createdAt DESC")
    fun getActiveRoutes(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :routeId")
    suspend fun deleteRouteById(routeId: String)

    @Query("UPDATE routes SET status = :status, updatedAt = :updatedAt WHERE id = :routeId")
    suspend fun updateRouteStatus(routeId: String, status: String, updatedAt: Long)

    @Query("UPDATE routes SET startedAt = :startedAt, updatedAt = :updatedAt WHERE id = :routeId")
    suspend fun updateRouteStartTime(routeId: String, startedAt: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE routes SET completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :routeId")
    suspend fun updateRouteCompletionTime(routeId: String, completedAt: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE routes SET kurirId = :kurirId, updatedAt = :updatedAt WHERE id = :routeId")
    suspend fun assignRouteToKurir(routeId: String, kurirId: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM routes")
    suspend fun getRoutesCount(): Int

    @Query("SELECT COUNT(*) FROM routes WHERE status = :status")
    suspend fun getRoutesCountByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM routes WHERE kurirId = :kurirId AND status IN ('PLANNED', 'IN_PROGRESS')")
    suspend fun getActiveRoutesCountByKurir(kurirId: String): Int

    @Query("SELECT AVG(totalDistance) FROM routes WHERE status = 'COMPLETED'")
    suspend fun getAverageRouteDistance(): Double

    @Query("SELECT AVG(estimatedDuration) FROM routes WHERE status = 'COMPLETED'")
    suspend fun getAverageRouteDuration(): Long
}