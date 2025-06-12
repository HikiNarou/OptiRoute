package com.optiroute.com.domain.algorithms.models

import com.optiroute.com.domain.models.Location

data class OptimizationResult(
    val path: List<Location>,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val visitOrder: List<String> // Customer IDs in visit order
)

data class Node(
    val location: Location,
    val customerId: String = "",
    val gCost: Double = 0.0, // Cost from start
    val hCost: Double = 0.0, // Heuristic cost to goal
    val fCost: Double = gCost + hCost, // Total cost
    val parent: Node? = null
)

data class Edge(
    val from: Int,
    val to: Int,
    val distance: Double,
    val savings: Double = 0.0
)