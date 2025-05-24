package com.optiroute.com.ui.screens.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.R
import com.optiroute.com.data.local.entity.VehicleEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.repository.VehicleRepository
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

// Batasan dari PDF
const val MAX_VEHICLES_ALLOWED = 10

/**
 * ViewModel untuk VehiclesScreen dan AddEditVehicleScreen.
 * Mengelola state dan logika bisnis terkait data kendaraan.
 */
@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    // State untuk daftar kendaraan
    private val _vehiclesListState = MutableStateFlow<VehicleListUiState>(VehicleListUiState.Loading)
    val vehiclesListState: StateFlow<VehicleListUiState> = _vehiclesListState.asStateFlow()

    // State untuk form tambah/ubah kendaraan
    private val _vehicleFormState = MutableStateFlow(VehicleFormState())
    val vehicleFormState: StateFlow<VehicleFormState> = _vehicleFormState.asStateFlow()

    // State untuk kendaraan yang sedang diedit (jika ada)
    private val _editingVehicle = MutableStateFlow<VehicleEntity?>(null)

    // SharedFlow untuk event UI sekali jalan
    private val _uiEvent = MutableSharedFlow<VehicleUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentVehicleCount = 0

    init {
        Timber.d("VehicleViewModel initialized")
        loadAllVehicles()
        observeVehicleCount()
    }

    private fun loadAllVehicles() {
        viewModelScope.launch {
            _vehiclesListState.value = VehicleListUiState.Loading
            vehicleRepository.getAllVehicles()
                .catch { e ->
                    Timber.e(e, "Error loading vehicles list")
                    _vehiclesListState.value = VehicleListUiState.Error(R.string.error_occurred)
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageResId = R.string.error_occurred))
                }
                .collectLatest { vehicles ->
                    Timber.i("Vehicles loaded: ${vehicles.size} items")
                    _vehiclesListState.value = if (vehicles.isEmpty()) {
                        VehicleListUiState.Empty
                    } else {
                        VehicleListUiState.Success(vehicles)
                    }
                }
        }
    }

    private fun observeVehicleCount() {
        viewModelScope.launch {
            vehicleRepository.getVehiclesCount().collectLatest { count ->
                currentVehicleCount = count
                // Jika perlu, bisa update state lain berdasarkan jumlah kendaraan
                Timber.d("Current vehicle count: $currentVehicleCount")
            }
        }
    }

    /**
     * Mempersiapkan form untuk menambah kendaraan baru.
     */
    fun prepareNewVehicleForm() {
        if (currentVehicleCount >= MAX_VEHICLES_ALLOWED) {
            viewModelScope.launch {
                _uiEvent.emit(VehicleUiEvent.ShowSnackbar(
                    messageResId = R.string.max_vehicles_reached_message,
                    args = arrayOf(MAX_VEHICLES_ALLOWED)
                ))
            }
            return
        }
        _editingVehicle.value = null
        _vehicleFormState.value = VehicleFormState(isEditMode = false)
        Timber.d("Prepared form for new vehicle.")
    }

    /**
     * Mempersiapkan form untuk mengedit kendaraan yang sudah ada.
     * @param vehicleId ID kendaraan yang akan diedit.
     */
    fun loadVehicleForEditing(vehicleId: Int) {
        viewModelScope.launch {
            vehicleRepository.getVehicleById(vehicleId).collectLatest { vehicleEntity ->
                if (vehicleEntity != null) {
                    _editingVehicle.value = vehicleEntity
                    _vehicleFormState.value = VehicleFormState(
                        id = vehicleEntity.id,
                        name = vehicleEntity.name,
                        capacity = vehicleEntity.capacity.toString(),
                        capacityUnit = vehicleEntity.capacityUnit,
                        notes = vehicleEntity.notes ?: "",
                        isEditMode = true
                    )
                    Timber.d("Loaded vehicle for editing: ${vehicleEntity.name}")
                } else {
                    Timber.w("Vehicle with ID $vehicleId not found for editing.")
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageResId = R.string.error_occurred))
                    _uiEvent.emit(VehicleUiEvent.NavigateBack) // Kembali jika kendaraan tidak ditemukan
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _vehicleFormState.update { it.copy(name = name, nameError = null) }
    }

    fun onCapacityChange(capacity: String) {
        _vehicleFormState.update { it.copy(capacity = capacity, capacityError = null) }
    }

    fun onCapacityUnitChange(unit: String) {
        _vehicleFormState.update { it.copy(capacityUnit = unit, capacityUnitError = null) }
    }

    fun onNotesChange(notes: String) {
        _vehicleFormState.update { it.copy(notes = notes) }
    }

    fun saveVehicle() {
        val currentForm = _vehicleFormState.value
        if (!validateForm(currentForm)) {
            return
        }

        // Cek batasan jumlah kendaraan saat menambah baru
        if (!currentForm.isEditMode && currentVehicleCount >= MAX_VEHICLES_ALLOWED) {
            viewModelScope.launch {
                _uiEvent.emit(VehicleUiEvent.ShowSnackbar(
                    messageResId = R.string.max_vehicles_reached_message,
                    args = arrayOf(MAX_VEHICLES_ALLOWED)
                ))
            }
            return
        }


        val vehicleEntity = VehicleEntity(
            id = if (currentForm.isEditMode) currentForm.id!! else 0,
            name = currentForm.name.trim(),
            capacity = currentForm.capacity.toDoubleOrNull() ?: 0.0, // Validasi sudah memastikan ini > 0
            capacityUnit = currentForm.capacityUnit.trim(),
            notes = currentForm.notes.trim().takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _vehicleFormState.update { it.copy(isSaving = true) }
            val result = if (currentForm.isEditMode) {
                vehicleRepository.updateVehicle(vehicleEntity)
            } else {
                vehicleRepository.addVehicle(vehicleEntity)
            }
            _vehicleFormState.update { it.copy(isSaving = false) }

            when (result) {
                is AppResult.Success -> {
                    val messageRes = if (currentForm.isEditMode) R.string.vehicle_updated_successfully else R.string.vehicle_added_successfully
                    Timber.i("Vehicle saved successfully. Mode: ${if (currentForm.isEditMode) "Edit" else "Add"}")
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageResId = messageRes))
                    _uiEvent.emit(VehicleUiEvent.NavigateBack)
                    loadAllVehicles() // Muat ulang daftar setelah berhasil
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Error saving vehicle: ${result.message}")
                    val errorMessage = result.message ?: "Gagal menyimpan kendaraan"
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageText = errorMessage))
                }
            }
        }
    }

    private fun validateForm(form: VehicleFormState): Boolean {
        var isValid = true
        if (form.name.isBlank()) {
            _vehicleFormState.update { it.copy(nameError = R.string.required_field) }
            isValid = false
        } else {
            _vehicleFormState.update { it.copy(nameError = null) }
        }

        val capacityDouble = form.capacity.toDoubleOrNull()
        if (capacityDouble == null || capacityDouble <= 0) {
            _vehicleFormState.update { it.copy(capacityError = R.string.value_must_be_positive) }
            isValid = false
        } else {
            _vehicleFormState.update { it.copy(capacityError = null) }
        }

        if (form.capacityUnit.isBlank()) {
            _vehicleFormState.update { it.copy(capacityUnitError = R.string.required_field) }
            isValid = false
        } else {
            _vehicleFormState.update { it.copy(capacityUnitError = null) }
        }

        return isValid
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            val result = vehicleRepository.deleteVehicle(vehicle)
            when (result) {
                is AppResult.Success -> {
                    Timber.i("Vehicle deleted successfully: ${vehicle.name}")
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageResId = R.string.vehicle_deleted_successfully))
                    loadAllVehicles() // Muat ulang daftar
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Error deleting vehicle: ${result.message}")
                    _uiEvent.emit(VehicleUiEvent.ShowSnackbar(messageResId = R.string.error_deleting_vehicle))
                }
            }
        }
    }
}

// UI State untuk daftar kendaraan
sealed interface VehicleListUiState {
    data object Loading : VehicleListUiState
    data class Success(val vehicles: List<VehicleEntity>) : VehicleListUiState
    data object Empty : VehicleListUiState
    data class Error(val messageResId: Int) : VehicleListUiState
}

// State untuk form input kendaraan
data class VehicleFormState(
    val id: Int? = null, // Hanya ada saat edit mode
    val name: String = "",
    val capacity: String = "", // Simpan sebagai String untuk input, konversi ke Double saat save
    val capacityUnit: String = "",
    val notes: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: Int? = null,
    val capacityError: Int? = null,
    val capacityUnitError: Int? = null
)

// Event UI sekali jalan
sealed interface VehicleUiEvent {
    data class ShowSnackbar(val messageResId: Int? = null, val messageText: String? = null, val args: Array<Any>? = null) : VehicleUiEvent {
        // Untuk kesetaraan jika args digunakan
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ShowSnackbar
            if (messageResId != other.messageResId) return false
            if (messageText != other.messageText) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false
            return true
        }
        override fun hashCode(): Int {
            var result = messageResId ?: 0
            result = 31 * result + (messageText?.hashCode() ?: 0)
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
    data object NavigateBack : VehicleUiEvent
    // Tambahkan event lain jika perlu
}
