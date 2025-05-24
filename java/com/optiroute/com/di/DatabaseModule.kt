package com.optiroute.com.di

import android.content.Context
import com.optiroute.com.data.local.OptiRouteDatabase
import com.optiroute.com.data.local.dao.CustomerDao
import com.optiroute.com.data.local.dao.DepotDao
import com.optiroute.com.data.local.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module untuk menyediakan dependensi terkait database.
 *
 * Anotasi @Module menandakan bahwa ini adalah modul Hilt.
 * Anotasi @InstallIn(SingletonComponent::class) berarti bahwa dependensi yang disediakan
 * dalam modul ini akan memiliki scope Singleton dan tersedia di seluruh aplikasi.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Menyediakan instance singleton dari OptiRouteDatabase.
     *
     * @param context Context aplikasi, disediakan oleh Hilt melalui @ApplicationContext.
     * @return Instance singleton dari OptiRouteDatabase.
     */
    @Singleton // Memastikan hanya satu instance database yang dibuat.
    @Provides
    fun provideOptiRouteDatabase(@ApplicationContext context: Context): OptiRouteDatabase {
        return OptiRouteDatabase.getInstance(context)
    }

    /**
     * Menyediakan instance dari DepotDao.
     * Hilt akan menggunakan provideOptiRouteDatabase untuk mendapatkan instance database
     * yang kemudian digunakan untuk mendapatkan DepotDao.
     *
     * @param database Instance OptiRouteDatabase yang disediakan oleh Hilt.
     * @return Instance dari DepotDao.
     */
    @Provides
    @Singleton // DAO biasanya juga singleton karena terikat dengan database singleton
    fun provideDepotDao(database: OptiRouteDatabase): DepotDao {
        return database.depotDao()
    }

    /**
     * Menyediakan instance dari VehicleDao.
     *
     * @param database Instance OptiRouteDatabase yang disediakan oleh Hilt.
     * @return Instance dari VehicleDao.
     */
    @Provides
    @Singleton
    fun provideVehicleDao(database: OptiRouteDatabase): VehicleDao {
        return database.vehicleDao()
    }

    /**
     * Menyediakan instance dari CustomerDao.
     *
     * @param database Instance OptiRouteDatabase yang disediakan oleh Hilt.
     * @return Instance dari CustomerDao.
     */
    @Provides
    @Singleton
    fun provideCustomerDao(database: OptiRouteDatabase): CustomerDao {
        return database.customerDao()
    }
}
