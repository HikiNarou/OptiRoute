package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.repository.AuthRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Resource<com.optiroute.com.domain.models.User> {
        return if (username.isBlank() || password.isBlank()) {
            Resource.Error("Username and password cannot be empty")
        } else {
            authRepository.login(username.trim(), password)
        }
    }
}