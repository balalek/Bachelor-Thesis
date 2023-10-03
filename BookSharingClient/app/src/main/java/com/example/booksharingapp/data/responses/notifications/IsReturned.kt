/**
 * File: IsReturned.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called IsReturned which contains user's response for return book notification.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses.notifications

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's response for return book notification. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class IsReturned(
    @field:SerializedName("vraceno_drive")
    val isReturned : Boolean,
    @field:SerializedName("id_zaznamu")
    val recordId: Int? = null
)
