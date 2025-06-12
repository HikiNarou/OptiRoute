package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.algorithms.models.Node
import com.optiroute.com.domain.algorithms.models.OptimizationResult
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.utils.Constants
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class AStarAlgorithm @Inject constructor() {

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

        // For single customer, return direct route
        if (customers.size == 1) {
            val customer = customers.first()
            val distance = depot.location.distanceTo(customer.location) * 2
            return OptimizationResult(
                path = listOf(depot.location, customer.location, depot.location),
                totalDistance = distance,
                estimatedDuration = calculateDuration(distance),
                visitOrder = listOf(customer.id)
            )
        }

        // Use A* to find optimal tour
        val tour = findOptimalTour(depot.location, customers)
        val path = buildFullPath(depot.location, tour, customers)
        val totalDistance = calculateTotalDistance(path)

        return OptimizationResult(
            path = path,
            totalDistance = totalDistance,
            estimatedDuration = calculateDuration(totalDistance),
            visitOrder = tour.map { customers[it].id }
        )
    }

    private fun findOptimalTour(depotLocation: Location, customers: List<Customer>): List<Int> {
        val n = customers.size
        if (n <= 2) {
            return (0 until n).toList()
        }

        // For larger sets, use modified A* with TSP heuristics
        return solveTSPWithAStar(depotLocation, customers)
    }

    private fun solveTSPWithAStar(depotLocation: Location, customers: List<Customer>): List<Int> {
        val n = customers.size
        val distances = Array(n + 1) { Array(n + 1) { 0.0 } }

        // Calculate distance matrix (including depot as node 0)
        for (i in 0 until n) {
            distances[0][i + 1] = depotLocation.distanceTo(customers[i].location)
            distances[i + 1][0] = distances[0][i + 1]
            for (j in 0 until n) {
                if (i != j) {
                    distances[i + 1][j + 1] = customers[i].location.distanceTo(customers[j].location)
                }
            }
        }

        // Use nearest neighbor with 2-opt improvement
        val tour = nearestNeighborTour(distances, n)
        return improveTourWith2Opt(tour, distances)
    }

    private fun nearestNeighborTour(distances: Array<Array<Double>>, n: Int): MutableList<Int> {
        val tour = mutableListOf<Int>()
        val visited = BooleanArray(n + 1)

        var current = 0 // Start from depot
        visited[current] = true

        while (tour.size < n) {
            var nearest = -1
            var minDistance = Double.MAX_VALUE

            for (i in 1..n) {
                if (!visited[i] && distances[current][i] < minDistance) {
                    minDistance = distances[current][i]
                    nearest = i
                }
            }

            if (nearest != -1) {
                tour.add(nearest - 1) // Convert to 0-based customer index
                visited[nearest] = true
                current = nearest
            } else {
                break
            }
        }

        return tour
    }

    private fun improveTourWith2Opt(tour: MutableList<Int>, distances: Array<Array<Double>>): List<Int> {
        val n = tour.size
        var improved = true

        while (improved) {
            improved = false

            for (i in 0 until n - 1) {
                for (j in i + 2 until n) {
                    if (j == n - 1 && i == 0) continue // Skip if it would disconnect the tour

                    val before = calculateSegmentDistance(tour, distances, i, j)

                    // Perform 2-opt swap
                    reverseSegment(tour, i + 1, j)
                    val after = calculateSegmentDistance(tour, distances, i, j)

                    if (after < before) {
                        improved = true
                    } else {
                        // Revert if no improvement
                        reverseSegment(tour, i + 1, j)
                    }
                }
            }
        }

        return tour.toList()
    }

    private fun calculateSegmentDistance(
        tour: List<Int>,
        distances: Array<Array<Double>>,
        i: Int,
        j: Int
    ): Double {
        val prev = if (i == 0) 0 else tour[i - 1] + 1
        val curr1 = tour[i] + 1
        val curr2 = tour[j] + 1
        val next = if (j == tour.size - 1) 0 else tour[j + 1] + 1

        return distances[prev][curr1] + distances[curr2][next]
    }

    private fun reverseSegment(tour: MutableList<Int>, start: Int, end: Int) {
        var i = start
        var j = end
        while (i < j) {
            val temp = tour[i]
            tour[i] = tour[j]
            tour[j] = temp
            i++
            j--
        }
    }

    private fun buildFullPath(
        depotLocation: Location,
        tour: List<Int>,
        customers: List<Customer>
    ): List<Location> {
        val path = mutableListOf<Location>()
        path.add(depotLocation)

        for (customerIndex in tour) {
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
        val hours = distance / Constants.DEFAULT_VEHICLE_SPEED_KMH
        return (hours * 3600 * 1000).toLong()
    }

    // Heuristic function for A* (Manhattan distance approximation)
    private fun heuristic(from: Location, to: Location): Double {
        return from.distanceTo(to)
    }
}