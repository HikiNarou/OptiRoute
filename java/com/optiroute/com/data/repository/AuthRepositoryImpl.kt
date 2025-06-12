package com.optiroute.com.data.repository

import com.optiroute.com.data.local.dao.UserDao
import com.optiroute.com.data.local.entities.UserEntity
import com.optiroute.com.data.preferences.UserPreferences
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.repository.AuthRepository
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override fun isUserLoggedIn(): Flow<Boolean> {
        return userPreferences.getCurrentUser().map { it != null }
    }

    override suspend fun login(username: String, password: String): Resource<User> {
        return try {
            val userEntity = userDao.getUserByUsername(username)

            if (userEntity != null) {
                // Simple password verification (in production, use proper hashing)
                if (userEntity.password == password && userEntity.isActive) {
                    val user = userEntity.toDomainModel()
                    userPreferences.saveCurrentUser(user)
                    Resource.Success(user)
                } else {
                    Resource.Error("Invalid credentials or account is inactive")
                }
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error("Login failed: ${e.message}")
        }
    }

    override suspend fun register(user: User, password: String): Resource<User> {
        return try {
            // Check if username already exists
            val existingUser = userDao.getUserByUsername(user.username)
            if (existingUser != null) {
                return Resource.Error("Username already exists")
            }

            // Check if email already exists
            val existingEmail = userDao.getUserByEmail(user.email)
            if (existingEmail != null) {
                return Resource.Error("Email already exists")
            }

            val userEntity = UserEntity.fromDomainModel(
                user.copy(id = generateId()),
                password
            )

            userDao.insertUser(userEntity)
            Resource.Success(userEntity.toDomainModel())
        } catch (e: Exception) {
            Resource.Error("Registration failed: ${e.message}")
        }
    }

    override suspend fun logout() {
        userPreferences.clearCurrentUser()
    }

    override suspend fun getCurrentUser(): User? {
        return userPreferences.getCurrentUser().firstOrNull()
    }

    override suspend fun updateUserProfile(user: User): Resource<User> {
        return try {
            val existingEntity = userDao.getUserById(user.id)
            if (existingEntity != null) {
                val updatedEntity = existingEntity.copy(
                    username = user.username,
                    email = user.email,
                    fullName = user.fullName,
                    updatedAt = System.currentTimeMillis()
                )
                userDao.updateUser(updatedEntity)

                // Update preferences if this is the current user
                val currentUser = getCurrentUser()
                if (currentUser?.id == user.id) {
                    userPreferences.saveCurrentUser(updatedEntity.toDomainModel())
                }

                Resource.Success(updatedEntity.toDomainModel())
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to update profile: ${e.message}")
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Boolean> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                val userEntity = userDao.getUserById(currentUser.id)
                if (userEntity != null && userEntity.password == oldPassword) {
                    val updatedEntity = userEntity.copy(
                        password = newPassword,
                        updatedAt = System.currentTimeMillis()
                    )
                    userDao.updateUser(updatedEntity)
                    Resource.Success(true)
                } else {
                    Resource.Error("Current password is incorrect")
                }
            } else {
                Resource.Error("No user logged in")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to change password: ${e.message}")
        }
    }

    override suspend fun getAllUsersByType(userType: String): Flow<List<User>> {
        return userDao.getUsersByType(userType).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateUserStatus(userId: String, isActive: Boolean): Resource<Boolean> {
        return try {
            userDao.updateUserStatus(userId, isActive, System.currentTimeMillis())
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to update user status: ${e.message}")
        }
    }

    override suspend fun deleteUser(userId: String): Resource<Boolean> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser?.id == userId) {
                return Resource.Error("Cannot delete currently logged in user")
            }

            userDao.deleteUserById(userId)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error("Failed to delete user: ${e.message}")
        }
    }

    override suspend fun getUserById(userId: String): Resource<User> {
        return try {
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null) {
                Resource.Success(userEntity.toDomainModel())
            } else {
                Resource.Error("User not found")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to get user: ${e.message}")
        }
    }
}