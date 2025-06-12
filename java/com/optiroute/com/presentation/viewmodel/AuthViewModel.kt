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
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = loginUseCase(username, password)) {
                is Resource.Success -> {
                    _currentUser.value = result.data
                    _authState.value = AuthState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Login failed")
                }
                is Resource.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun register(
        username: String,
        email: String,
        fullName: String,
        password: String,
        userType: UserType
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val user = User(
                username = username,
                email = email,
                fullName = fullName,
                userType = userType
            )

            when (val result = registerUseCase(user, password)) {
                is Resource.Success -> {
                    _authState.value = AuthState.Success(result.data!!)
                }
                is Resource.Error -> {
                    _authState.value = AuthState.Error(result.message ?: "Registration failed")
                }
                is Resource.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _currentUser.value = null
            _authState.value = AuthState.Initial
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase()
            _currentUser.value = user
            if (user != null) {
                _authState.value = AuthState.Success(user)
            }
        }
    }

    fun clearAuthState() {
        _authState.value = AuthState.Initial
    }

    sealed class AuthState {
        object Initial : AuthState()
        object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}