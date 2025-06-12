package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import com.optiroute.com.domain.usecase.auth.*
import com.optiroute.com.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val getAllUsersByTypeUseCase: GetAllUsersByTypeUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _userManagementState = MutableStateFlow<UserManagementState>(UserManagementState.Initial)
    val userManagementState: StateFlow<UserManagementState> = _userManagementState.asStateFlow()

    fun loadAllUsers() {
        viewModelScope.launch {
            _userManagementState.value = UserManagementState.Loading

            try {
                val allUsers = mutableListOf<User>()

                // Load users of all types
                getAllUsersByTypeUseCase(UserType.UMKM.name).collect { umkmUsers ->
                    allUsers.addAll(umkmUsers)
                }

                getAllUsersByTypeUseCase(UserType.ADMIN.name).collect { adminUsers ->
                    allUsers.addAll(adminUsers)
                }

                getAllUsersByTypeUseCase(UserType.KURIR.name).collect { kurirUsers ->
                    allUsers.addAll(kurirUsers)
                }

                _users.value = allUsers.distinctBy { it.id }
                _userManagementState.value = UserManagementState.Initial
            } catch (e: Exception) {
                _userManagementState.value = UserManagementState.Error("Failed to load users: ${e.message}")
            }
        }
    }

    fun createUser(
        username: String,
        email: String,
        fullName: String,
        password: String,
        userType: UserType
    ) {
        viewModelScope.launch {
            _userManagementState.value = UserManagementState.Loading

            val user = User(
                username = username,
                email = email,
                fullName = fullName,
                userType = userType
            )

            when (val result = registerUseCase(user, password)) {
                is Resource.Success -> {
                    _userManagementState.value = UserManagementState.Success("User created successfully")
                    loadAllUsers()
                }
                is Resource.Error -> {
                    _userManagementState.value = UserManagementState.Error(result.message ?: "Failed to create user")
                }
                is Resource.Loading -> {
                    _userManagementState.value = UserManagementState.Loading
                }
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _userManagementState.value = UserManagementState.Loading

            when (val result = updateUserProfileUseCase(user)) {
                is Resource.Success -> {
                    _userManagementState.value = UserManagementState.Success("User updated successfully")
                    loadAllUsers()
                }
                is Resource.Error -> {
                    _userManagementState.value = UserManagementState.Error(result.message ?: "Failed to update user")
                }
                is Resource.Loading -> {
                    _userManagementState.value = UserManagementState.Loading
                }
            }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            _userManagementState.value = UserManagementState.Loading

            when (val result = updateUserStatusUseCase(userId, isActive)) {
                is Resource.Success -> {
                    _userManagementState.value = UserManagementState.Success(
                        if (isActive) "User activated successfully" else "User deactivated successfully"
                    )
                    loadAllUsers()
                }
                is Resource.Error -> {
                    _userManagementState.value = UserManagementState.Error(result.message ?: "Failed to update user status")
                }
                is Resource.Loading -> {
                    _userManagementState.value = UserManagementState.Loading
                }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _userManagementState.value = UserManagementState.Loading

            when (val result = deleteUserUseCase(userId)) {
                is Resource.Success -> {
                    _userManagementState.value = UserManagementState.Success("User deleted successfully")
                    loadAllUsers()
                }
                is Resource.Error -> {
                    _userManagementState.value = UserManagementState.Error(result.message ?: "Failed to delete user")
                }
                is Resource.Loading -> {
                    _userManagementState.value = UserManagementState.Loading
                }
            }
        }
    }

    fun clearState() {
        _userManagementState.value = UserManagementState.Initial
    }

    sealed class UserManagementState {
        object Initial : UserManagementState()
        object Loading : UserManagementState()
        data class Success(val message: String) : UserManagementState()
        data class Error(val message: String) : UserManagementState()
    }
}