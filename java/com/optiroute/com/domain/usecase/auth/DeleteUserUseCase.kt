package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.repository.AuthRepository
import com.optiroute.com.utils.Resource
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Resource<Boolean> {
        return authRepository.deleteUser(userId)
    }
}