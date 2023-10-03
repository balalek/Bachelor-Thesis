/**
 * File: Resource.kt
 * Author: Belal Khan
 * Original code: https://github.com/probelalkhan/android-login-signup-tutorial/blob/master/app/src/main/java/net/simplifiedcoding/data/network/Resource.kt
 * Description: This file contains sealed class, that is used to represent a network response wrapped with additional information such as success, failure or loading.
 */

package com.example.booksharingapp.data.network

import okhttp3.ResponseBody

/**
 * This sealed class is used to represent a network response wrapped with additional information such as success, failure or loading.
 */
sealed class Resource<out T> {

    // Represents a successful response along with the response data
    data class Success<out T>(val value: T) : Resource<T>()
    // Represents a failure response with additional information about the error
    data class Failure(
        val isNetworkError: Boolean,
        val errorCode: Int?,
        val errorBody: ResponseBody?
    ) : Resource<Nothing>()
    // Represents that the network call is currently in progress
    object Loading: Resource<Nothing>()

}