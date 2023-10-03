/**
 * File: IsConfirmed.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called IsConfirmed which represents entity about book borrow state in the system.
 * 				It is used to deserialize JSON data from the API requests.
 */

package com.example.model.notifications

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Book borrow state. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class IsConfirmed(
    @field:SerializedName("potvrzeno")
    val isConfirmed : Boolean,
    @field:SerializedName("id_zaznamu")
    val recordId: Int
)
