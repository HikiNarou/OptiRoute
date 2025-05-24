package com.optiroute.com.di

import com.optiroute.com.data.repository.CustomerRepositoryImpl
import com.optiroute.com.data.repository.DepotRepositoryImpl
import com.optiroute.com.data.repository.VehicleRepositoryImpl
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module untuk menyediakan implementasi Repository.
 *
 * Anotasi @Module menandakan bahwa ini adalah modul Hilt.
 * Anotasi @InstallIn(SingletonComponent::class) berarti bahwa dependensi yang disediakan
 * dalam modul ini akan memiliki scope Singleton dan tersedia di seluruh aplikasi.
 *
 * Metode yang dianotasi dengan @Binds digunakan untuk memberitahu Hilt implementasi mana
 * yang harus digunakan ketika sebuah interface diminta sebagai dependensi.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Mengikat implementasi DepotRepositoryImpl ke interface DepotRepository.
     * Ketika DepotRepository diinjeksi, Hilt akan menyediakan instance dari DepotRepositoryImpl.
     *
     * @param depotRepositoryImpl Implementasi dari DepotRepository.
     * @return Instance yang diikat ke interface DepotRepository.
     */
    @Binds
    @Singleton // Pastikan implementasi repository juga singleton
    abstract fun bindDepotRepository(
        depotRepositoryImpl: DepotRepositoryImpl
    ): DepotRepository

    /**
     * Mengikat implementasi VehicleRepositoryImpl ke interface VehicleRepository.
     *
     * @param vehicleRepositoryImpl Implementasi dari VehicleRepository.
     * @return Instance yang diikat ke interface VehicleRepository.
     */
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository

    /**
     * Mengikat implementasi CustomerRepositoryImpl ke interface CustomerRepository.
     *
     * @param customerRepositoryImpl Implementasi dari CustomerRepository.
     * @return Instance yang diikat ke interface CustomerRepository.
     */
    @Binds
    @Singleton
    abstract fun bindCustomerRepository(
        customerRepositoryImpl: CustomerRepositoryImpl
    ): CustomerRepository
}
