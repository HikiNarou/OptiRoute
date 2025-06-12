package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.Customer
import com.optiroute.com.domain.models.CustomerPriority
import com.optiroute.com.domain.models.CustomerStatus
import com.optiroute.com.domain.models.Location
import com.optiroute.com.domain.usecase.customer.CreateCustomerUseCase
import com.optiroute.com.domain.usecase.customer.GetCustomersByUmkmUseCase
import com.optiroute.com.domain.usecase.customer.UpdateCustomerStatusUseCase
import com.optiroute.com.utils.Resource
import com.optiroute.com.utils.generateId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UmkmViewModel @Inject constructor(
    private val createCustomerUseCase: CreateCustomerUseCase,
    private val getCustomersByUmkmUseCase: GetCustomersByUmkmUseCase,
    private val updateCustomerStatusUseCase: UpdateCustomerStatusUseCase
) : ViewModel() {

    private val _customerState = MutableStateFlow<CustomerState>(CustomerState.Initial)
    val customerState: StateFlow<CustomerState> = _customerState.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _customerFormState = MutableStateFlow(CustomerFormState())
    val customerFormState: StateFlow<CustomerFormState> = _customerFormState.asStateFlow()

    fun loadCustomers(umkmId: String) {
        viewModelScope.launch {
            getCustomersByUmkmUseCase(umkmId).collect { customerList ->
                _customers.value = customerList
            }
        }
    }

    fun createCustomer(
        name: String,
        location: Location,
        phoneNumber: String,
        email: String,
        itemType: String,
        itemWeight: Double,
        weightUnit: String,
        notes: String,
        priority: CustomerPriority,
        umkmId: String
    ) {
        viewModelScope.launch {
            _customerState.value = CustomerState.Loading

            val customer = Customer(
                id = generateId(),
                name = name,
                location = location,
                phoneNumber = phoneNumber,
                email = email,
                itemType = itemType,
                itemWeight = itemWeight,
                weightUnit = weightUnit,
                notes = notes,
                priority = priority,
                umkmId = umkmId,
                status = CustomerStatus.PENDING
            )

            when (val result = createCustomerUseCase(customer)) {
                is Resource.Success -> {
                    _customerState.value = CustomerState.Success("Customer created successfully")
                    clearCustomerForm()
                }
                is Resource.Error -> {
                    _customerState.value = CustomerState.Error(result.message ?: "Failed to create customer")
                }
                is Resource.Loading -> {
                    _customerState.value = CustomerState.Loading
                }
            }
        }
    }

    fun updateCustomerForm(formState: CustomerFormState) {
        _customerFormState.value = formState
    }

    fun clearCustomerForm() {
        _customerFormState.value = CustomerFormState()
    }

    fun clearCustomerState() {
        _customerState.value = CustomerState.Initial
    }

    sealed class CustomerState {
        object Initial : CustomerState()
        object Loading : CustomerState()
        data class Success(val message: String) : CustomerState()
        data class Error(val message: String) : CustomerState()
    }

    data class CustomerFormState(
        val name: String = "",
        val address: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val phoneNumber: String = "",
        val email: String = "",
        val itemType: String = "",
        val itemWeight: String = "",
        val weightUnit: String = "kg",
        val notes: String = "",
        val priority: CustomerPriority = CustomerPriority.NORMAL
    )
}