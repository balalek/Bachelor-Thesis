/**
 * File: AuthRepository.kt
 * Authors: Martin Baláž, Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/data/repository/AuthRepository.kt
 * Description: This file contains repository class, that it used for handling authentication-related API calls and user preferences.
 */

package com.example.booksharingapp.data.repository

import com.example.booksharingapp.data.UserPreferences
import com.example.booksharingapp.data.network.AuthApi
import com.example.booksharingapp.data.responses.LoginUser
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.responses.RegisterUser

/**
 * This is repository class for handling authentication-related API calls and user preferences. It extends BaseRepository class.
 * @param api An AuthApi instance for making API calls
 * @param preferences An UserPreferences instance for saving user authentication token in device
 */
class AuthRepository(
    private val api: AuthApi,
    private val preferences: UserPreferences
) : BaseRepository() {

    /**
     * This function makes an API call to log in the user with the given email, password and registration token.
     * @param email User's email address
     * @param password User's password
     * @param token Device registration token for push notifications
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun login(
        email: String,
        password: String,
        token: String
    ) = safeApiCall {
        val data = LoginUser(email, password, token)
        api.login(data)
    }

    /**
     * This function makes an API call to register a new user with the given email, password, username, phone number and date of birth.
     * @param email User's email address
     * @param password User's password
     * @param username User's desired username
     * @param phoneNumber User's phone number (optional)
     * @param dateOfBirth User's date of birth in the format yyyy-MM-dd
     * @return [Resource] object containing either [ApiResponse] or an error on failure
     */
    suspend fun register(
        email: String,
        password: String,
        username: String,
        phoneNumber: Int?,
        dateOfBirth: String
    ) = safeApiCall {
        val data = RegisterUser(email, password, username, phoneNumber, dateOfBirth)
        api.register(data)
    }

    /**
     * Saves the user authentication token to [UserPreferences].
     * @param token User's authentication token
     */
    suspend fun saveAuthToken(token: String) {
        preferences.saveAuthToken(token)
    }

}