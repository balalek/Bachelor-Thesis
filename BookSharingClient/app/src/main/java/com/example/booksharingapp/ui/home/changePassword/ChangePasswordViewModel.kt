/**
 * File: ChangePasswordViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for changing logged-in user's password in ChangePasswordFragment.
 */

package com.example.booksharingapp.ui.home.changePassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for changing logged-in user's password in ChangePasswordFragment.
 * @param repository The combined repository to handle change password requests
 */
class ChangePasswordViewModel(
    private val repository: CombinedRepository
): BaseViewModel(repository){

    // MutableLiveData to hold change password response data
    private val _changePasswordResponse : MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val changePasswordResponse: LiveData<Resource<ApiResponse>>
        get() = _changePasswordResponse

    /**
     * This function handles the changing logged-inm user's password.
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun changePassword(
        password: String,
        newPassword: String
    ) = viewModelScope.launch {
        _changePasswordResponse.value = Resource.Loading
        _changePasswordResponse.value = repository.changePassword(password, newPassword)
    }

}