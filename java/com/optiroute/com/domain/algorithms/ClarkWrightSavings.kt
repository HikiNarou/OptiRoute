package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.utils.LocationUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClarkWrightSavings @Inject constructor() {

    data class SavingsResult(
        val path: List<Location>,
        val totalDistance: Double,
        val savings: Double,
        val iterations: Int
    )

    data class SavingsPair(
        val customer1: Customer,
        val customer2: Customer,
        val savings: Double,
        val combinedWeight: Double
    )

    fun optimize(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): SavingsResult {
        if (customers.isEmpty()) {
            return SavingsResult(
                path = listOf(depot.location),
                totalDistance = 0.0,
                savings = 0.0,
                iterations = 0
            )
        }

        if (customers.size == 1) {
            val path = listOf(depot.location, customers[0].location, depot.location)
            return SavingsResult(
                path = path,
                totalDistance = calculateTotalDistance(path),
                savings = 0.0,
                iterations = 1
            )
        }

        // Step 1: Calculate savings for all customer pairs
        val savingsList = calculateSavings(customers, depot)

        // Step 2: Sort savings in descending order
        val sortedSavings = savingsList.sortedByDescending { it.savings }

        // Step 3: Build routes using Clarke-Wright algorithm
        val routes = buildRoutes(sortedSavings, depot, vehicle)

        // Step 4: Select the best route that fits vehicle capacity
        val bestRoute = selectBestRoute(routes, vehicle)

        // Step 5: Calculate total savings
        val originalDistance = calculateOriginalDistance(bestRoute.customers, depot)
        val optimizedDistance = bestRoute.totalDistance
        val totalSavings = originalDistance - optimizedDistance

        return SavingsResult(
            path = bestRoute.path,
            totalDistance = optimizedDistance,
            savings = totalSavings,
            iterations = sortedSavings.size
        )
    }

    private fun calculateSavings(customers: List<Customer>, depot: Depot): List<SavingsPair> {
        val savings = mutableListOf<SavingsPair>()

        for (i in customers.indices) {
            for (j in i + 1 until customers.size) {
                val customer1 = customers[i]
                val customer2 = customers[j]

                // Calculate distances
                val distanceDepotTo1 = LocationUtils.calculateDistance(
                    depot.location.latitude, depot.location.longitude,
                    customer1.location.latitude, customer1.location.longitude
                )

                val distanceDepotTo2 = LocationUtils.calculateDistance(
                    depot.location.latitude, depot.location.longitude,
                    customer2.location.latitude, customer2.location.longitude
                )

                val distance1To2 = LocationUtils.calculateDistance(
                    customer1.location.latitude, customer1.location.longitude,
                    customer2.location.latitude, customer2.location.longitude
                )

                // Clarke-Wright savings formula: S(i,j) = d(0,i) + d(0,j) - d(i,j)
                val savingsValue = distanceDepotTo1 + distanceDepotTo2 - distance1To2
                val combinedWeight = customer1.itemWeight + customer2.itemWeight

                if (savingsValue > 0) { // Only consider positive savings
                    savings.add(
                        SavingsPair(
                            customer1 = customer1,
                            customer2 = customer2,
                            savings = savingsValue,
                            combinedWeight = combinedWeight
                        )
                    )
                }
            }
        }

        return savings
    }

    private fun buildRoutes(
        sortedSavings: List<SavingsPair>,
        depot: Depot,
        vehicle: Vehicle
    ): List<RouteCandidate> {
        val routes = mutableListOf<RouteCandidate>()
        val usedCustomers = mutableSetOf<String>()

        // Initialize individual routes for each customer
        val initialRoutes = mutableMapOf<String, RouteCandidate>()

        for (savings in sortedSavings) {
            val customer1Id = savings.customer1.id
            val customer2Id = savings.customer2.id

            // Skip if either customer is already in a route that would exceed capacity
            if (savings.combinedWeight > vehicle.capacity) continue

            // Check if we can merge these customers
            val route1 = initialRoutes[customer1Id]
            val route2 = initialRoutes[customer2Id]

            when {
                route1 == null && route2 == null -> {
                    // Create new route with both customers
                    if (!usedCustomers.contains(customer1Id) && !usedCustomers.contains(customer2Id)) {
                        val newRoute = createRoute(
                            listOf(savings.customer1, savings.customer2),
                            depot
                        )
                        if (newRoute.totalWeight <= vehicle.capacity) {
                            routes.add(newRoute)
                            initialRoutes[customer1Id] = newRoute
                            initialRoutes[customer2Id] = newRoute
                            usedCustomers.add(customer1Id)
                            usedCustomers.add(customer2Id)
                        }
                    }
                }

                route1 != null && route2 == null -> {
                    // Add customer2 to route1 if capacity allows
                    if (!usedCustomers.contains(customer2Id)) {
                        val newCustomers = route1.customers + savings.customer2
                        val newWeight = newCustomers.sumOf { it.itemWeight }

                        if (newWeight <= vehicle.capacity) {
                            val updatedRoute = createRoute(newCustomers, depot)
                            routes.remove(route1)
                            routes.add(updatedRoute)
                            initialRoutes[customer2Id] = updatedRoute
                            usedCustomers.add(customer2Id)
                        }
                    }
                }

                route1 == null && route2 != null -> {
                    // Add customer1 to route2 if capacity allows
                    if (!usedCustomers.contains(customer1Id)) {
                        val newCustomers = route2.customers + savings.customer1
                        val newWeight = newCustomers.sumOf { it.itemWeight }

                        if (newWeight <= vehicle.capacity) {
                            val updatedRoute = createRoute(newCustomers, depot)
                            routes.remove(route2)
                            routes.add(updatedRoute)
                            initialRoutes[customer1Id] = updatedRoute
                            usedCustomers.add(customer1Id)
                        }
                    }
                }

                // route1 != null && route2 != null - cannot merge different routes
            }
        }

        return routes
    }

    private fun createRoute(customers: List<Customer>, depot: Depot): RouteCandidate {
        // Optimize the order of customers using nearest neighbor
        val optimizedOrder = optimizeCustomerOrder(customers, depot)
        val path = mutableListOf<Location>()

        path.add(depot.location)
        path.addAll(optimizedOrder.map { it.location })
        path.add(depot.location)

        return RouteCandidate(
            customers = optimizedOrder,
            path = path,
            totalDistance = calculateTotalDistance(path),
            totalWeight = optimizedOrder.sumOf { it.itemWeight }
        )
    }

    private fun optimizeCustomerOrder(customers: List<Customer>, depot: Depot): List<Customer> {
        if (customers.size <= 1) return customers

        val remaining = customers.toMutableList()
        val optimized = mutableListOf<Customer>()
        var currentLocation = depot.location

        while (remaining.isNotEmpty()) {
            // Find nearest unvisited customer
            val nearest = remaining.minByOrNull { customer ->
                LocationUtils.calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    customer.location.latitude, customer.location.longitude
                )
            }

            if (nearest != null) {
                optimized.add(nearest)
                remaining.remove(nearest)
                currentLocation = nearest.location
            }
        }

        return optimized
    }

    private fun selectBestRoute(routes: List<RouteCandidate>, vehicle: Vehicle): RouteCandidate {
        // Filter routes that fit vehicle capacity
        val feasibleRoutes = routes.filter { it.totalWeight <= vehicle.capacity }

        if (feasibleRoutes.isEmpty()) {
            // If no routes fit, create a route with highest priority customers
            return RouteCandidate(
                customers = emptyList(),
                path = emptyList(),
                totalDistance = 0.0,
                totalWeight = 0.0
            )
        }

        // Select route with minimum distance
        return feasibleRoutes.minByOrNull { it.totalDistance } ?: feasibleRoutes.first()
    }

    private fun calculateOriginalDistance(customers: List<Customer>, depot: Depot): Double {
        // Calculate distance if each customer was visited individually
        return customers.sumOf { customer ->
            LocationUtils.calculateDistance(
                depot.location.latitude, depot.location.longitude,
                customer.location.latitude, customer.location.longitude
            ) * 2 // Round trip
        }
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

    private data class RouteCandidate(
        val customers: List<Customer>,
        val path: List<Location>,
        val totalDistance: Double,
        val totalWeight: Double
    )

    // Advanced Clarke-Wright with multiple constraints
    fun optimizeWithConstraints(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle,
        maxRouteTime: Long,
        serviceTimePerCustomer: Long
    ): SavingsResult {
        val basicResult = optimize(customers, depot, vehicle)

        // Check time constraints
        val estimatedTime = calculateRouteTime(basicResult.path, serviceTimePerCustomer)

        if (estimatedTime <= maxRouteTime) {
            return basicResult
        }

        // If time constraint is violated, reduce customers
        val reducedCustomers = reduceCustomersForTimeConstraint(
            customers, depot, vehicle, maxRouteTime, serviceTimePerCustomer
        )

        return optimize(reducedCustomers, depot, vehicle)
    }

    private fun calculateRouteTime(path: List<Location>, serviceTimePerCustomer: Long): Long {
        val travelTime = calculateTotalDistance(path) / 40.0 * 3600 * 1000 // 40 km/h in milliseconds
        val serviceTime = (path.size - 2) * serviceTimePerCustomer // Exclude depot
        return (travelTime + serviceTime).toLong()
    }

    private fun reduceCustomersForTimeConstraint(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle,
        maxRouteTime: Long,
        serviceTimePerCustomer: Long
    ): List<Customer> {
        val sortedByPriority = customers.sortedByDescending { it.priority.ordinal }
        val reducedCustomers = mutableListOf<Customer>()

        for (customer in sortedByPriority) {
            val testCustomers = reducedCustomers + customer
            val testResult = optimize(testCustomers, depot, vehicle)
            val testTime = calculateRouteTime(testResult.path, serviceTimePerCustomer)

            if (testTime <= maxRouteTime && testResult.path.isNotEmpty()) {
                reducedCustomers.add(customer)
            } else {
                break
            }
        }

        return reducedCustomers
    }
}