package com.optiroute.com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.domain.models.DeliveryTracking
import com.optiroute.com.domain.models.DeliveryTrackingStatus
import com.optiroute.com.domain.repository.DeliveryTrackingRepository
import com.optiroute.com.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryTrackingViewModel @Inject constructor(
    private val deliveryTrackingRepository: DeliveryTrackingRepository
) : ViewModel() {

    private val _deliveryTrackings = MutableStateFlow<List<DeliveryTracking>>(emptyList())
    val deliveryTrackings: StateFlow<List<DeliveryTracking>> = _deliveryTrackings.asStateFlow()

    private val _trackingState = MutableStateFlow<TrackingState>(TrackingState.Initial)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    fun loadTrackingForUmkm(umkmId: String) {
        viewModelScope.launch {
            deliveryTrackingRepository.getDeliveryTrackingByCustomer(umkmId).collect { trackings ->
                _deliveryTrackings.value = trackings
            }
        }
    }

    fun loadTrackingForKurir(kurirId: String) {
        viewModelScope.launch {
            deliveryTrackingRepository.getDeliveryTrackingByKurir(kurirId).collect { trackings ->
                _deliveryTrackings.value = trackings
            }
        }
    }

    fun loadAllActiveDeliveries() {
        viewModelScope.launch {
            deliveryTrackingRepository.getActiveDeliveries().collect { trackings ->
                _deliveryTrackings.value = trackings
            }
        }
    }

    fun updateDeliveryStatus(trackingId: String, status: DeliveryTrackingStatus) {
        viewModelScope.launch {
            _trackingState.value = TrackingState.Loading

            when (val result = deliveryTrackingRepository.updateDeliveryStatus(trackingId, status)) {
                is Resource.Success -> {
                    _trackingState.value = TrackingState.Success("Status updated successfully")
                }
                is Resource.Error -> {
                    _trackingState.value = TrackingState.Error(result.message ?: "Failed to update status")
                }
                is Resource.Loading -> {
                    _trackingState.value = TrackingState.Loading
                }
            }
        }
    }

    fun clearState() {
        _trackingState.value = TrackingState.Initial
    }

    sealed class TrackingState {
        object Initial : TrackingState()
        object Loading : TrackingState()
        data class Success(val message: String) : TrackingState()
        data class Error(val message: String) : TrackingState()
    }
}