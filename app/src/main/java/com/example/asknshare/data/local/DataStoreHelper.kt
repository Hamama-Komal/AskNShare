package com.example.asknshare.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class DataStoreHelper(private val context: Context) {

    companion object {
        private val IS_REGISTERED_KEY = booleanPreferencesKey("is_registered")
    }

    // Save user registration status
    suspend fun saveUserRegistrationStatus(isRegistered: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_REGISTERED_KEY] = isRegistered
        }
    }

    // Retrieve user registration status
    val isUserRegistered: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_REGISTERED_KEY] ?: false
        }

    // Function to clear all stored preferences
    suspend fun clearDataStore() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

