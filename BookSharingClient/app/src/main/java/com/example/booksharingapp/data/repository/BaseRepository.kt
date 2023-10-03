/**
 * File: BaseRepository.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/data/repository/BaseRepository.kt
 * Description: This file contains abstract repository class, that provides a base implementation of common functionality for all child repositories.
 */

package com.example.booksharingapp.data.repository

import com.example.booksharingapp.data.network.Resource
import com.example.booksharingapp.data.network.CombinedApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * This class provides a base implementation of common functionality for all repositories.
 * This includes safe API calls and logout functionality.
 */
abstract class BaseRepository {

    /**
     * This function calls an API method wrapped in a try-catch block to handle exceptions.
     * @param apiCall The API method to call
     * @return A Resource object representing the API (either successful or failure) response
     */
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ) : Resource<T> {
        return withContext(Dispatchers.IO){
            try {
                // Call the provided suspend function and return a Resource.Success object with the result
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when(throwable) {
                    is HttpException -> {
                        // Handle HttpExceptions by returning a Resource.Failure object with the Http status code and error body
                        Resource.Failure(false, throwable.code(), throwable.response()?.errorBody())
                    }
                    else -> {
                        // Handle all other exceptions by returning a Resource.Failure object with no additional details
                        Resource.Failure(true, null, null)
                    }
                }
            }
        }
    }

    /**
     * This function calls the API method to log out the user.
     * @param api The UserApi instance used to call the logout API method.
     * @return A Resource object representing the API response.
     */
    suspend fun logout(api: CombinedApi) = safeApiCall {
        api.logout()
    }

}