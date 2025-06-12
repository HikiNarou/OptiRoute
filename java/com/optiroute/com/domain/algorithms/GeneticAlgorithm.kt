package com.optiroute.com.domain.algorithms

import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.utils.LocationUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GeneticAlgorithm @Inject constructor() {

    data class GeneticResult(
        val path: List<Location>,
        val totalDistance: Double,
        val generations: Int,
        val finalFitness: Double,
        val convergenceGeneration: Int
    )

    data class Chromosome(
        val genes: List<Int>, // Customer indices
        val fitness: Double,
        val totalDistance: Double,
        val isValid: Boolean
    )

    private data class GeneticParameters(
        val populationSize: Int = 100,
        val maxGenerations: Int = 500,
        val mutationRate: Double = 0.1,
        val crossoverRate: Double = 0.8,
        val elitismRate: Double = 0.1,
        val convergenceThreshold: Int = 50
    )

    fun optimize(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle
    ): GeneticResult {
        if (customers.isEmpty()) {
            return GeneticResult(
                path = listOf(depot.location),
                totalDistance = 0.0,
                generations = 0,
                finalFitness = 0.0,
                convergenceGeneration = 0
            )
        }

        if (customers.size == 1) {
            val path = listOf(depot.location, customers[0].location, depot.location)
            return GeneticResult(
                path = path,
                totalDistance = calculatePathDistance(path),
                generations = 1,
                finalFitness = 1.0,
                convergenceGeneration = 1
            )
        }

        val params = GeneticParameters()
        val feasibleCustomers = filterByCapacity(customers, vehicle)

        if (feasibleCustomers.isEmpty()) {
            return GeneticResult(
                path = listOf(depot.location),
                totalDistance = 0.0,
                generations = 0,
                finalFitness = 0.0,
                convergenceGeneration = 0
            )
        }

        // Initialize population
        var population = initializePopulation(feasibleCustomers, depot, params.populationSize)
        var bestChromosome = population.maxByOrNull { it.fitness } ?: population.first()
        var convergenceCounter = 0
        var convergenceGeneration = 0

        for (generation in 1..params.maxGenerations) {
            // Selection
            val selectedParents = tournamentSelection(population, params.populationSize)

            // Crossover and Mutation
            val offspring = mutableListOf<Chromosome>()

            for (i in selectedParents.indices step 2) {
                if (i + 1 < selectedParents.size) {
                    val parent1 = selectedParents[i]
                    val parent2 = selectedParents[i + 1]

                    val (child1, child2) = if (Random.nextDouble() < params.crossoverRate) {
                        crossover(parent1, parent2, feasibleCustomers, depot)
                    } else {
                        Pair(parent1, parent2)
                    }

                    val mutatedChild1 = if (Random.nextDouble() < params.mutationRate) {
                        mutate(child1, feasibleCustomers, depot)
                    } else child1

                    val mutatedChild2 = if (Random.nextDouble() < params.mutationRate) {
                        mutate(child2, feasibleCustomers, depot)
                    } else child2

                    offspring.add(mutatedChild1)
                    offspring.add(mutatedChild2)
                }
            }

            // Elitism - keep best chromosomes
            val eliteCount = (params.populationSize * params.elitismRate).toInt()
            val elite = population.sortedByDescending { it.fitness }.take(eliteCount)

            // Create new population
            population = (elite + offspring).sortedByDescending { it.fitness }.take(params.populationSize)

            // Check for improvement
            val currentBest = population.first()
            if (currentBest.fitness > bestChromosome.fitness) {
                bestChromosome = currentBest
                convergenceCounter = 0
                convergenceGeneration = generation
            } else {
                convergenceCounter++
            }

            // Early stopping if converged
            if (convergenceCounter >= params.convergenceThreshold) {
                break
            }
        }

        val bestPath = createPathFromChromosome(bestChromosome, feasibleCustomers, depot)

        return GeneticResult(
            path = bestPath,
            totalDistance = bestChromosome.totalDistance,
            generations = convergenceGeneration,
            finalFitness = bestChromosome.fitness,
            convergenceGeneration = convergenceGeneration
        )
    }

    private fun filterByCapacity(customers: List<Customer>, vehicle: Vehicle): List<Customer> {
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

    private fun initializePopulation(
        customers: List<Customer>,
        depot: Depot,
        populationSize: Int
    ): List<Chromosome> {
        val population = mutableListOf<Chromosome>()

        // Add greedy solution
        val greedySolution = createGreedyChromosome(customers, depot)
        population.add(greedySolution)

        // Add random solutions
        repeat(populationSize - 1) {
            val randomGenes = customers.indices.shuffled()
            val chromosome = createChromosome(randomGenes, customers, depot)
            population.add(chromosome)
        }

        return population
    }

    private fun createGreedyChromosome(customers: List<Customer>, depot: Depot): Chromosome {
        val remaining = customers.toMutableList()
        val path = mutableListOf<Int>()
        var currentLocation = depot.location

        while (remaining.isNotEmpty()) {
            val nearestIndex = remaining.indices.minByOrNull { i ->
                LocationUtils.calculateDistance(
                    currentLocation.latitude, currentLocation.longitude,
                    remaining[i].location.latitude, remaining[i].location.longitude
                )
            } ?: 0

            val nearest = remaining[nearestIndex]
            path.add(customers.indexOf(nearest))
            remaining.removeAt(nearestIndex)
            currentLocation = nearest.location
        }

        return createChromosome(path, customers, depot)
    }

    private fun createChromosome(genes: List<Int>, customers: List<Customer>, depot: Depot): Chromosome {
        val path = createPathFromGenes(genes, customers, depot)
        val distance = calculatePathDistance(path)
        val fitness = calculateFitness(distance)
        val isValid = genes.size == customers.size && genes.distinct().size == genes.size

        return Chromosome(
            genes = genes,
            fitness = fitness,
            totalDistance = distance,
            isValid = isValid
        )
    }

    private fun createPathFromGenes(genes: List<Int>, customers: List<Customer>, depot: Depot): List<Location> {
        val path = mutableListOf<Location>()
        path.add(depot.location)

        for (geneIndex in genes) {
            if (geneIndex in customers.indices) {
                path.add(customers[geneIndex].location)
            }
        }

        path.add(depot.location)
        return path
    }

    private fun createPathFromChromosome(chromosome: Chromosome, customers: List<Customer>, depot: Depot): List<Location> {
        return createPathFromGenes(chromosome.genes, customers, depot)
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

    private fun calculateFitness(distance: Double): Double {
        // Fitness is inversely proportional to distance
        return if (distance > 0) 1000.0 / distance else 1000.0
    }

    private fun tournamentSelection(population: List<Chromosome>, selectionSize: Int): List<Chromosome> {
        val selected = mutableListOf<Chromosome>()
        val tournamentSize = 3

        repeat(selectionSize) {
            val tournament = population.shuffled().take(tournamentSize)
            val winner = tournament.maxByOrNull { it.fitness } ?: tournament.first()
            selected.add(winner)
        }

        return selected
    }

    private fun crossover(
        parent1: Chromosome,
        parent2: Chromosome,
        customers: List<Customer>,
        depot: Depot
    ): Pair<Chromosome, Chromosome> {
        if (parent1.genes.size != parent2.genes.size || parent1.genes.size < 2) {
            return Pair(parent1, parent2)
        }

        // Order Crossover (OX)
        val size = parent1.genes.size
        val start = Random.nextInt(size)
        val end = Random.nextInt(start + 1, size + 1)

        val child1Genes = MutableList(size) { -1 }
        val child2Genes = MutableList(size) { -1 }

        // Copy segments
        for (i in start until end) {
            child1Genes[i] = parent1.genes[i]
            child2Genes[i] = parent2.genes[i]
        }

        // Fill remaining positions
        fillRemainingPositions(child1Genes, parent2.genes, start, end)
        fillRemainingPositions(child2Genes, parent1.genes, start, end)

        val child1 = createChromosome(child1Genes.filterNot { it == -1 }, customers, depot)
        val child2 = createChromosome(child2Genes.filterNot { it == -1 }, customers, depot)

        return Pair(child1, child2)
    }

    private fun fillRemainingPositions(childGenes: MutableList<Int>, parentGenes: List<Int>, start: Int, end: Int) {
        val used = childGenes.filter { it != -1 }.toSet()
        val remaining = parentGenes.filter { it !in used }
        var remainingIndex = 0

        for (i in childGenes.indices) {
            if (childGenes[i] == -1 && remainingIndex < remaining.size) {
                childGenes[i] = remaining[remainingIndex]
                remainingIndex++
            }
        }
    }

    private fun mutate(chromosome: Chromosome, customers: List<Customer>, depot: Depot): Chromosome {
        if (chromosome.genes.size < 2) return chromosome

        val mutatedGenes = chromosome.genes.toMutableList()
        val mutationType = Random.nextInt(3)

        when (mutationType) {
            0 -> swapMutation(mutatedGenes)
            1 -> inverseMutation(mutatedGenes)
            2 -> insertMutation(mutatedGenes)
        }

        return createChromosome(mutatedGenes, customers, depot)
    }

    private fun swapMutation(genes: MutableList<Int>) {
        val index1 = Random.nextInt(genes.size)
        val index2 = Random.nextInt(genes.size)
        val temp = genes[index1]
        genes[index1] = genes[index2]
        genes[index2] = temp
    }

    private fun inverseMutation(genes: MutableList<Int>) {
        val start = Random.nextInt(genes.size)
        val end = Random.nextInt(start + 1, genes.size + 1)
        genes.subList(start, end).reverse()
    }

    private fun insertMutation(genes: MutableList<Int>) {
        val fromIndex = Random.nextInt(genes.size)
        val toIndex = Random.nextInt(genes.size)
        val element = genes.removeAt(fromIndex)
        genes.add(toIndex, element)
    }

    // Advanced genetic algorithm with adaptive parameters
    fun optimizeAdaptive(
        customers: List<Customer>,
        depot: Depot,
        vehicle: Vehicle,
        maxTime: Long = 30000 // 30 seconds
    ): GeneticResult {
        val startTime = System.currentTimeMillis()
        var params = GeneticParameters()
        var bestResult = optimize(customers, depot, vehicle)

        while (System.currentTimeMillis() - startTime < maxTime) {
            // Adapt parameters based on performance
            params = adaptParameters(params, bestResult.finalFitness)

            val currentResult = optimize(customers, depot, vehicle)
            if (currentResult.totalDistance < bestResult.totalDistance) {
                bestResult = currentResult
            }
        }

        return bestResult
    }

    private fun adaptParameters(params: GeneticParameters, currentFitness: Double): GeneticParameters {
        return params.copy(
            mutationRate = if (currentFitness < 10.0) {
                (params.mutationRate * 1.1).coerceAtMost(0.3)
            } else {
                (params.mutationRate * 0.9).coerceAtLeast(0.05)
            },
            populationSize = if (currentFitness < 5.0) {
                (params.populationSize * 1.2).toInt().coerceAtMost(200)
            } else {
                params.populationSize
            }
        )
    }
}