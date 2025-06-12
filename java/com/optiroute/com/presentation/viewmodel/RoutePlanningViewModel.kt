package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.domain.repository.VehicleRepository
import com.optiroute.com.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutePlanningViewModel @Inject constructor(
    private val depotRepository: DepotRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _depots = MutableStateFlow<List<Depot>>(emptyList())
    val depots: StateFlow<List<Depot>> = _depots.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _state = MutableStateFlow<State>(State.Initial)
    val state: StateFlow<State> = _state.asStateFlow()

    fun loadDepots() {
        viewModelScope.launch {
            _state.value = State.Loading

            try {
                depotRepository.getAllDepots().collect { depotList ->
                    _depots.value = depotList
                    if (depotList.isEmpty()) {
                        _state.value = State.Error("No depots found. Please contact your admin to create depots.")
                    } else {
                        _state.value = State.Success
                    }
                }
            } catch (e: Exception) {
                _state.value = State.Error("Failed to load depots: ${e.message}")
                _depots.value = emptyList()
            }
        }
    }

    fun loadVehiclesByDepot(depotId: String) {
        viewModelScope.launch {
            _state.value = State.Loading

            try {
                vehicleRepository.getVehiclesByDepot(depotId).collect { vehicleList ->
                    // Filter only available vehicles
                    val availableVehicles = vehicleList.filter { it.isAvailable }
                    _vehicles.value = availableVehicles

                    if (availableVehicles.isEmpty()) {
                        _state.value = State.Error("No available vehicles found for this depot. Please contact your admin.")
                    } else {
                        _state.value = State.Success
                    }
                }
            } catch (e: Exception) {
                _state.value = State.Error("Failed to load vehicles: ${e.message}")
                _vehicles.value = emptyList()
            }
        }
    }

    fun refreshData() {
        loadDepots()
    }

    fun clearState() {
        _state.value = State.Initial
    }

    sealed class State {
        object Initial : State()
        object Loading : State()
        object Success : State()
        data class Error(val message: String) : State()
    }
}