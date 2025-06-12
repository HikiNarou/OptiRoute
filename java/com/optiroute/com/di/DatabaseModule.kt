package com.optiroute.com.di

import android.content.Context
import androidx.room.Room
import com.optiroute.com.data.local.database.OptiRouteDatabase
import com.optiroute.com.data.local.dao.*
import com.optiroute.com.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOptiRouteDatabase(
        @ApplicationContext context: Context
    ): OptiRouteDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            OptiRouteDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: OptiRouteDatabase): UserDao = database.userDao()

    @Provides
    fun provideDepotDao(database: OptiRouteDatabase): DepotDao = database.depotDao()

    @Provides
    fun provideVehicleDao(database: OptiRouteDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideCustomerDao(database: OptiRouteDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideRouteDao(database: OptiRouteDatabase): RouteDao = database.routeDao()

    @Provides
    fun provideDeliveryTaskDao(database: OptiRouteDatabase): DeliveryTaskDao = database.deliveryTaskDao()

    @Provides
    fun provideTaskAssignmentDao(database: OptiRouteDatabase): TaskAssignmentDao = database.taskAssignmentDao()

    @Provides
    fun provideDeliveryTrackingDao(database: OptiRouteDatabase): DeliveryTrackingDao = database.deliveryTrackingDao()
}