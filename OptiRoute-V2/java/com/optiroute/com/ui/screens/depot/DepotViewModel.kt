package com.optiroute.com.ui.screens.depot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.data.local.entity.DepotEntity
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.model.LatLng
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.R
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
 * ViewModel untuk DepotScreen.
 * Bertanggung jawab untuk mengambil, menyimpan, dan mengelola state terkait data depot.
 *
 * @param depotRepository Repository untuk mengakses data depot.
 */
@HiltViewModel
class DepotViewModel @Inject constructor(
    private val depotRepository: DepotRepository
) : ViewModel() {

    // State internal untuk data depot
    private val _depotState = MutableStateFlow<DepotUiState>(DepotUiState.Loading)
    val depotState: StateFlow<DepotUiState> = _depotState.asStateFlow()

    // State untuk input form
    private val _formState = MutableStateFlow(DepotFormState())
    val formState: StateFlow<DepotFormState> = _formState.asStateFlow()

    // SharedFlow untuk event UI sekali jalan (misalnya, pesan toast, navigasi)
    private val _uiEvent = MutableSharedFlow<DepotUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.d("DepotViewModel initialized")
        loadDepot()
    }

    private fun loadDepot() {
        viewModelScope.launch {
            _depotState.value = DepotUiState.Loading
            depotRepository.getDepot()
                .catch { e ->
                    Timber.e(e, "Error loading depot")
                    _depotState.value = DepotUiState.Error(R.string.error_occurred)
                    _uiEvent.emit(DepotUiEvent.ShowSnackbar(R.string.error_occurred))
                }
                .collectLatest { depotEntity ->
                    if (depotEntity != null) {
                        Timber.i("Depot loaded: ${depotEntity.name}")
                        _depotState.value = DepotUiState.Success(depotEntity)
                        _formState.update {
                            it.copy(
                                name = depotEntity.name,
                                address = depotEntity.address ?: "",
                                notes = depotEntity.notes ?: "",
                                selectedLocation = depotEntity.location
                            )
                        }
                    } else {
                        Timber.i("No depot found, presenting empty state.")
                        _depotState.value = DepotUiState.Empty
                        _formState.value = DepotFormState() // Reset form
                    }
                }
        }
    }

    fun onNameChange(name: String) {
        _formState.update { it.copy(name = name, nameError = null) }
    }

    fun onAddressChange(address: String) {
        _formState.update { it.copy(address = address) }
    }

    fun onNotesChange(notes: String) {
        _formState.update { it.copy(notes = notes) }
    }

    fun onLocationSelected(latLng: LatLng) {
        Timber.d("Location selected/updated: $latLng")
        _formState.update { it.copy(selectedLocation = latLng, locationError = null) }
    }

    fun saveDepot() {
        val currentForm = _formState.value
        if (!validateForm(currentForm)) {
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            val result = depotRepository.saveDepot(
                name = currentForm.name.trim(),
                location = currentForm.selectedLocation!!, // Validasi memastikan ini tidak null
                address = currentForm.address.trim().takeIf { it.isNotBlank() },
                notes = currentForm.notes.trim().takeIf { it.isNotBlank() }
            )
            _formState.update { it.copy(isSaving = false) }

            when (result) {
                is AppResult.Success -> {
                    Timber.i("Depot saved successfully.")
                    _uiEvent.emit(DepotUiEvent.ShowSnackbar(R.string.depot_updated_successfully))
                    // loadDepot() akan dipanggil otomatis oleh Flow jika data berubah,
                    // tapi jika ingin memastikan UI state diperbarui segera:
                    if (_depotState.value is DepotUiState.Empty) { // Jika ini adalah depot pertama
                        loadDepot() // Muat ulang untuk mengubah state dari Empty ke Success
                    }
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Error saving depot: ${result.message}")
                    val errorMessage = result.message ?: "Gagal menyimpan depot"
                    _uiEvent.emit(DepotUiEvent.ShowSnackbar(messageText = errorMessage))
                }
            }
        }
    }

    private fun validateForm(form: DepotFormState): Boolean {
        var isValid = true
        if (form.name.isBlank()) {
            _formState.update { it.copy(nameError = R.string.required_field) }
            isValid = false
        } else {
            _formState.update { it.copy(nameError = null) }
        }

        if (form.selectedLocation == null || !form.selectedLocation.isValid()) {
            _formState.update { it.copy(locationError = R.string.required_field) } // Atau pesan error yang lebih spesifik
            isValid = false
        } else {
            _formState.update { it.copy(locationError = null) }
        }
        return isValid
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            _uiEvent.emit(DepotUiEvent.RequestLocationPermission)
        }
    }
}

// Sealed interface untuk UI State Depot
sealed interface DepotUiState {
    data object Loading : DepotUiState
    data class Success(val depot: DepotEntity) : DepotUiState
    data object Empty : DepotUiState // Tidak ada depot yang diatur
    data class Error(val messageResId: Int) : DepotUiState
}

// Data class untuk state form input
data class DepotFormState(
    val name: String = "",
    val address: String = "",
    val notes: String = "",
    val selectedLocation: LatLng? = null,
    val isSaving: Boolean = false,
    val nameError: Int? = null, // Resource ID untuk pesan error
    val locationError: Int? = null // Resource ID untuk pesan error
)

// Sealed interface untuk event UI sekali jalan
sealed interface DepotUiEvent {
    data class ShowSnackbar(val messageResId: Int? = null, val messageText: String? = null) : DepotUiEvent
    data object RequestLocationPermission : DepotUiEvent
    // Tambahkan event lain jika perlu, misalnya navigasi
}
