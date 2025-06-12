package com.optiroute.com.data.local.dao

import androidx.room.*
import com.optiroute.com.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userType = :userType ORDER BY fullName ASC")
    fun getUsersByType(userType: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isActive = :isActive ORDER BY fullName ASC")
    fun getUsersByStatus(isActive: Boolean): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("UPDATE users SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, isActive: Boolean, updatedAt: Long)

    @Query("UPDATE users SET password = :password, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updatePassword(userId: String, password: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM users WHERE userType = :userType")
    suspend fun getUserCountByType(userType: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE isActive = 1")
    suspend fun getActiveUserCount(): Int
}