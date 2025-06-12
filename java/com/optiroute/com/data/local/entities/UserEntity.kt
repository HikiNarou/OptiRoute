package com.optiroute.com.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val userType: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            username = username,
            email = email,
            fullName = fullName,
            userType = UserType.valueOf(userType),
            isActive = isActive,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(user: User, password: String): UserEntity {
            return UserEntity(
                id = user.id,
                username = user.username,
                email = user.email,
                password = password,
                fullName = user.fullName,
                userType = user.userType.name,
                isActive = user.isActive,
                createdAt = user.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}