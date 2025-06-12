package com.optiroute.com.domain.repository

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Route
import com.optiroute.com.domain.models.RouteStatus
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface RouteRepository {
    suspend fun createRoute(route: Route): Resource<Route>
    suspend fun updateRoute(route: Route): Resource<Route>
    suspend fun getRouteById(routeId: String): Resource<Route>
    fun getRoutesByKurir(kurirId: String): Flow<List<Route>>
    fun getRoutesByStatus(status: RouteStatus): Flow<List<Route>>
    fun getAllRoutes(): Flow<List<Route>>
    suspend fun optimizeRoute(customers: List<Customer>, depotId: String, vehicleId: String): Resource<Route>
    suspend fun startRoute(routeId: String): Resource<Boolean>
    suspend fun completeRoute(routeId: String): Resource<Boolean>
    suspend fun cancelRoute(routeId: String, reason: String): Resource<Boolean>
    suspend fun deleteRoute(routeId: String): Resource<Boolean>
    suspend fun assignRouteToKurir(routeId: String, kurirId: String): Resource<Boolean>
}