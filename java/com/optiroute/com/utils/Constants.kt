package com.optiroute.com.utils

object Constants {
    // Database
    const val DATABASE_NAME = "optiroute_database"
    const val DATABASE_VERSION = 2

    // Network
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Location and Map
    const val EARTH_RADIUS_KM = 6371.0
    const val DEFAULT_VEHICLE_SPEED_KMH = 50.0
    const val DEFAULT_MAP_ZOOM = 15f
    const val LOCATION_UPDATE_INTERVAL = 5000L // 5 seconds
    const val LOCATION_FASTEST_INTERVAL = 2000L // 2 seconds
    const val LOCATION_ACCURACY_THRESHOLD = 100f // meters

    // Route Optimization
    const val MAX_CUSTOMERS_PER_ROUTE = 20
    const val MAX_ROUTE_DISTANCE_KM = 100.0
    const val MAX_VEHICLE_CAPACITY_KG = 1000.0
    const val OPTIMIZATION_TIMEOUT_MS = 30000L // 30 seconds

    // Delivery
    const val DELIVERY_ARRIVAL_THRESHOLD_METERS = 50.0
    const val DELIVERY_TIMEOUT_HOURS = 24L
    const val PHOTO_QUALITY = 85
    const val MAX_PHOTO_SIZE_KB = 1024

    // Preferences Keys
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_LANGUAGE = "language"
    const val PREF_NOTIFICATIONS = "notifications_enabled"
    const val PREF_LOCATION_TRACKING = "location_tracking_enabled"

    // Theme Modes
    const val THEME_LIGHT = "LIGHT"
    const val THEME_DARK = "DARK"
    const val THEME_SYSTEM = "SYSTEM"

    // Languages
    const val LANG_ENGLISH = "en"
    const val LANG_INDONESIAN = "id"

    // File Paths
    const val DELIVERY_PHOTOS_DIR = "delivery_photos"
    const val ROUTE_CACHE_DIR = "route_cache"
    const val APP_CACHE_DIR = "optiroute_cache"

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_USERNAME_LENGTH = 50
    const val MAX_FULLNAME_LENGTH = 100
    const val MAX_ADDRESS_LENGTH = 255
    const val MAX_NOTES_LENGTH = 500

    // Request Codes
    const val REQUEST_LOCATION_PERMISSION = 1001
    const val REQUEST_CAMERA_PERMISSION = 1002
    const val REQUEST_PHONE_PERMISSION = 1003
    const val REQUEST_NOTIFICATION_PERMISSION = 1004
    const val REQUEST_STORAGE_PERMISSION = 1005

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "optiroute_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "OptiRoute Notifications"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Notifications for delivery updates and route changes"

    // Error Messages
    const val ERROR_NETWORK = "Network connection error"
    const val ERROR_TIMEOUT = "Operation timed out"
    const val ERROR_UNKNOWN = "An unknown error occurred"
    const val ERROR_PERMISSION_DENIED = "Permission denied"
    const val ERROR_LOCATION_UNAVAILABLE = "Location not available"
    const val ERROR_ROUTE_OPTIMIZATION_FAILED = "Route optimization failed"

    // Success Messages
    const val SUCCESS_LOGIN = "Login successful"
    const val SUCCESS_REGISTER = "Registration successful"
    const val SUCCESS_PROFILE_UPDATE = "Profile updated successfully"
    const val SUCCESS_PASSWORD_CHANGE = "Password changed successfully"
    const val SUCCESS_ROUTE_OPTIMIZED = "Route optimized successfully"
    const val SUCCESS_DELIVERY_COMPLETED = "Delivery completed successfully"

    // Algorithm Parameters
    const val CWS_ALPHA = 1.0
    const val CWS_BETA = 1.0
    const val CWS_GAMMA = 1.0
    const val ASTAR_HEURISTIC_WEIGHT = 1.0
    const val GENETIC_POPULATION_SIZE = 100
    const val GENETIC_GENERATIONS = 1000
    const val GENETIC_MUTATION_RATE = 0.1
    const val GENETIC_CROSSOVER_RATE = 0.8

    // UI Constants
    const val ANIMATION_DURATION_SHORT = 150L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    const val DEBOUNCE_DELAY = 300L
    const val SHIMMER_DURATION = 1000L

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40
    const val PREFETCH_DISTANCE = 5

    // Cache
    const val CACHE_SIZE_MB = 50L
    const val CACHE_MAX_AGE_HOURS = 24L
    const val IMAGE_CACHE_SIZE_MB = 100L

    // Security
    const val SESSION_TIMEOUT_MINUTES = 60L
    const val MAX_LOGIN_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MINUTES = 15L
}