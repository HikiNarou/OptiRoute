package com.optiroute.com.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import java.util.UUID
import kotlin.random.Random

fun generateId(): String {
    return UUID.randomUUID().toString()
}

fun generateShortId(): String {
    return UUID.randomUUID().toString().substring(0, 8)
}

fun generateRandomColor(): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFF44336), // Red
        Color(0xFF00BCD4), // Cyan
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[Random.nextInt(colors.size)]
}

fun makePhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to make phone call", Toast.LENGTH_SHORT).show()
    }
}

fun openMapsNavigation(context: Context, latitude: Double, longitude: Double, label: String = "") {
    try {
        val uri = if (label.isNotEmpty()) {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        } else {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to browser
        try {
            val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open maps", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareText(context: Context, text: String, title: String = "Share") {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
    }
}

fun formatPhoneNumber(phoneNumber: String): String {
    val cleaned = phoneNumber.replace(Regex("[^\\d]"), "")
    return when {
        cleaned.startsWith("0") -> "+62${cleaned.substring(1)}"
        cleaned.startsWith("62") -> "+$cleaned"
        else -> "+62$cleaned"
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun calculateProgress(current: Int, total: Int): Float {
    return if (total > 0) current.toFloat() / total.toFloat() else 0f
}

fun formatFileSize(bytes: Long): String {
    val kilobyte = 1024.0
    val megabyte = kilobyte * 1024
    val gigabyte = megabyte * 1024

    return when {
        bytes < kilobyte -> "$bytes B"
        bytes < megabyte -> "${(bytes / kilobyte).toInt()} KB"
        bytes < gigabyte -> "${(bytes / megabyte).toInt()} MB"
        else -> "${(bytes / gigabyte).roundTo(2)} GB"
    }
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minute = 60 * 1000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    return when {
        diff < minute -> "Just now"
        diff < hour -> "${diff / minute}m ago"
        diff < day -> "${diff / hour}h ago"
        diff < week -> "${diff / day}d ago"
        diff < month -> "${diff / week}w ago"
        diff < year -> "${diff / month}mo ago"
        else -> "${diff / year}y ago"
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object Validator {
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult(false, "Email is required")
            !email.isValidEmail() -> ValidationResult(false, "Invalid email format")
            else -> ValidationResult(true)
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password is required")
            password.length < Constants.MIN_PASSWORD_LENGTH -> ValidationResult(false, "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters")
            else -> ValidationResult(true)
        }
    }

    fun validateUsername(username: String): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult(false, "Username is required")
            username.length > Constants.MAX_USERNAME_LENGTH -> ValidationResult(false, "Username too long")
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> ValidationResult(false, "Username can only contain letters, numbers, and underscores")
            else -> ValidationResult(true)
        }
    }

    fun validateFullName(fullName: String): ValidationResult {
        return when {
            fullName.isBlank() -> ValidationResult(false, "Full name is required")
            fullName.length > Constants.MAX_FULLNAME_LENGTH -> ValidationResult(false, "Full name too long")
            else -> ValidationResult(true)
        }
    }

    fun validatePhoneNumber(phoneNumber: String): ValidationResult {
        return when {
            phoneNumber.isBlank() -> ValidationResult(false, "Phone number is required")
            !phoneNumber.isValidPhoneNumber() -> ValidationResult(false, "Invalid phone number format")
            else -> ValidationResult(true)
        }
    }
}