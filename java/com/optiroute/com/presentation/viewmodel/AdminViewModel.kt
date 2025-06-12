package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.Depot
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.models.Vehicle
import com.optiroute.com.domain.models.VehicleType
import com.optiroute.com.domain.models.MaintenanceStatus
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
class AdminViewModel @Inject constructor(
    private val depotRepository: DepotRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _depots = MutableStateFlow<List<Depot>>(emptyList())
    val depots: StateFlow<List<Depot>> = _depots.asStateFlow()

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    private val _adminState = MutableStateFlow<AdminState>(AdminState.Initial)
    val adminState: StateFlow<AdminState> = _adminState.asStateFlow()

    fun loadDepots() {
        viewModelScope.launch {
            depotRepository.getAllDepots().collect { depotList ->
                _depots.value = depotList
            }
        }
    }

    fun loadVehicles(depotId: String) {
        viewModelScope.launch {
            vehicleRepository.getVehiclesByDepot(depotId).collect { vehicleList ->
                _vehicles.value = vehicleList
            }
        }
    }

    fun loadAllVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect { vehicleList ->
                _vehicles.value = vehicleList
            }
        }
    }

    fun createDepot(
        name: String,
        location: Location,
        capacity: Double,
        operationalHours: String
    ) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            val depot = Depot(
                name = name,
                location = location,
                capacity = capacity,
                operationalHours = operationalHours,
                updatedAt = System.currentTimeMillis()
            )

            when (val result = depotRepository.createDepot(depot)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Depot created successfully")
                    loadDepots()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to create depot")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun createVehicle(
        licensePlate: String,
        vehicleType: VehicleType,
        capacity: Double,
        capacityUnit: String,
        fuelConsumption: Double,
        depotId: String,
        kurirId: String
    ) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            val vehicle = Vehicle(
                licensePlate = licensePlate,
                vehicleType = vehicleType,
                capacity = capacity,
                capacityUnit = capacityUnit,
                fuelConsumption = fuelConsumption,
                depotId = depotId,
                kurirId = kurirId,
                isAvailable = kurirId.isEmpty(),
                maintenanceStatus = MaintenanceStatus.OPERATIONAL
            )

            when (val result = vehicleRepository.createVehicle(vehicle)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Vehicle created successfully")
                    loadAllVehicles()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to create vehicle")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun updateDepot(depot: Depot) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = depotRepository.updateDepot(depot)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Depot updated successfully")
                    loadDepots()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to update depot")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun deleteDepot(depotId: String) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = depotRepository.deleteDepot(depotId)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Depot deleted successfully")
                    loadDepots()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to delete depot")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = vehicleRepository.updateVehicle(vehicle)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Vehicle updated successfully")
                    loadAllVehicles()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to update vehicle")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = vehicleRepository.deleteVehicle(vehicleId)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Vehicle deleted successfully")
                    loadAllVehicles()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to delete vehicle")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun toggleVehicleAvailability(vehicleId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = vehicleRepository.updateVehicleAvailability(vehicleId, isAvailable)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success(
                        if (isAvailable) "Vehicle enabled successfully" else "Vehicle disabled successfully"
                    )
                    loadAllVehicles()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to update vehicle availability")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun updateVehicleMaintenanceStatus(vehicleId: String, status: MaintenanceStatus) {
        viewModelScope.launch {
            _adminState.value = AdminState.Loading

            when (val result = vehicleRepository.updateMaintenanceStatus(vehicleId, status)) {
                is Resource.Success -> {
                    _adminState.value = AdminState.Success("Maintenance status updated successfully")
                    loadAllVehicles()
                }
                is Resource.Error -> {
                    _adminState.value = AdminState.Error(result.message ?: "Failed to update maintenance status")
                }
                is Resource.Loading -> {
                    _adminState.value = AdminState.Loading
                }
            }
        }
    }

    fun getVehicleTypeDisplayName(vehicleType: VehicleType): String {
        return when (vehicleType) {
            VehicleType.MOTORCYCLE -> "Motorcycle"
            VehicleType.CAR -> "Car"
            VehicleType.VAN -> "Van"
            VehicleType.TRUCK -> "Truck"
            VehicleType.PICKUP -> "Pickup"
        }
    }

    fun clearAdminState() {
        _adminState.value = AdminState.Initial
    }

    sealed class AdminState {
        object Initial : AdminState()
        object Loading : AdminState()
        data class Success(val message: String) : AdminState()
        data class Error(val message: String) : AdminState()
    }
}