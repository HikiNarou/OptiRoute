package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.usecase.auth.UpdateUserProfileUseCase
import com.optiroute.com.domain.usecase.auth.ChangePasswordUseCase
import com.optiroute.com.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            when (val result = updateUserProfileUseCase(user)) {
                is Resource.Success -> {
                    _profileState.value = ProfileState.Success("Profile updated successfully")
                }
                is Resource.Error -> {
                    _profileState.value = ProfileState.Error(result.message ?: "Failed to update profile")
                }
                is Resource.Loading -> {
                    _profileState.value = ProfileState.Loading
                }
            }
        }
    }

    fun updateProfileWithPassword(
        user: User,
        currentPassword: String,
        newPassword: String
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            // First update the profile
            when (val profileResult = updateUserProfileUseCase(user)) {
                is Resource.Success -> {
                    // Then change the password
                    when (val passwordResult = changePasswordUseCase(currentPassword, newPassword)) {
                        is Resource.Success -> {
                            _profileState.value = ProfileState.Success("Profile and password updated successfully")
                        }
                        is Resource.Error -> {
                            _profileState.value = ProfileState.Error("Profile updated but password change failed: ${passwordResult.message}")
                        }
                        is Resource.Loading -> {
                            _profileState.value = ProfileState.Loading
                        }
                    }
                }
                is Resource.Error -> {
                    _profileState.value = ProfileState.Error(profileResult.message ?: "Failed to update profile")
                }
                is Resource.Loading -> {
                    _profileState.value = ProfileState.Loading
                }
            }
        }
    }

    fun clearState() {
        _profileState.value = ProfileState.Initial
    }

    sealed class ProfileState {
        object Initial : ProfileState()
        object Loading : ProfileState()
        data class Success(val message: String) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }
}