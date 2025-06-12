package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.repository.AuthRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(oldPassword: String, newPassword: String): Resource<Boolean> {
        return when {
            oldPassword.isBlank() -> Resource.Error("Current password is required")
            newPassword.isBlank() -> Resource.Error("New password is required")
            newPassword.length < 6 -> Resource.Error("New password must be at least 6 characters")
            oldPassword == newPassword -> Resource.Error("New password must be different from current password")
            else -> authRepository.changePassword(oldPassword, newPassword)
        }
    }
}