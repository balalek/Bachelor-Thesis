/**
 * File: ReviewUser.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called ReviewUser which contains user's review to be stored on server.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses.notifications

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's review to be stored on server. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class ReviewUser(
    @field:SerializedName("id_zaznamu")
    val recordId: Int,
    @field:SerializedName("hodnoceni")
    val score : Int,
    @field:SerializedName("obsah")
    val content : String?
)
