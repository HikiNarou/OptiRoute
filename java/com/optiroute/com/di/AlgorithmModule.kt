package com.optiroute.com.di

import com.optiroute.com.domain.algorithms.RouteOptimizer
import com.optiroute.com.domain.algorithms.GeneticAlgorithm
import com.optiroute.com.domain.algorithms.AStarPathfinder
import com.optiroute.com.domain.algorithms.ClarkWrightSavings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlgorithmModule {

    @Provides
    @Singleton
    fun provideAStarPathfinder(): AStarPathfinder {
        return AStarPathfinder()
    }

    @Provides
    @Singleton
    fun provideClarkWrightSavings(): ClarkWrightSavings {
        return ClarkWrightSavings()
    }

    @Provides
    @Singleton
    fun provideGeneticAlgorithm(): GeneticAlgorithm {
        return GeneticAlgorithm()
    }

    @Provides
    @Singleton
    fun provideRouteOptimizer(
        aStarPathfinder: AStarPathfinder,
        clarkWrightSavings: ClarkWrightSavings,
        geneticAlgorithm: GeneticAlgorithm
    ): RouteOptimizer {
        return RouteOptimizer(aStarPathfinder, clarkWrightSavings, geneticAlgorithm)
    }
}