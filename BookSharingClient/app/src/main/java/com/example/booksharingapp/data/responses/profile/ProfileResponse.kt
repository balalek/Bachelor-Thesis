/**
 * File: ProfileResponse.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called ProfileResponse which represents a response for user's profile information API call.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

/**
 * This data class represents a response for user's profile information API call.
 */
data class ProfileResponse(
    val isSuccess: Boolean,
    val data: Profile
)
