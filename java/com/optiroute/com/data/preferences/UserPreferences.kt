package com.optiroute.com.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.optiroute.com.domain.models.User
import com.optiroute.com.domain.models.UserType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val USER_TYPE = stringPreferencesKey("user_type")
        private val IS_ACTIVE = booleanPreferencesKey("is_active")
        private val CREATED_AT = longPreferencesKey("created_at")
        private val UPDATED_AT = longPreferencesKey("updated_at")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val LOCATION_TRACKING_ENABLED = booleanPreferencesKey("location_tracking_enabled")
    }

    suspend fun saveCurrentUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USERNAME] = user.username
            preferences[EMAIL] = user.email
            preferences[FULL_NAME] = user.fullName
            preferences[USER_TYPE] = user.userType.name
            preferences[IS_ACTIVE] = user.isActive
            preferences[CREATED_AT] = user.createdAt
            preferences[UPDATED_AT] = user.updatedAt
            preferences[IS_LOGGED_IN] = true
        }
    }

    fun getCurrentUser(): Flow<User?> {
        return context.dataStore.data.map { preferences ->
            val isLoggedIn = preferences[IS_LOGGED_IN] ?: false
            if (isLoggedIn) {
                User(
                    id = preferences[USER_ID] ?: "",
                    username = preferences[USERNAME] ?: "",
                    email = preferences[EMAIL] ?: "",
                    fullName = preferences[FULL_NAME] ?: "",
                    userType = UserType.valueOf(preferences[USER_TYPE] ?: UserType.UMKM.name),
                    isActive = preferences[IS_ACTIVE] ?: true,
                    createdAt = preferences[CREATED_AT] ?: System.currentTimeMillis(),
                    updatedAt = preferences[UPDATED_AT] ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        }
    }

    suspend fun clearCurrentUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID)
            preferences.remove(USERNAME)
            preferences.remove(EMAIL)
            preferences.remove(FULL_NAME)
            preferences.remove(USER_TYPE)
            preferences.remove(IS_ACTIVE)
            preferences.remove(CREATED_AT)
            preferences.remove(UPDATED_AT)
            preferences[IS_LOGGED_IN] = false
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    }

    suspend fun saveThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode
        }
    }

    fun getThemeMode(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_MODE] ?: "SYSTEM"
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    fun getLanguage(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LANGUAGE] ?: "en"
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATION_ENABLED] ?: true
        }
    }

    suspend fun setLocationTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_TRACKING_ENABLED] = enabled
        }
    }

    fun isLocationTrackingEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[LOCATION_TRACKING_ENABLED] ?: true
        }
    }
}