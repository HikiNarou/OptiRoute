package com.optiroute.com.utils

import java.text.SimpleDateFormat
import java.util.*

fun Long.toDateString(pattern: String = "dd MMM yyyy HH:mm"): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        "Unknown Date"
    }
}

fun Long.toFormattedDuration(): String {
    val hours = this / (1000 * 60 * 60)
    val minutes = (this % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}

fun Double.toFormattedDistance(): String {
    return when {
        this < 1.0 -> "${(this * 1000).toInt()} m"
        this < 10.0 -> "${"%.1f".format(this)} km"
        else -> "${this.toInt()} km"
    }
}

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhoneNumber(): Boolean {
    return android.util.Patterns.PHONE.matcher(this).matches()
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") {
        it.lowercase().replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}

fun <T> List<T>.safe(index: Int): T? {
    return if (index in 0 until size) this[index] else null
}

fun <T> MutableList<T>.addIfNotExists(item: T): Boolean {
    return if (!contains(item)) {
        add(item)
        true
    } else false
}

fun <T> List<T>.chunkedSafely(size: Int): List<List<T>> {
    return if (size <= 0) listOf(this) else chunked(size)
}