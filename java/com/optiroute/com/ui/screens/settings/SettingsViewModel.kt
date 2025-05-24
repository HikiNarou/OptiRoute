package com.optiroute.com.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.BuildConfig
import com.optiroute.com.R
import com.optiroute.com.domain.model.AppResult
import com.optiroute.com.domain.repository.CustomerRepository
import com.optiroute.com.domain.repository.DepotRepository
import com.optiroute.com.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val depotRepository: DepotRepository,
    private val vehicleRepository: VehicleRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(appVersion = BuildConfig.VERSION_NAME))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.d("SettingsViewModel initialized")
    }

    fun onClearAllDataConfirmed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearingData = true) }
            Timber.i("Attempting to clear all application data.")

            var allClearSuccess = true
            var errorOccurred = false // Flag untuk menandai jika ada error
            var lastErrorMessage: String? = null // Menyimpan pesan error terakhir jika ada

            // Hapus data depot
            val depotResult = depotRepository.clearAllDepots()
            if (depotResult is AppResult.Error) {
                allClearSuccess = false
                errorOccurred = true
                lastErrorMessage = depotResult.message ?: "Gagal menghapus data depot."
                Timber.e(depotResult.exception, "Error clearing depot data: $lastErrorMessage")
            } else {
                Timber.d("Depots cleared successfully.")
            }

            // Hapus data kendaraan, hanya jika operasi sebelumnya berhasil
            if (allClearSuccess) {
                val vehicleResult = vehicleRepository.clearAllVehicles()
                if (vehicleResult is AppResult.Error) {
                    allClearSuccess = false
                    errorOccurred = true
                    lastErrorMessage = vehicleResult.message ?: "Gagal menghapus data kendaraan."
                    Timber.e(vehicleResult.exception, "Error clearing vehicle data: $lastErrorMessage")
                } else {
                    Timber.d("Vehicles cleared successfully.")
                }
            }

            // Hapus data pelanggan, hanya jika operasi sebelumnya berhasil
            if (allClearSuccess) {
                val customerResult = customerRepository.clearAllCustomers()
                if (customerResult is AppResult.Error) {
                    allClearSuccess = false
                    // errorOccurred = true; // Tidak perlu diset lagi jika sudah true
                    lastErrorMessage = customerResult.message ?: "Gagal menghapus data pelanggan."
                    Timber.e(customerResult.exception, "Error clearing customer data: $lastErrorMessage")
                } else {
                    Timber.d("Customers cleared successfully.")
                }
            }

            _uiState.update { it.copy(isClearingData = false) }

            if (allClearSuccess) {
                Timber.i("All application data cleared successfully.")
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(messageResId = R.string.data_cleared_successfully))
            } else {
                Timber.e("Failed to clear all application data. Last error: $lastErrorMessage")
                // Mengirim pesan error mentah atau resource ID jika ada pesan fallback yang lebih generik
                // PERBAIKAN: Mengirim messageText atau messageResId, bukan context.getString
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(messageText = lastErrorMessage ?: "Gagal membersihkan sebagian data."))
            }
        }
    }
}

data class SettingsUiState(
    val appVersion: String,
    val isClearingData: Boolean = false
)

sealed interface SettingsUiEvent {
    data class ShowSnackbar(val messageResId: Int? = null, val messageText: String? = null) : SettingsUiEvent
}
