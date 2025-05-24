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

/**
 * ViewModel untuk SettingsScreen.
 * Mengelola state dan logika bisnis terkait pengaturan aplikasi.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val depotRepository: DepotRepository,
    private val vehicleRepository: VehicleRepository,
    private val customerRepository: CustomerRepository
    // Tambahkan repository lain jika ada pengaturan yang terkait dengannya
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
            var errorMessage: String? = null

            // Hapus data depot
            when (val depotResult = depotRepository.clearAllDepots()) {
                is AppResult.Success -> Timber.d("Depots cleared successfully.")
                is AppResult.Error -> {
                    allClearSuccess = false
                    errorMessage = depotResult.message ?: "Failed to clear depot data."
                    Timber.e(depotResult.exception, "Error clearing depot data: $errorMessage")
                }
            }

            // Hapus data kendaraan (jika depot berhasil atau tetap lanjut)
            if (allClearSuccess) {
                when (val vehicleResult = vehicleRepository.clearAllVehicles()) {
                    is AppResult.Success -> Timber.d("Vehicles cleared successfully.")
                    is AppResult.Error -> {
                        allClearSuccess = false
                        errorMessage = vehicleResult.message ?: "Failed to clear vehicle data."
                        Timber.e(vehicleResult.exception, "Error clearing vehicle data: $errorMessage")
                    }
                }
            }

            // Hapus data pelanggan (jika sebelumnya berhasil atau tetap lanjut)
            if (allClearSuccess) {
                when (val customerResult = customerRepository.clearAllCustomers()) {
                    is AppResult.Success -> Timber.d("Customers cleared successfully.")
                    is AppResult.Error -> {
                        allClearSuccess = false
                        errorMessage = customerResult.message ?: "Failed to clear customer data."
                        Timber.e(customerResult.exception, "Error clearing customer data: $errorMessage")
                    }
                }
            }

            _uiState.update { it.copy(isClearingData = false) }

            if (allClearSuccess) {
                Timber.i("All application data cleared successfully.")
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(messageResId = R.string.data_cleared_successfully))
                // Mungkin perlu memicu pembaruan di layar lain jika mereka menampilkan data ini
                // atau mengandalkan event untuk mereset state mereka.
            } else {
                Timber.e("Failed to clear all application data. Last error: $errorMessage")
                _uiEvent.emit(SettingsUiEvent.ShowSnackbar(messageText = errorMessage ?: context.getString(R.string.error_clearing_data)))
            }
        }
    }
}

data class SettingsUiState(
    val appVersion: String,
    val isClearingData: Boolean = false
    // Tambahkan state lain untuk pengaturan di masa mendatang
    // val selectedMapStyle: String = "Normal",
    // val selectedDistanceUnit: String = "km"
)

sealed interface SettingsUiEvent {
    data class ShowSnackbar(val messageResId: Int? = null, val messageText: String? = null) : SettingsUiEvent
    // Tambahkan event lain jika perlu, misalnya navigasi ke layar detail pengaturan
}

