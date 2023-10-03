/**
 * File: ApiResponse.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called ApiResponse which represents a response for API call.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

/**
 * This data class represents a response for API call.
 * This is mainly used for any responses that shouldn't contain any data (contains null).
 */
data class ApiResponse(
    val isSuccess: Boolean,
    val data: Data
)
