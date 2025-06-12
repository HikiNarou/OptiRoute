package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.models.Location
import com.optiroute.com.utils.LocationUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AStarPathfinder @Inject constructor() {

    data class PathNode(
        val location: Location,
        val gCost: Double, // Cost from start to this node
        val hCost: Double, // Heuristic cost from this node to target
        val parent: PathNode? = null
    ) {
        val fCost: Double get() = gCost + hCost
    }

    data class PathResult(
        val path: List<Location>,
        val totalDistance: Double,
        val nodesExplored: Int,
        val executionTime: Long
    )

    fun findPath(
        start: Location,
        target: Location,
        obstacles: List<Location> = emptyList(),
        maxSearchRadius: Double = 50.0 // km
    ): PathResult {
        val startTime = System.currentTimeMillis()

        if (start == target) {
            return PathResult(
                path = listOf(start),
                totalDistance = 0.0,
                nodesExplored = 1,
                executionTime = System.currentTimeMillis() - startTime
            )
        }

        val openSet = PriorityQueue<PathNode> { a, b -> a.fCost.compareTo(b.fCost) }
        val closedSet = mutableSetOf<String>()
        val startNode = PathNode(
            location = start,
            gCost = 0.0,
            hCost = calculateHeuristic(start, target)
        )

        openSet.add(startNode)
        var nodesExplored = 0

        while (openSet.isNotEmpty()) {
            val currentNode = openSet.poll()
            val currentKey = locationKey(currentNode.location)
            nodesExplored++

            if (closedSet.contains(currentKey)) continue
            closedSet.add(currentKey)

            // Check if we reached the target
            if (isLocationClose(currentNode.location, target, 0.1)) {
                val path = reconstructPath(currentNode, target)
                val totalDistance = calculatePathDistance(path)

                return PathResult(
                    path = path,
                    totalDistance = totalDistance,
                    nodesExplored = nodesExplored,
                    executionTime = System.currentTimeMillis() - startTime
                )
            }

            // Generate neighbors
            val neighbors = generateNeighbors(currentNode.location, maxSearchRadius / 10)

            for (neighborLocation in neighbors) {
                val neighborKey = locationKey(neighborLocation)

                if (closedSet.contains(neighborKey)) continue
                if (isLocationInObstacles(neighborLocation, obstacles)) continue

                val tentativeGCost = currentNode.gCost +
                        calculateMovementCost(currentNode.location, neighborLocation)

                val neighborNode = PathNode(
                    location = neighborLocation,
                    gCost = tentativeGCost,
                    hCost = calculateHeuristic(neighborLocation, target),
                    parent = currentNode
                )

                // Check if this path to neighbor is better
                val existingNode = openSet.find {
                    locationKey(it.location) == neighborKey
                }

                if (existingNode == null || tentativeGCost < existingNode.gCost) {
                    if (existingNode != null) {
                        openSet.remove(existingNode)
                    }
                    openSet.add(neighborNode)
                }
            }

            // Prevent infinite loops
            if (nodesExplored > 1000) break
        }

        // No path found, return direct path
        return PathResult(
            path = listOf(start, target),
            totalDistance = LocationUtils.calculateDistance(
                start.latitude, start.longitude,
                target.latitude, target.longitude
            ),
            nodesExplored = nodesExplored,
            executionTime = System.currentTimeMillis() - startTime
        )
    }

    fun findOptimalPath(locations: List<Location>): PathResult {
        if (locations.size < 2) {
            return PathResult(
                path = locations,
                totalDistance = 0.0,
                nodesExplored = 0,
                executionTime = 0L
            )
        }

        val startTime = System.currentTimeMillis()
        var totalDistance = 0.0
        var totalNodesExplored = 0
        val completePath = mutableListOf<Location>()

        completePath.add(locations.first())

        for (i in 0 until locations.size - 1) {
            val pathResult = findPath(locations[i], locations[i + 1])

            // Add path excluding the first location (to avoid duplication)
            if (pathResult.path.size > 1) {
                completePath.addAll(pathResult.path.drop(1))
            }

            totalDistance += pathResult.totalDistance
            totalNodesExplored += pathResult.nodesExplored
        }

        return PathResult(
            path = completePath,
            totalDistance = totalDistance,
            nodesExplored = totalNodesExplored,
            executionTime = System.currentTimeMillis() - startTime
        )
    }

    private fun generateNeighbors(location: Location, stepSize: Double): List<Location> {
        val neighbors = mutableListOf<Location>()
        val directions = listOf(
            Pair(0.0, stepSize),      // North
            Pair(stepSize, 0.0),      // East
            Pair(0.0, -stepSize),     // South
            Pair(-stepSize, 0.0),     // West
            Pair(stepSize, stepSize), // Northeast
            Pair(stepSize, -stepSize), // Southeast
            Pair(-stepSize, -stepSize), // Southwest
            Pair(-stepSize, stepSize)   // Northwest
        )

        for ((deltaLat, deltaLng) in directions) {
            val newLat = location.latitude + deltaLat
            val newLng = location.longitude + deltaLng

            // Basic bounds checking
            if (newLat in -90.0..90.0 && newLng in -180.0..180.0) {
                neighbors.add(Location(newLat, newLng, "Generated"))
            }
        }

        return neighbors
    }

    private fun calculateHeuristic(from: Location, to: Location): Double {
        // Using Euclidean distance as heuristic
        return LocationUtils.calculateDistance(
            from.latitude, from.longitude,
            to.latitude, to.longitude
        )
    }

    private fun calculateMovementCost(from: Location, to: Location): Double {
        val distance = LocationUtils.calculateDistance(
            from.latitude, from.longitude,
            to.latitude, to.longitude
        )

        // Add penalty for diagonal movements
        val latDiff = abs(to.latitude - from.latitude)
        val lngDiff = abs(to.longitude - from.longitude)
        val isDiagonal = latDiff > 0 && lngDiff > 0

        return if (isDiagonal) distance * 1.414 else distance
    }

    private fun reconstructPath(endNode: PathNode, target: Location): List<Location> {
        val path = mutableListOf<Location>()
        var currentNode: PathNode? = endNode

        while (currentNode != null) {
            path.add(0, currentNode.location)
            currentNode = currentNode.parent
        }

        // Ensure we end at the exact target
        if (path.isNotEmpty() && path.last() != target) {
            path[path.size - 1] = target
        }

        return path
    }

    private fun calculatePathDistance(path: List<Location>): Double {
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

    private fun locationKey(location: Location): String {
        return "${location.latitude.hashCode()}_${location.longitude.hashCode()}"
    }

    private fun isLocationClose(loc1: Location, loc2: Location, toleranceKm: Double): Boolean {
        val distance = LocationUtils.calculateDistance(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude
        )
        return distance <= toleranceKm
    }

    private fun isLocationInObstacles(location: Location, obstacles: List<Location>): Boolean {
        return obstacles.any { obstacle ->
            isLocationClose(location, obstacle, 0.1) // 100m tolerance
        }
    }

    // Advanced pathfinding with road network consideration
    fun findRoadAwarePath(
        start: Location,
        target: Location,
        roadNetwork: List<Location> = emptyList()
    ): PathResult {
        if (roadNetwork.isEmpty()) {
            return findPath(start, target)
        }

        // Find nearest road points
        val startRoad = findNearestRoadPoint(start, roadNetwork)
        val targetRoad = findNearestRoadPoint(target, roadNetwork)

        val roadPath = findPath(startRoad, targetRoad, emptyList())

        // Combine start -> road -> target
        val completePath = mutableListOf<Location>()
        if (start != startRoad) completePath.add(start)
        completePath.addAll(roadPath.path)
        if (target != targetRoad) completePath.add(target)

        return PathResult(
            path = completePath,
            totalDistance = calculatePathDistance(completePath),
            nodesExplored = roadPath.nodesExplored,
            executionTime = roadPath.executionTime
        )
    }

    private fun findNearestRoadPoint(location: Location, roadNetwork: List<Location>): Location {
        return roadNetwork.minByOrNull { roadPoint ->
            LocationUtils.calculateDistance(
                location.latitude, location.longitude,
                roadPoint.latitude, roadPoint.longitude
            )
        } ?: location
    }
}