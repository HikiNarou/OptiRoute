package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.RouteDao
import com.optiroute.com.data.local.dao.DepotDao
import com.optiroute.com.data.local.dao.VehicleDao
import com.optiroute.com.data.local.entities.RouteEntity
import com.optiroute.com.domain.algorithms.RouteOptimizer
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Route
import com.optiroute.com.domain.models.RouteStatus
import com.optiroute.com.domain.repository.RouteRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepositoryImpl @Inject constructor(
    private val routeDao: RouteDao,
    private val depotDao: DepotDao,
    private val vehicleDao: VehicleDao,
    private val routeOptimizer: RouteOptimizer
) : RouteRepository {

    override suspend fun createRoute(route: Route): Resource<Route> {
        return try {
            val entity = RouteEntity.fromDomainModel(
                route.copy(id = generateId())
            )
            routeDao.insertRoute(entity)
            Resource.Success(entity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Failed to create route: ${e.message}")
        }
    }

    override suspend fun updateRoute(route: Route): Resource<Route> {
        return try {
            val entity = RouteEntity.fromDomainModel(
                route.copy(updatedAt = System.currentTimeMillis())
            )
            routeDao.updateRoute(entity)
            Resource.Success(route)
        } catch (e: Exception) {
            Resource.Error("Failed to update route: ${e.message}")
        }
    }

    override suspend fun getRouteById(routeId: String): Resource<Route> {
        return try {
            val entity = routeDao.getRouteById(routeId)
            if (entity != null) {
                Resource.Success(entity.toDomainModel())
            } else {
                Resource.Error("Route not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get route: ${e.message}")
        }
    }

    override fun getRoutesByKurir(kurirId: String): Flow<List<Route>> {
        return routeDao.getRoutesByKurir(kurirId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getRoutesByStatus(status: RouteStatus): Flow<List<Route>> {
        return routeDao.getRoutesByStatus(status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllRoutes(): Flow<List<Route>> {
        return routeDao.getAllRoutes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun optimizeRoute(customers: List<Customer>, depotId: String, vehicleId: String): Resource<Route> {
        return try {
            if (customers.isEmpty()) {
                return Resource.Error("No customers provided for route optimization")
            }

            // Get depot and vehicle entities
            val depotEntity = depotDao.getDepotById(depotId)
            val vehicleEntity = vehicleDao.getVehicleById(vehicleId)

            if (depotEntity == null) {
                return Resource.Error("Depot not found")
            }

            if (vehicleEntity == null) {
                return Resource.Error("Vehicle not found")
            }

            // Convert entities to domain models
            val depot = depotEntity.toDomainModel()
            val vehicle = vehicleEntity.toDomainModel()

            // Optimize route using the correct parameters
            val optimizedResult = routeOptimizer.optimizeRoute(customers, depot, vehicle)

            val route = Route(
                id = generateId(),
                customerIds = customers.map { it.id },
                vehicleId = vehicleId,
                depotId = depotId,
                kurirId = "", // Will be assigned later
                optimizedPath = optimizedResult.path,
                totalDistance = optimizedResult.totalDistance,
                estimatedDuration = optimizedResult.estimatedDuration,
                totalWeight = customers.sumOf { it.itemWeight },
                status = RouteStatus.PLANNED,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            createRoute(route)
        } catch (e: Exception) {
            Resource.Error("Failed to optimize route: ${e.message}")
        }
    }

    override suspend fun startRoute(routeId: String): Resource<Boolean> {
        return try {
            routeDao.updateRouteStatus(routeId, RouteStatus.IN_PROGRESS.name, System.currentTimeMillis())
            routeDao.updateRouteStartTime(routeId, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to start route: ${e.message}")
        }
    }

    override suspend fun completeRoute(routeId: String): Resource<Boolean> {
        return try {
            val currentTime = System.currentTimeMillis()
            routeDao.updateRouteStatus(routeId, RouteStatus.COMPLETED.name, currentTime)
            routeDao.updateRouteCompletionTime(routeId, currentTime)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to complete route: ${e.message}")
        }
    }

    override suspend fun cancelRoute(routeId: String, reason: String): Resource<Boolean> {
        return try {
            routeDao.updateRouteStatus(routeId, RouteStatus.CANCELLED.name, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to cancel route: ${e.message}")
        }
    }

    override suspend fun deleteRoute(routeId: String): Resource<Boolean> {
        return try {
            routeDao.deleteRouteById(routeId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete route: ${e.message}")
        }
    }

    override suspend fun assignRouteToKurir(routeId: String, kurirId: String): Resource<Boolean> {
        return try {
            routeDao.assignRouteToKurir(routeId, kurirId, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to assign route to kurir: ${e.message}")
        }
    }
}