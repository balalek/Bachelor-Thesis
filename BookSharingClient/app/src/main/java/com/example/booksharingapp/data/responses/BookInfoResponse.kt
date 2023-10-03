/**
 * File: BookInfoResponse.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookInfoResponse which represents a response for book information API call.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

/**
 * This data class represents a response for book information API call.
 */
data class BookInfoResponse(
    val isSuccess: Boolean,
    val data: BookInfo
)
