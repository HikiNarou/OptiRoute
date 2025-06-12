package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}