package com.optiroute.com.di

import com.optiroute.com.data.repository.*
import com.optiroute.com.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDepotRepository(
        depotRepositoryImpl: DepotRepositoryImpl
    ): DepotRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryRepository(
        deliveryRepositoryImpl: DeliveryRepositoryImpl
    ): DeliveryRepository

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        routeRepositoryImpl: RouteRepositoryImpl
    ): RouteRepository

    @Binds
    @Singleton
    abstract fun bindTaskAssignmentRepository(
        taskAssignmentRepositoryImpl: TaskAssignmentRepositoryImpl
    ): TaskAssignmentRepository

    @Binds
    @Singleton
    abstract fun bindDeliveryTrackingRepository(
        deliveryTrackingRepositoryImpl: DeliveryTrackingRepositoryImpl
    ): DeliveryTrackingRepository
}