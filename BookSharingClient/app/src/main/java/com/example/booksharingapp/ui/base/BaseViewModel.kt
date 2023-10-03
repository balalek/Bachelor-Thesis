/**
 * File: BaseViewModel.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/ui/base/BaseViewModel.kt
 * Description: This file contains class, that provides a base implementation of ViewModel for all child ViewModels.
 */

package com.example.booksharingapp.ui.base

import androidx.lifecycle.ViewModel
import com.example.booksharingapp.data.network.CombinedApi
import com.example.booksharingapp.data.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This is base class for ViewModels to be extended from.
 * This class contains methods and variables that can be used across all view models
 * @param repository: A BaseRepository instance for handling API calls
 */
abstract class BaseViewModel (
    private val repository: BaseRepository
): ViewModel(){

    /**
     * This function is used to logout user from the application.
     * @param api: UserApi instance for handling the logout API call
     */
    suspend fun logout(api: CombinedApi) = withContext(Dispatchers.IO) {
        repository.logout(api)
    }

}