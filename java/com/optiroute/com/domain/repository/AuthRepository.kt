package com.optiroute.com.domain.repository

import  com.optiroute.com.domain.models.User
import com.optiroute.com.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Resource<User>
    suspend fun register(user: User, password: String): Resource<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    suspend fun updateUserProfile(user: User): Resource<User>
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Boolean>
    suspend fun getAllUsersByType(userType: String): Flow<List<User>>
    suspend fun updateUserStatus(userId: String, isActive: Boolean): Resource<Boolean>
    suspend fun deleteUser(userId: String): Resource<Boolean>
    suspend fun getUserById(userId: String): Resource<User>
}