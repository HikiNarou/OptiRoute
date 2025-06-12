package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.DepotDao
import com.optiroute.com.data.local.entities.DepotEntity
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepotRepositoryImpl @Inject constructor(
    private val depotDao: DepotDao
) : DepotRepository {

    override fun getDepotsByAdmin(adminId: String): Flow<List<Depot>> {
        return depotDao.getDepotsByAdmin(adminId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createDepot(depot: Depot): Resource<Depot> {
        return try {
            val entity = DepotEntity.fromDomainModel(
                depot.copy(id = generateId())
            )
            depotDao.insertDepot(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create depot: ${e.message}")
        }
    }

    override suspend fun updateDepot(depot: Depot): Resource<Depot> {
        return try {
            val entity = DepotEntity.fromDomainModel(
                depot.copy(updatedAt = System.currentTimeMillis())
            )
            depotDao.updateDepot(entity)
            Resource.Success(depot)
        } catch (e: Exception) {
            Resource.Error("Failed to update depot: ${e.message}")
        }
    }

    override suspend fun deleteDepot(depotId: String): Resource<Boolean> {
        return try {
            depotDao.deleteDepotById(depotId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete depot: ${e.message}")
        }
    }

    override suspend fun getDepotById(depotId: String): Resource<Depot> {
        return try {
            val entity = depotDao.getDepotById(depotId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Depot not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get depot: ${e.message}")
        }
    }

    override fun getAllDepots(): Flow<List<Depot>> {
        return depotDao.getAllDepots().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllActiveDepots(): Flow<List<Depot>> {
        return depotDao.getAllActiveDepots().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateDepotStatus(depotId: String, isActive: Boolean): Resource<Boolean> {
        return try {
            depotDao.updateDepotStatus(depotId, isActive)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update depot status: ${e.message}")
        }
    }

    override fun getDepotsByLocation(location: Location, radiusKm: Double): Flow<List<Depot>> {
        return depotDao.getAllDepots().map { entities ->
            entities.map { it.toDomainModel() }.filter { depot ->
                val distance = calculateDistance(
                    location.latitude, location.longitude,
                    depot.location.latitude, depot.location.longitude
                )
                distance <= radiusKm
            }
        }
    }

    override suspend fun getDepotsCount(): Int {
        return try {
            depotDao.getDepotsCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getActiveDepotsCount(): Int {
        return try {
            depotDao.getActiveDepotsCount()
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)

        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }
}