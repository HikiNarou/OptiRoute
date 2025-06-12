package com.optiroute.com.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optiroute.com.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionSettingsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _permissionStatus = MutableStateFlow(
        PermissionManager.PermissionStatus(
            isLocationGranted = false,
            isCoarseLocationGranted = false,
            isBackgroundLocationGranted = false,
            isCameraGranted = false,
            isPhoneGranted = false,
            isNotificationGranted = false,
            isStorageGranted = false
        )
    )
    val permissionStatus: StateFlow<PermissionManager.PermissionStatus> = _permissionStatus.asStateFlow()

    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            _permissionStatus.value = permissionManager.getPermissionStatus(context)
        }
    }
}