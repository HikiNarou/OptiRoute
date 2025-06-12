package com.optiroute.com.domain.usecase.route

import com.optiroute.com.domain.algorithms.RouteOptimizer
import com.optiroute.com.domain.models.*
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.domain.repository.RouteRepository
import com.optiroute.com.domain.repository.VehicleRepository
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OptimizeRouteUseCase @Inject constructor(
    private val routeOptimizer: RouteOptimizer,
    private val routeRepository: RouteRepository,
    private val customerRepository: CustomerRepository,
    private val depotRepository: DepotRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(
        depotId: String,
        vehicleId: String,
        kurirId: String,
        customerIds: List<String>
    ): Resource<Route> {
        return try {
            // Validate inputs
            if (depotId.isBlank()) return Resource.Error("Depot must be selected")
            if (vehicleId.isBlank()) return Resource.Error("Vehicle must be selected")
            if (kurirId.isBlank()) return Resource.Error("Kurir must be selected")
            if (customerIds.isEmpty()) return Resource.Error("At least one customer must be selected")

            // Get depot
            val depotResult = depotRepository.getDepotById(depotId)
            if (depotResult !is Resource.Success) {
                return Resource.Error("Depot not found")
            }
            val depot = depotResult.data!!

            // Get vehicle
            val vehicleResult = vehicleRepository.getVehicleById(vehicleId)
            if (vehicleResult !is Resource.Success) {
                return Resource.Error("Vehicle not found")
            }
            val vehicle = vehicleResult.data!!

            // Get customers
            val customers = mutableListOf<Customer>()
            var totalWeight = 0.0

            for (customerId in customerIds) {
                val customerResult = customerRepository.getCustomerById(customerId)
                if (customerResult is Resource.Success) {
                    val customer = customerResult.data!!
                    customers.add(customer)
                    totalWeight += customer.itemWeight
                } else {
                    return Resource.Error("Customer $customerId not found")
                }
            }

            // Check vehicle capacity
            if (totalWeight > vehicle.capacity) {
                return Resource.Error("Total weight (${totalWeight}kg) exceeds vehicle capacity (${vehicle.capacity}kg)")
            }

            // Optimize route
            val optimizedRoute = routeOptimizer.optimizeRoute(
                depot = depot,
                customers = customers,
                vehicle = vehicle
            )

            // Create route
            val route = Route(
                depotId = depotId,
                vehicleId = vehicleId,
                kurirId = kurirId,
                customerIds = customerIds,
                optimizedPath = optimizedRoute.path,
                totalDistance = optimizedRoute.totalDistance,
                estimatedDuration = optimizedRoute.estimatedDuration,
                totalWeight = totalWeight,
                status = RouteStatus.PLANNED
            )

            routeRepository.createRoute(route)
        } catch (e: Exception) {
            Resource.Error("Route optimization failed: ${e.message}")
        }
    }
}