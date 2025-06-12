package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.utils.LocationUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteOptimizer @Inject constructor(
    private val aStarPathfinder: AStarPathfinder,
    private val clarkWrightSavings: ClarkWrightSavings,
    private val geneticAlgorithm: GeneticAlgorithm
) {

    data class OptimizedRouteResult(
        val path: List<Location>,
        val totalDistance: Double,
        val estimatedDuration: Long,
        val optimizationScore: Double,
        val algorithmUsed: String,
        val optimizationMetrics: OptimizationMetrics
    )

    data class OptimizationMetrics(
        val totalCustomers: Int,
        val vehicleUtilization: Double,
        val routeEfficiency: Double,
        val timeComplexity: Long,
        val iterationsPerformed: Int
    )

    fun optimizeRoute(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): OptimizedRouteResult {
        if (customers.isEmpty()) {
            return createEmptyResult(depot)
        }

        val startTime = System.currentTimeMillis()

        // Step 1: Filter customers based on vehicle capacity
        val feasibleCustomers = filterFeasibleCustomers(customers, vehicle)

        if (feasibleCustomers.isEmpty()) {
            return createEmptyResult(depot, "No customers fit vehicle capacity")
        }

        // Step 2: Choose optimization algorithm based on problem size
        val result = when {
            feasibleCustomers.size <= 5 -> {
                // Use brute force for small problems
                optimizeSmallRoute(feasibleCustomers, depot, vehicle)
            }
            feasibleCustomers.size <= 15 -> {
                // Use Clarke-Wright Savings for medium problems
                optimizeWithClarkWright(feasibleCustomers, depot, vehicle)
            }
            else -> {
                // Use Genetic Algorithm for large problems
                optimizeWithGeneticAlgorithm(feasibleCustomers, depot, vehicle)
            }
        }

        val optimizationTime = System.currentTimeMillis() - startTime

        return result.copy(
            optimizationMetrics = result.optimizationMetrics.copy(
                timeComplexity = optimizationTime
            )
        )
    }

    private fun filterFeasibleCustomers(customers: List<Customer>, vehicle: Vehicle): List<Customer> {
        val sortedByPriority = customers.sortedByDescending { it.priority.ordinal }
        val feasibleCustomers = mutableListOf<Customer>()
        var currentWeight = 0.0

        for (customer in sortedByPriority) {
            if (currentWeight + customer.itemWeight <= vehicle.capacity) {
                feasibleCustomers.add(customer)
                currentWeight += customer.itemWeight
            }
        }

        return feasibleCustomers
    }

    private fun optimizeSmallRoute(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): OptimizedRouteResult {
        // Brute force approach for small problems
        val allPermutations = generatePermutations(customers)
        var bestPath = listOf<Location>()
        var bestDistance = Double.MAX_VALUE
        var bestPermutation = emptyList<Customer>()

        for (permutation in allPermutations) {
            val path = createPath(permutation, depot)
            val distance = calculateTotalDistance(path)

            if (distance < bestDistance) {
                bestDistance = distance
                bestPath = path
                bestPermutation = permutation
            }
        }

        return createResult(
            path = bestPath,
            distance = bestDistance,
            customers = bestPermutation,
            vehicle = vehicle,
            algorithm = "Brute Force",
            iterations = allPermutations.size
        )
    }

    private fun optimizeWithClarkWright(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): OptimizedRouteResult {
        val result = clarkWrightSavings.optimize(customers, depot, vehicle)

        return createResult(
            path = result.path,
            distance = result.totalDistance,
            customers = customers,
            vehicle = vehicle,
            algorithm = "Clarke-Wright Savings",
            iterations = result.iterations
        )
    }

    private fun optimizeWithGeneticAlgorithm(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): OptimizedRouteResult {
        val result = geneticAlgorithm.optimize(customers, depot, vehicle)

        return createResult(
            path = result.path,
            distance = result.totalDistance,
            customers = customers,
            vehicle = vehicle,
            algorithm = "Genetic Algorithm",
            iterations = result.generations
        )
    }

    private fun generatePermutations(customers: List<Customer>): List<List<Customer>> {
        if (customers.isEmpty()) return listOf(emptyList())
        if (customers.size == 1) return listOf(customers)

        val result = mutableListOf<List<Customer>>()

        for (i in customers.indices) {
            val current = customers[i]
            val remaining = customers.toMutableList().apply { removeAt(i) }
            val subPermutations = generatePermutations(remaining)

            for (subPerm in subPermutations) {
                result.add(listOf(current) + subPerm)
            }
        }

        return result
    }

    private fun createPath(customers: List<Customer>, depot: Depot): List<Location> {
        val path = mutableListOf<Location>()
        path.add(depot.location)
        path.addAll(customers.map { it.location })
        path.add(depot.location)
        return path
    }

    private fun calculateTotalDistance(path: List<Location>): Double {
        if (path.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until path.size - 1) {
            totalDistance += LocationUtils.calculateDistance(
                path[i].latitude, path[i].longitude,
                path[i + 1].latitude, path[i + 1].longitude
            )
        }
        return totalDistance
    }

    private fun createResult(
        path: List<Location>,
        distance: Double,
        customers: List<Customer>,
        vehicle: Vehicle,
        algorithm: String,
        iterations: Int
    ): OptimizedRouteResult {
        val estimatedDuration = calculateEstimatedDuration(distance, vehicle)
        val optimizationScore = calculateOptimizationScore(path, customers, distance)
        val vehicleUtilization = customers.sumOf { it.itemWeight } / vehicle.capacity
        val routeEfficiency = calculateRouteEfficiency(distance, customers.size)

        return OptimizedRouteResult(
            path = path,
            totalDistance = distance,
            estimatedDuration = estimatedDuration,
            optimizationScore = optimizationScore,
            algorithmUsed = algorithm,
            optimizationMetrics = OptimizationMetrics(
                totalCustomers = customers.size,
                vehicleUtilization = vehicleUtilization,
                routeEfficiency = routeEfficiency,
                timeComplexity = 0L, // Will be set by caller
                iterationsPerformed = iterations
            )
        )
    }

    private fun createEmptyResult(depot: Depot, reason: String = "No customers"): OptimizedRouteResult {
        return OptimizedRouteResult(
            path = listOf(depot.location),
            totalDistance = 0.0,
            estimatedDuration = 0L,
            optimizationScore = 0.0,
            algorithmUsed = "None ($reason)",
            optimizationMetrics = OptimizationMetrics(
                totalCustomers = 0,
                vehicleUtilization = 0.0,
                routeEfficiency = 0.0,
                timeComplexity = 0L,
                iterationsPerformed = 0
            )
        )
    }

    private fun calculateEstimatedDuration(distance: Double, vehicle: Vehicle): Long {
        val averageSpeed = 40.0 // km/h in urban areas
        val serviceTimePerStop = 10 * 60 * 1000L // 10 minutes per stop in milliseconds
        val trafficFactor = 1.3 // 30% longer due to traffic

        val travelTime = (distance / averageSpeed) * 60 * 60 * 1000L * trafficFactor
        val totalServiceTime = serviceTimePerStop * vehicle.capacity.toInt() / 100

        return (travelTime + totalServiceTime).toLong()
    }

    private fun calculateOptimizationScore(path: List<Location>, customers: List<Customer>, distance: Double): Double {
        if (customers.isEmpty() || distance == 0.0) return 100.0

        // Calculate theoretical minimum distance (straight line to all customers)
        val depotLocation = path.firstOrNull() ?: return 0.0
        val theoreticalDistance = customers.sumOf { customer ->
            LocationUtils.calculateDistance(
                depotLocation.latitude, depotLocation.longitude,
                customer.location.latitude, customer.location.longitude
            ) * 2 // Round trip
        }

        // Efficiency score (100 = perfect, 0 = worst)
        return if (theoreticalDistance > 0) {
            maxOf(0.0, 100.0 - ((distance - theoreticalDistance) / theoreticalDistance * 100.0))
        } else {
            100.0
        }
    }

    private fun calculateRouteEfficiency(distance: Double, customerCount: Int): Double {
        if (customerCount == 0) return 0.0

        // Efficiency based on distance per customer
        val distancePerCustomer = distance / customerCount
        val idealDistancePerCustomer = 2.0 // 2km per customer is considered ideal

        return maxOf(0.0, 100.0 - ((distancePerCustomer - idealDistancePerCustomer) / idealDistancePerCustomer * 100.0))
    }

    // Advanced optimization methods
    fun optimizeMultiVehicleRoute(
        customers: List<Customer>,
        depot: Depot,
        vehicles: List<Vehicle>
    ): List<OptimizedRouteResult> {
        if (customers.isEmpty() || vehicles.isEmpty()) return emptyList()

        // Cluster customers based on location and vehicle capacity
        val customerClusters = clusterCustomersByVehicles(customers, vehicles)
        val results = mutableListOf<OptimizedRouteResult>()

        for ((vehicle, clusterCustomers) in customerClusters) {
            if (clusterCustomers.isNotEmpty()) {
                val result = optimizeRoute(clusterCustomers, depot, vehicle)
                results.add(result)
            }
        }

        return results
    }

    private fun clusterCustomersByVehicles(
        customers: List<Customer>,
        vehicles: List<Vehicle>
    ): Map<Vehicle, List<Customer>> {
        val sortedCustomers = customers.sortedByDescending { it.priority.ordinal }
        val sortedVehicles = vehicles.sortedByDescending { it.capacity }
        val clusters = mutableMapOf<Vehicle, MutableList<Customer>>()

        // Initialize clusters
        for (vehicle in sortedVehicles) {
            clusters[vehicle] = mutableListOf()
        }

        // Assign customers to vehicles based on capacity
        for (customer in sortedCustomers) {
            val suitableVehicle = sortedVehicles.find { vehicle ->
                val currentWeight = clusters[vehicle]?.sumOf { it.itemWeight } ?: 0.0
                currentWeight + customer.itemWeight <= vehicle.capacity
            }

            suitableVehicle?.let {
                clusters[it]?.add(customer)
            }
        }

        return clusters.mapValues { it.value.toList() }
    }

    fun calculateRouteMetrics(route: OptimizedRouteResult): Map<String, Any> {
        return mapOf(
            "total_distance_km" to route.totalDistance,
            "estimated_duration_hours" to route.estimatedDuration / (1000 * 60 * 60.0),
            "optimization_score" to route.optimizationScore,
            "algorithm_used" to route.algorithmUsed,
            "customer_count" to route.optimizationMetrics.totalCustomers,
            "vehicle_utilization_percent" to route.optimizationMetrics.vehicleUtilization * 100,
            "route_efficiency_score" to route.optimizationMetrics.routeEfficiency,
            "optimization_time_ms" to route.optimizationMetrics.timeComplexity,
            "iterations_performed" to route.optimizationMetrics.iterationsPerformed
        )
    }
}