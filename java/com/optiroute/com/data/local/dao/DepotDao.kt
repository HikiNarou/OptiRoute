package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.DepotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepotDao {

    @Query("SELECT * FROM depots WHERE id = :id")
    suspend fun getDepotById(id: String): DepotEntity?

    @Query("SELECT * FROM depots WHERE adminId = :adminId AND isActive = 1")
    fun getDepotsByAdmin(adminId: String): Flow<List<DepotEntity>>

    @Query("SELECT * FROM depots WHERE isActive = 1")
    fun getAllActiveDepots(): Flow<List<DepotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepot(depot: DepotEntity)

    @Update
    suspend fun updateDepot(depot: DepotEntity)

    @Delete
    suspend fun deleteDepot(depot: DepotEntity)

    @Query("UPDATE depots SET isActive = :isActive WHERE id = :id")
    suspend fun updateDepotStatus(id: String, isActive: Boolean)

    @Query("DELETE FROM depots WHERE id = :depotId")
    suspend fun deleteDepotById(depotId: String)
}