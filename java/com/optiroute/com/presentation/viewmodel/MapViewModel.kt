package com.optiroute.com.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Route
import com.optiroute.com.utils.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _mapState = MutableStateFlow<MapState>(MapState.Initial)
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _currentRoute = MutableStateFlow<Route?>(null)
    val currentRoute: StateFlow<Route?> = _currentRoute.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            _mapState.value = MapState.Loading

            try {
                val location = LocationUtils.getCurrentLocation(context)
                _currentLocation.value = location
                _mapState.value = MapState.Success
            } catch (e: Exception) {
                _mapState.value = MapState.Error("Failed to get location: ${e.message}")
            }
        }
    }

    fun loadRouteData(route: Route) {
        viewModelScope.launch {
            _mapState.value = MapState.Loading

            try {
                _currentRoute.value = route
                _mapState.value = MapState.Success
            } catch (e: Exception) {
                _mapState.value = MapState.Error("Failed to load route: ${e.message}")
            }
        }
    }

    fun startNavigation() {
        _isNavigating.value = true
    }

    fun stopNavigation() {
        _isNavigating.value = false
    }

    fun updateLocation(location: Location) {
        _currentLocation.value = location
    }

    fun clearMapData() {
        _currentRoute.value = null
        _currentLocation.value = null
        _isNavigating.value = false
        _mapState.value = MapState.Initial
    }

    sealed class MapState {
        object Initial : MapState()
        object Loading : MapState()
        object Success : MapState()
        data class Error(val message: String) : MapState()
    }
}