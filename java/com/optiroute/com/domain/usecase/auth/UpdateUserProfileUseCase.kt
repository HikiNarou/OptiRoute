package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.repository.AuthRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User): Resource<User> {
        return when {
            user.username.isBlank() -> Resource.Error("Username cannot be empty")
            user.email.isBlank() -> Resource.Error("Email cannot be empty")
            user.fullName.isBlank() -> Resource.Error("Full name cannot be empty")
            !isValidEmail(user.email) -> Resource.Error("Invalid email format")
            else -> authRepository.updateUserProfile(user)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}