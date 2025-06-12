package com.optiroute.com.domain.usecase.auth

import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersByTypeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userType: String): Flow<List<User>> {
        return authRepository.getAllUsersByType(userType)
    }
}