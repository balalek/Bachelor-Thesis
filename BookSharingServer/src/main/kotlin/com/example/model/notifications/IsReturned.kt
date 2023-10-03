/**
 * File: IsReturned.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called IsReturned which represents entity about book return state in the system.
 * 				It is used to deserialize JSON data from the API requests.
 */

package com.example.model.notifications

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Book return state. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class IsReturned(
    @field:SerializedName("vraceno_drive")
    val isReturned : Boolean,
    @field:SerializedName("id_zaznamu")
    val recordId: Int
)
