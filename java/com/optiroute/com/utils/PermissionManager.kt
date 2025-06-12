package com.optiroute.com.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor() {

    companion object {
        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
        const val BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val PHONE_PERMISSION = Manifest.permission.CALL_PHONE
        const val NOTIFICATION_PERMISSION = "android.permission.POST_NOTIFICATIONS"
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun isLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isCoarseLocationPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            COARSE_LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isBackgroundLocationPermissionGranted(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                BACKGROUND_LOCATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }

    fun isCameraPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isPhonePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PHONE_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                NOTIFICATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }

    fun isStoragePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            LOCATION_PERMISSION,
            COARSE_LOCATION_PERMISSION,
            CAMERA_PERMISSION,
            PHONE_PERMISSION,
            READ_EXTERNAL_STORAGE
        )
    }

    fun getRequiredPermissionsForAPI33AndAbove(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                LOCATION_PERMISSION,
                COARSE_LOCATION_PERMISSION,
                CAMERA_PERMISSION,
                PHONE_PERMISSION,
                NOTIFICATION_PERMISSION
            )
        } else {
            getRequiredPermissions()
        }
    }

    data class PermissionStatus(
        val isLocationGranted: Boolean,
        val isCoarseLocationGranted: Boolean,
        val isBackgroundLocationGranted: Boolean,
        val isCameraGranted: Boolean,
        val isPhoneGranted: Boolean,
        val isNotificationGranted: Boolean,
        val isStorageGranted: Boolean
    ) {
        val allRequiredPermissionsGranted: Boolean
            get() = isLocationGranted && isCoarseLocationGranted && isCameraGranted &&
                    isPhoneGranted && isNotificationGranted
    }

    fun getPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            isLocationGranted = isLocationPermissionGranted(context),
            isCoarseLocationGranted = isCoarseLocationPermissionGranted(context),
            isBackgroundLocationGranted = isBackgroundLocationPermissionGranted(context),
            isCameraGranted = isCameraPermissionGranted(context),
            isPhoneGranted = isPhonePermissionGranted(context),
            isNotificationGranted = isNotificationPermissionGranted(context),
            isStorageGranted = isStoragePermissionGranted(context)
        )
    }
}