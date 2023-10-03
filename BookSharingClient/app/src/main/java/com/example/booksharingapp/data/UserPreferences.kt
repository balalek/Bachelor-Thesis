/**
 * File: UserPreferences.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/data/UserPreferences.kt
 * Description: This file contains class called UserPreferences which manages the storage of a user's authentication token.
 */

package com.example.booksharingapp.data

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This class takes in a Context object in its constructor and uses it to create a DataStore object.
 * @param context The context in which this class is being instantiated
 */
class UserPreferences (
    context: Context
){
    // Store the application context to avoid leaking the activity context
    private val applicationContext = context.applicationContext
    // Create a DataStore object for storing key-value pairs
    private val dataStore: DataStore<Preferences>

    // Initialize the DataStore object in the init block with the name "my_data_store"
    init {
        dataStore = applicationContext.createDataStore(
            name = "my_data_store"
        )
    }

    // The authToken property returns the user's authentication token
    val authToken: Flow<String?>
    get() = dataStore.data.map { preferences ->
        preferences[KEY_AUTH]
    }

    // Save the authentication token to the DataStore
    suspend fun saveAuthToken(authToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH] = authToken
        }
    }

    // Clear all key-value pairs in the DataStore
    suspend fun clear(){
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // A companion object to hold the key for the authentication token
    companion object{
        private val KEY_AUTH = preferencesKey<String>("key_auth")
    }

}