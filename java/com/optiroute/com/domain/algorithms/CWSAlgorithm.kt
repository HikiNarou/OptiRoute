package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.algorithms.models.Edge
import com.optiroute.com.domain.algorithms.models.OptimizationResult
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class CWSAlgorithm @Inject constructor() {

    fun optimizeRoute(
        depot: Depot,
        customers: List<Customer>,
        vehicle: Vehicle
    ): OptimizationResult {
        if (customers.isEmpty()) {
            return OptimizationResult(
                path = listOf(depot.location),
                totalDistance = 0.0,
                estimatedDuration = 0L,
                visitOrder = emptyList()
            )
        }

        if (customers.size == 1) {
            val customer = customers.first()
            val distance = depot.location.distanceTo(customer.location) * 2 // Round trip
            return OptimizationResult(
                path = listOf(depot.location, customer.location, depot.location),
                totalDistance = distance,
                estimatedDuration = calculateDuration(distance),
                visitOrder = listOf(customer.id)
            )
        }

        // Calculate savings for all customer pairs
        val savings = calculateSavings(depot.location, customers)

        // Sort savings in descending order
        val sortedSavings = savings.sortedByDescending { it.savings }

        // Create initial routes (depot-customer-depot)
        val routes = customers.mapIndexed { index, customer ->
            mutableListOf(index)
        }.toMutableList()

        val routeWeights = customers.map { it.itemWeight }.toMutableList()

        // Merge routes based on savings
        for (saving in sortedSavings) {
            val route1Index = findRouteContaining(routes, saving.from)
            val route2Index = findRouteContaining(routes, saving.to)

            if (route1Index != route2Index && route1Index != -1 && route2Index != -1) {
                val route1 = routes[route1Index]
                val route2 = routes[route2Index]
                val weight1 = routeWeights[route1Index]
                val weight2 = routeWeights[route2Index]

                // Check if merging is feasible (capacity constraint)
                if (weight1 + weight2 <= vehicle.capacity) {
                    // Check if customers are at the ends of their routes
                    if (canMergeRoutes(route1, route2, saving.from, saving.to)) {
                        mergeRoutes(routes, routeWeights, route1Index, route2Index, saving.from, saving.to)
                    }
                }
            }
        }

        // Find the best single route or combine multiple routes
        val bestRoute = if (routes.size == 1) {
            routes.first()
        } else {
            // If we have multiple routes, combine them using nearest neighbor
            combineMultipleRoutes(routes, customers, depot.location)
        }

        // Build the final path
        val path = buildPath(depot.location, bestRoute, customers)
        val totalDistance = calculateTotalDistance(path)

        return OptimizationResult(
            path = path,
            totalDistance = totalDistance,
            estimatedDuration = calculateDuration(totalDistance),
            visitOrder = bestRoute.map { customers[it].id }
        )
    }

    private fun calculateSavings(depotLocation: Location, customers: List<Customer>): List<Edge> {
        val savings = mutableListOf<Edge>()

        for (i in customers.indices) {
            for (j in i + 1 until customers.size) {
                val customer1 = customers[i]
                val customer2 = customers[j]

                val distanceDepotTo1 = depotLocation.distanceTo(customer1.location)
                val distanceDepotTo2 = depotLocation.distanceTo(customer2.location)
                val distance1To2 = customer1.location.distanceTo(customer2.location)

                // Clarke-Wright savings formula: s(i,j) = d(0,i) + d(0,j) - d(i,j)
                val savingsValue = distanceDepotTo1 + distanceDepotTo2 - distance1To2

                savings.add(Edge(i, j, distance1To2, savingsValue))
            }
        }

        return savings
    }

    private fun findRouteContaining(routes: List<List<Int>>, customerIndex: Int): Int {
        return routes.indexOfFirst { it.contains(customerIndex) }
    }

    private fun canMergeRoutes(route1: List<Int>, route2: List<Int>, customer1: Int, customer2: Int): Boolean {
        // Check if customers are at the ends of their respective routes
        val isCustomer1AtEnd = route1.first() == customer1 || route1.last() == customer1
        val isCustomer2AtEnd = route2.first() == customer2 || route2.last() == customer2

        return isCustomer1AtEnd && isCustomer2AtEnd
    }

    private fun mergeRoutes(
        routes: MutableList<MutableList<Int>>,
        routeWeights: MutableList<Double>,
        route1Index: Int,
        route2Index: Int,
        customer1: Int,
        customer2: Int
    ) {
        val route1 = routes[route1Index]
        val route2 = routes[route2Index]

        // Determine merge order
        val mergedRoute = when {
            route1.last() == customer1 && route2.first() == customer2 -> {
                route1 + route2
            }
            route1.first() == customer1 && route2.last() == customer2 -> {
                route2 + route1
            }
            route1.last() == customer1 && route2.last() == customer2 -> {
                route1 + route2.reversed()
            }
            route1.first() == customer1 && route2.first() == customer2 -> {
                route1.reversed() + route2
            }
            else -> route1 + route2 // Fallback
        }

        // Update routes
        routes[route1Index] = mergedRoute.toMutableList()
        routeWeights[route1Index] = routeWeights[route1Index] + routeWeights[route2Index]

        // Remove the second route
        routes.removeAt(route2Index)
        routeWeights.removeAt(route2Index)
    }

    private fun combineMultipleRoutes(
        routes: List<List<Int>>,
        customers: List<Customer>,
        depotLocation: Location
    ): List<Int> {
        // Use nearest neighbor to combine multiple routes
        val combined = mutableListOf<Int>()
        val remaining = routes.flatten().toMutableSet()

        if (remaining.isEmpty()) return combined

        // Start with the customer closest to depot
        var current = remaining.minByOrNull {
            depotLocation.distanceTo(customers[it].location)
        } ?: return combined

        combined.add(current)
        remaining.remove(current)

        // Add nearest neighbors
        while (remaining.isNotEmpty()) {
            val currentLocation = customers[current].location
            val nearest = remaining.minByOrNull {
                currentLocation.distanceTo(customers[it].location)
            } ?: break

            combined.add(nearest)
            remaining.remove(nearest)
            current = nearest
        }

        return combined
    }

    private fun buildPath(depotLocation: Location, route: List<Int>, customers: List<Customer>): List<Location> {
        val path = mutableListOf<Location>()
        path.add(depotLocation)

        for (customerIndex in route) {
            path.add(customers[customerIndex].location)
        }

        path.add(depotLocation) // Return to depot
        return path
    }

    private fun calculateTotalDistance(path: List<Location>): Double {
        var totalDistance = 0.0
        for (i in 0 until path.size - 1) {
            totalDistance += path[i].distanceTo(path[i + 1])
        }
        return totalDistance
    }

    private fun calculateDuration(distance: Double): Long {
        // Convert distance to duration in milliseconds
        // Assuming average speed of 40 km/h
        val hours = distance / Constants.DEFAULT_VEHICLE_SPEED_KMH
        return (hours * 3600 * 1000).toLong()
    }
}