package com.optiroute.com.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.optiroute.com.data.local.dao.*
import com.optiroute.com.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        DepotEntity::class,
        VehicleEntity::class,
        CustomerEntity::class,
        DeliveryTaskEntity::class,
        RouteEntity::class,
        TaskAssignmentEntity::class,
        TaskOfferEntity::class,
        DeliveryTrackingEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    RouteConverters::class,
    DeliveryTrackingConverters::class
)
abstract class OptiRouteDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun depotDao(): DepotDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun customerDao(): CustomerDao
    abstract fun deliveryTaskDao(): DeliveryTaskDao
    abstract fun routeDao(): RouteDao
    abstract fun taskAssignmentDao(): TaskAssignmentDao
    abstract fun deliveryTrackingDao(): DeliveryTrackingDao

    companion object {
        @Volatile
        private var INSTANCE: OptiRouteDatabase? = null

        fun getDatabase(context: Context): OptiRouteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OptiRouteDatabase::class.java,
                    "optiroute_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}