package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Route
import com.optiroute.com.domain.models.RouteStatus

@TypeConverters(RouteConverters::class)
@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey
    val id: String,
    val customerIds: List<String>,
    val vehicleId: String,
    val depotId: String,
    val kurirId: String,
    val optimizedPath: List<Location>,
    val totalDistance: Double,
    val estimatedDuration: Long,
    val totalWeight: Double,
    val status: String,
    val startedAt: Long,
    val completedAt: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): Route {
        return Route(
            id = id,
            customerIds = customerIds,
            vehicleId = vehicleId,
            depotId = depotId,
            kurirId = kurirId,
            optimizedPath = optimizedPath,
            totalDistance = totalDistance,
            estimatedDuration = estimatedDuration,
            totalWeight = totalWeight,
            status = RouteStatus.valueOf(status),
            startedAt = startedAt,
            completedAt = completedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(route: Route): RouteEntity {
            return RouteEntity(
                id = route.id,
                customerIds = route.customerIds,
                vehicleId = route.vehicleId,
                depotId = route.depotId,
                kurirId = route.kurirId,
                optimizedPath = route.optimizedPath,
                totalDistance = route.totalDistance,
                estimatedDuration = route.estimatedDuration,
                totalWeight = route.totalWeight,
                status = route.status.name,
                startedAt = route.startedAt,
                completedAt = route.completedAt,
                createdAt = route.createdAt,
                updatedAt = route.updatedAt
            )
        }
    }
}

class RouteConverters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromLocationList(value: List<Location>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toLocationList(value: String): List<Location> {
        return Gson().fromJson(value, object : TypeToken<List<Location>>() {}.type)
    }
}