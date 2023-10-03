/**
 * File: DeleteAccountViewModel.kt
 * Author: Martin Baláž
 * Description: This file contains ViewModel class for deleting logged-in user account in DeleteAccountFragment.
 */

package com.example.booksharingapp.ui.home.deleteAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.repository.CombinedRepository
import com.example.booksharingapp.data.responses.ApiResponse
import com.example.booksharingapp.ui.base.BaseViewModel
import kotlinx.coroutines.launch

/**
 * This class is used for deleting logged-in user account in DeleteAccountFragment.
 * @param repository The combined repository to handle delete account requests
 */
class DeleteAccountViewModel(
    private val repository: CombinedRepository
): BaseViewModel(repository){

    // MutableLiveData to hold account delete response data
    private val _account : MutableLiveData<Resource<ApiResponse>> = MutableLiveData()
    val account: LiveData<Resource<ApiResponse>>
        get() = _account

    /**
     * This function handles the deleting logged-in user's account.
     * @return A [LiveData] object containing a [Resource] with the result of the operation
     */
    fun deleteAccount() = viewModelScope.launch {
        _account.value = Resource.Loading
        _account.value = repository.deleteAccount()
    }

}