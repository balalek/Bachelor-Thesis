/**
 * File: BookCardResponse.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookCardResponse which represents a response for book card information API call.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

/**
 * This data class represents a response for book card information API call.
 */
data class BookCardResponse(
    val data: List<BookCard>,
    val isSuccess: Boolean
)