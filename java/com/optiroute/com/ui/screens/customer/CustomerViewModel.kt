package com.optiroute.com.ui.screens.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.R
import com.optiroute.com.data.local.entity.CustomerEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel untuk CustomersScreen dan AddEditCustomerScreen.
 * Mengelola state dan logika bisnis terkait data pelanggan.
 */
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // State untuk daftar pelanggan
    private val _customersListState = MutableStateFlow<CustomerListUiState>(CustomerListUiState.Loading)
    val customersListState: StateFlow<CustomerListUiState> = _customersListState.asStateFlow()

    // State untuk form tambah/ubah pelanggan
    private val _customerFormState = MutableStateFlow(CustomerFormState())
    val customerFormState: StateFlow<CustomerFormState> = _customerFormState.asStateFlow()

    // State untuk pelanggan yang sedang diedit (jika ada)
    private val _editingCustomer = MutableStateFlow<CustomerEntity?>(null)

    // SharedFlow untuk event UI sekali jalan
    private val _uiEvent = MutableSharedFlow<CustomerUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.d("CustomerViewModel initialized")
        loadAllCustomers()
    }

    private fun loadAllCustomers() {
        viewModelScope.launch {
            _customersListState.value = CustomerListUiState.Loading
            customerRepository.getAllCustomers()
                .catch { e ->
                    Timber.e(e, "Error loading customers list")
                    _customersListState.value = CustomerListUiState.Error(R.string.error_occurred)
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageResId = R.string.error_occurred))
                }
                .collectLatest { customers ->
                    Timber.i("Customers loaded: ${customers.size} items")
                    _customersListState.value = if (customers.isEmpty()) {
                        CustomerListUiState.Empty
                    } else {
                        CustomerListUiState.Success(customers)
                    }
                }
        }
    }

    /**
     * Mempersiapkan form untuk menambah pelanggan baru.
     */
    fun prepareNewCustomerForm() {
        _editingCustomer.value = null
        _customerFormState.value = CustomerFormState(isEditMode = false)
        Timber.d("Prepared form for new customer.")
    }

    /**
     * Mempersiapkan form untuk mengedit pelanggan yang sudah ada.
     * @param customerId ID pelanggan yang akan diedit.
     */
    fun loadCustomerForEditing(customerId: Int) {
        viewModelScope.launch {
            customerRepository.getCustomerById(customerId).collectLatest { customerEntity ->
                if (customerEntity != null) {
                    _editingCustomer.value = customerEntity
                    _customerFormState.value = CustomerFormState(
                        id = customerEntity.id,
                        name = customerEntity.name,
                        address = customerEntity.address ?: "",
                        demand = customerEntity.demand.toString(),
                        selectedLocation = customerEntity.location,
                        notes = customerEntity.notes ?: "",
                        isEditMode = true
                    )
                    Timber.d("Loaded customer for editing: ${customerEntity.name}")
                } else {
                    Timber.w("Customer with ID $customerId not found for editing.")
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageResId = R.string.error_occurred))
                    _uiEvent.emit(CustomerUiEvent.NavigateBack) // Kembali jika pelanggan tidak ditemukan
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _customerFormState.update { it.copy(name = name, nameError = null) }
    }

    fun onAddressChange(address: String) {
        _customerFormState.update { it.copy(address = address) }
    }

    fun onDemandChange(demand: String) {
        _customerFormState.update { it.copy(demand = demand, demandError = null) }
    }

    fun onNotesChange(notes: String) {
        _customerFormState.update { it.copy(notes = notes) }
    }

    fun onLocationSelected(latLng: LatLng) {
        Timber.d("Location selected/updated for customer: $latLng")
        _customerFormState.update { it.copy(selectedLocation = latLng, locationError = null) }
    }

    fun saveCustomer() {
        val currentForm = _customerFormState.value
        if (!validateForm(currentForm)) {
            return
        }

        val customerEntity = CustomerEntity(
            id = if (currentForm.isEditMode) currentForm.id!! else 0,
            name = currentForm.name.trim(),
            address = currentForm.address.trim().takeIf { it.isNotBlank() },
            demand = currentForm.demand.toDoubleOrNull() ?: 0.0, // Validasi sudah memastikan ini >= 0
            location = currentForm.selectedLocation!!, // Validasi memastikan ini tidak null
            notes = currentForm.notes.trim().takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _customerFormState.update { it.copy(isSaving = true) }
            val result = if (currentForm.isEditMode) {
                customerRepository.updateCustomer(customerEntity)
            } else {
                customerRepository.addCustomer(customerEntity)
            }
            _customerFormState.update { it.copy(isSaving = false) }

            when (result) {
                is AppResult.Success -> {
                    val messageRes = if (currentForm.isEditMode) R.string.customer_updated_successfully else R.string.customer_added_successfully
                    Timber.i("Customer saved successfully. Mode: ${if (currentForm.isEditMode) "Edit" else "Add"}")
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageResId = messageRes))
                    _uiEvent.emit(CustomerUiEvent.NavigateBack)
                    loadAllCustomers() // Muat ulang daftar setelah berhasil
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Error saving customer: ${result.message}")
                    val errorMessage = result.message ?: "Gagal menyimpan pelanggan"
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageText = errorMessage))
                }
            }
        }
    }

    private fun validateForm(form: CustomerFormState): Boolean {
        var isValid = true
        if (form.name.isBlank()) {
            _customerFormState.update { it.copy(nameError = R.string.required_field) }
            isValid = false
        } else {
            _customerFormState.update { it.copy(nameError = null) }
        }

        val demandDouble = form.demand.toDoubleOrNull()
        if (demandDouble == null || demandDouble < 0) { // Permintaan bisa 0
            _customerFormState.update { it.copy(demandError = R.string.invalid_number) } // Atau pesan error yang lebih spesifik
            isValid = false
        } else {
            _customerFormState.update { it.copy(demandError = null) }
        }

        if (form.selectedLocation == null || !form.selectedLocation.isValid()) {
            _customerFormState.update { it.copy(locationError = R.string.required_field) }
            isValid = false
        } else {
            _customerFormState.update { it.copy(locationError = null) }
        }
        return isValid
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            val result = customerRepository.deleteCustomer(customer)
            when (result) {
                is AppResult.Success -> {
                    Timber.i("Customer deleted successfully: ${customer.name}")
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageResId = R.string.customer_deleted_successfully))
                    loadAllCustomers() // Muat ulang daftar
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Error deleting customer: ${result.message}")
                    _uiEvent.emit(CustomerUiEvent.ShowSnackbar(messageResId = R.string.error_deleting_customer))
                }
            }
        }
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            _uiEvent.emit(CustomerUiEvent.RequestLocationPermission)
        }
    }
}

// UI State untuk daftar pelanggan
sealed interface CustomerListUiState {
    data object Loading : CustomerListUiState
    data class Success(val customers: List<CustomerEntity>) : CustomerListUiState
    data object Empty : CustomerListUiState
    data class Error(val messageResId: Int) : CustomerListUiState
}

// State untuk form input pelanggan
data class CustomerFormState(
    val id: Int? = null, // Hanya ada saat edit mode
    val name: String = "",
    val address: String = "",
    val demand: String = "", // Simpan sebagai String untuk input
    val selectedLocation: LatLng? = null,
    val notes: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: Int? = null,
    val demandError: Int? = null,
    val locationError: Int? = null
)

// Event UI sekali jalan
sealed interface CustomerUiEvent {
    data class ShowSnackbar(val messageResId: Int? = null, val messageText: String? = null) : CustomerUiEvent
    data object NavigateBack : CustomerUiEvent
    data object RequestLocationPermission : CustomerUiEvent
    // Tambahkan event lain jika perlu
}
