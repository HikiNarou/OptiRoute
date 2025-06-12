package com.optiroute.com.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREF_NAME = "optiroute_preferences"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_ACTIVE = "is_active"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_UPDATED_AT = "updated_at"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_LOCATION_TRACKING = "location_tracking"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            val user = User(
                id = sharedPreferences.getString(KEY_USER_ID, "") ?: "",
                username = sharedPreferences.getString(KEY_USERNAME, "") ?: "",
                email = sharedPreferences.getString(KEY_EMAIL, "") ?: "",
                fullName = sharedPreferences.getString(KEY_FULL_NAME, "") ?: "",
                userType = UserType.valueOf(
                    sharedPreferences.getString(KEY_USER_TYPE, UserType.UMKM.name) ?: UserType.UMKM.name
                ),
                isActive = sharedPreferences.getBoolean(KEY_IS_ACTIVE, true),
                createdAt = sharedPreferences.getLong(KEY_CREATED_AT, System.currentTimeMillis()),
                updatedAt = sharedPreferences.getLong(KEY_UPDATED_AT, System.currentTimeMillis())
            )
            _currentUser.value = user
        }
    }

    suspend fun saveCurrentUser(user: User) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_FULL_NAME, user.fullName)
            putString(KEY_USER_TYPE, user.userType.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_ACTIVE, user.isActive)
            putLong(KEY_CREATED_AT, user.createdAt)
            putLong(KEY_UPDATED_AT, user.updatedAt)
            apply()
        }
        _currentUser.value = user
    }

    suspend fun clearCurrentUser() {
        sharedPreferences.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USERNAME)
            remove(KEY_EMAIL)
            remove(KEY_FULL_NAME)
            remove(KEY_USER_TYPE)
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_IS_ACTIVE)
            remove(KEY_CREATED_AT)
            remove(KEY_UPDATED_AT)
            apply()
        }
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUserId(): String? {
        return if (isLoggedIn()) {
            sharedPreferences.getString(KEY_USER_ID, null)
        } else null
    }

    fun getCurrentUserType(): UserType? {
        return if (isLoggedIn()) {
            val userTypeString = sharedPreferences.getString(KEY_USER_TYPE, null)
            userTypeString?.let { UserType.valueOf(it) }
        } else null
    }

    fun saveThemeMode(themeMode: String) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, themeMode).apply()
    }

    fun getThemeMode(): String {
        return sharedPreferences.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    }

    fun saveLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    fun setLocationTrackingEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_LOCATION_TRACKING, enabled).apply()
    }

    fun isLocationTrackingEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_LOCATION_TRACKING, true)
    }
}