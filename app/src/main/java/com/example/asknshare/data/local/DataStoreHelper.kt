package com.example.asknshare.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.asknshare.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.DATASTORE_NAME)

class DataStoreHelper(private val context: Context) {

    companion object {
        private val IS_REGISTERED_KEY = booleanPreferencesKey(Constants.KEY_REGISTERED)
        private val EMAIL_KEY = stringPreferencesKey(Constants.KEY_EMAIL)
        private val USERNAME_KEY = stringPreferencesKey(Constants.KEY_USERNAME)
    }

    // Save user registration status
    suspend fun saveUserRegistrationStatus(isRegistered: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED_KEY] = isRegistered
        }
    }

    // Save user email
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = email
        }
    }

    // Save username
    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    // Retrieve user registration status
    val isUserRegistered: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_REGISTERED_KEY] ?: false
        }

    // Retrieve user email
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[EMAIL_KEY]
        }

    // Retrieve username
    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_KEY]
        }

    // Function to clear all stored preferences
    suspend fun clearDataStore() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

