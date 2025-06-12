package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.*
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.domain.repository.DeliveryRepository
import com.optiroute.com.domain.repository.RouteRepository
import com.optiroute.com.domain.repository.TaskAssignmentRepository
import com.optiroute.com.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KurirViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val deliveryRepository: DeliveryRepository,
    private val routeRepository: RouteRepository,
    private val taskAssignmentRepository: TaskAssignmentRepository
) : ViewModel() {

    private val _pendingCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val pendingCustomers: StateFlow<List<Customer>> = _pendingCustomers.asStateFlow()

    private val _assignedCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val assignedCustomers: StateFlow<List<Customer>> = _assignedCustomers.asStateFlow()

    private val _deliveryTasks = MutableStateFlow<List<DeliveryTask>>(emptyList())
    val deliveryTasks: StateFlow<List<DeliveryTask>> = _deliveryTasks.asStateFlow()

    private val _optimizedRoute = MutableStateFlow<Route?>(null)
    val optimizedRoute: StateFlow<Route?> = _optimizedRoute.asStateFlow()

    private val _kurirState = MutableStateFlow<KurirState>(KurirState.Initial)
    val kurirState: StateFlow<KurirState> = _kurirState.asStateFlow()

    private val _selectedCustomerIds = MutableStateFlow<List<String>>(emptyList())
    private val _selectedDepotId = MutableStateFlow<String>("")
    private val _selectedVehicleId = MutableStateFlow<String>("")

    fun loadPendingCustomers() {
        viewModelScope.launch {
            customerRepository.getCustomersByStatus(CustomerStatus.PENDING).collect { customers ->
                _pendingCustomers.value = customers
            }
        }
    }

    fun loadAssignedCustomers(kurirId: String) {
        viewModelScope.launch {
            customerRepository.getCustomersByKurir(kurirId).collect { customers ->
                _assignedCustomers.value = customers.filter { it.status == CustomerStatus.ASSIGNED }
            }
        }
    }

    fun loadDeliveryTasks() {
        viewModelScope.launch {
            deliveryRepository.getAllDeliveryTasks().collect { tasks ->
                _deliveryTasks.value = tasks
            }
        }
    }

    fun acceptTask(customerId: String, kurirId: String) {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                // First, assign customer to kurir
                when (val assignResult = customerRepository.assignCustomerToKurir(customerId, kurirId)) {
                    is Resource.Success -> {
                        // Then accept the task assignment
                        when (val acceptResult = taskAssignmentRepository.acceptTask(customerId, kurirId)) {
                            is Resource.Success -> {
                                _kurirState.value = KurirState.Success("Task accepted successfully! You can now plan your route.")
                                loadPendingCustomers()
                                loadAssignedCustomers(kurirId)
                            }
                            is Resource.Error -> {
                                _kurirState.value = KurirState.Error(acceptResult.message ?: "Failed to accept task")
                            }
                            is Resource.Loading -> {
                                _kurirState.value = KurirState.Loading
                            }
                        }
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(assignResult.message ?: "Failed to assign task")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun rejectTask(customerId: String, reason: String) {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                when (val result = taskAssignmentRepository.rejectTaskByKurir(customerId, "", reason)) {
                    is Resource.Success -> {
                        _kurirState.value = KurirState.Success("Task declined. The task will be available for other couriers.")
                        loadPendingCustomers()
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(result.message ?: "Failed to decline task")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun completeDelivery(taskId: String, notes: String) {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                when (val result = deliveryRepository.completeDelivery(taskId, notes)) {
                    is Resource.Success -> {
                        _kurirState.value = KurirState.Success("Delivery completed successfully")
                        loadDeliveryTasks()
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(result.message ?: "Failed to complete delivery")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error: ${e.message}")
            }
        }
    }

    fun selectCustomersForRoute(customerIds: List<String>) {
        _selectedCustomerIds.value = customerIds
    }

    fun selectDepot(depotId: String) {
        _selectedDepotId.value = depotId
    }

    fun selectVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }

    fun optimizeRoute() {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                val customers = assignedCustomers.value.filter { it.id in _selectedCustomerIds.value }

                if (customers.isEmpty()) {
                    _kurirState.value = KurirState.Error("No customers selected for route optimization")
                    return@launch
                }

                if (_selectedDepotId.value.isEmpty()) {
                    _kurirState.value = KurirState.Error("Please select a depot")
                    return@launch
                }

                if (_selectedVehicleId.value.isEmpty()) {
                    _kurirState.value = KurirState.Error("Please select a vehicle")
                    return@launch
                }

                when (val result = routeRepository.optimizeRoute(
                    customers = customers,
                    depotId = _selectedDepotId.value,
                    vehicleId = _selectedVehicleId.value
                )) {
                    is Resource.Success -> {
                        _optimizedRoute.value = result.data
                        _kurirState.value = KurirState.RouteOptimized("Route optimized successfully! Ready to start delivery.")
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(result.message ?: "Failed to optimize route")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error during route optimization: ${e.message}")
            }
        }
    }

    fun startRoute(routeId: String) {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                when (val result = routeRepository.startRoute(routeId)) {
                    is Resource.Success -> {
                        _kurirState.value = KurirState.Success("Route started successfully! Begin your deliveries.")

                        // Update assigned customers to IN_DELIVERY status
                        assignedCustomers.value.forEach { customer ->
                            if (customer.id in _selectedCustomerIds.value) {
                                viewModelScope.launch {
                                    customerRepository.updateCustomerStatus(customer.id, CustomerStatus.IN_DELIVERY)
                                }
                            }
                        }

                        loadDeliveryTasks()
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(result.message ?: "Failed to start route")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error starting route: ${e.message}")
            }
        }
    }

    fun updateDeliveryStatus(taskId: String, status: DeliveryStatus) {
        viewModelScope.launch {
            _kurirState.value = KurirState.Loading

            try {
                when (val result = deliveryRepository.updateDeliveryStatus(taskId, status)) {
                    is Resource.Success -> {
                        _kurirState.value = KurirState.Success("Delivery status updated successfully")
                        loadDeliveryTasks()
                    }
                    is Resource.Error -> {
                        _kurirState.value = KurirState.Error(result.message ?: "Failed to update status")
                    }
                    is Resource.Loading -> {
                        _kurirState.value = KurirState.Loading
                    }
                }
            } catch (e: Exception) {
                _kurirState.value = KurirState.Error("Unexpected error updating delivery status: ${e.message}")
            }
        }
    }

    fun refreshAllData(kurirId: String) {
        viewModelScope.launch {
            loadPendingCustomers()
            loadAssignedCustomers(kurirId)
            loadDeliveryTasks()
        }
    }

    fun clearSelectedItems() {
        _selectedCustomerIds.value = emptyList()
        _selectedDepotId.value = ""
        _selectedVehicleId.value = ""
        _optimizedRoute.value = null
    }

    fun clearKurirState() {
        _kurirState.value = KurirState.Initial
    }

    sealed class KurirState {
        object Initial : KurirState()
        object Loading : KurirState()
        data class Success(val message: String) : KurirState()
        data class Error(val message: String) : KurirState()
        data class RouteOptimized(val message: String) : KurirState()
    }
}