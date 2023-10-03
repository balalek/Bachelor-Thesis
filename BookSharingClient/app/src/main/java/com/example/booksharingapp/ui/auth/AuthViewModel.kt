/**
 * File: AuthViewModel.kt
 * Authors: Martin Baláž, Belal Khan
 * Original Code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/ui/auth/AuthViewModel.kt
 * Description: This file contains ViewModel class for handling the authentication-related operations.
 */

package com.example.booksharingapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.AuthRepository
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is handling the authentication-related operations.
 * @param repository The authentication repository to handle authentication requests
 */
class AuthViewModel(
    private val repository: AuthRepository
) : BaseViewModel(repository) {

    // MutableLiveData to hold the login response data
    private val _loginResponse : MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val loginResponse: LiveData<Resource<ApiResponse>>
        get() = _loginResponse

    /**
     * This function handles the login operation.
     * @param email The email of the user
     * @param password The password of the user
     * @param token The registration token of device required for push notifications
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun login(
        email: String,
        password: String,
        token: String
    ) = viewModelScope.launch {
        _loginResponse.value = Resource.Loading
        _loginResponse.value = repository.login(email, password, token)
    }

    // MutableLiveData to hold the register response data
    private val _registerResponse : MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val registerResponse: LiveData<Resource<ApiResponse>>
        get() = _registerResponse

    /**
     * This function handles the register operation.
     * @param email The email of the user
     * @param password The password of the user
     * @param username The username of the user
     * @param phoneNumber The phone number of the user
     * @param dateOfBirth The date of birth of the user
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun register(
        email: String,
        password: String,
        username: String,
        phoneNumber: Int?,
        dateOfBirth: String
    ) = viewModelScope.launch {
        _registerResponse.value = Resource.Loading
        _registerResponse.value = repository.register(email, password, username, phoneNumber, dateOfBirth)
    }

    /**
     * This function saves the authentication token in the repository.
     * @param token: The token to be saved
     */
    suspend fun saveAuthToken(token: String) {
        repository.saveAuthToken(token)
    }

}