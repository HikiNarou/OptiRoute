package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DepotRepository {
    suspend fun createDepot(depot: Depot): Resource<Depot>
    suspend fun updateDepot(depot: Depot): Resource<Depot>
    suspend fun deleteDepot(depotId: String): Resource<Boolean>
    suspend fun getDepotById(depotId: String): Resource<Depot>
    fun getDepotsByAdmin(adminId: String): Flow<List<Depot>>
    fun getAllActiveDepots(): Flow<List<Depot>>
    suspend fun updateDepotStatus(depotId: String, isActive: Boolean): Resource<Boolean>
    fun getAllDepots(): Flow<List<Depot>>
    fun getDepotsByLocation(location: Location, radiusKm: Double): Flow<List<Depot>>
    suspend fun getDepotsCount(): Int
    suspend fun getActiveDepotsCount(): Int
}