/**
 * File: Data.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Data which contains some user's information, but it can be used just for a few of them.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents some user's information. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Data(
    @field:SerializedName("uziv_jmeno")
    val name: String,
    @field:SerializedName("email")
    val email: String,
    @field:SerializedName("profilovka")
    val profilePic: String?,
    @field:SerializedName("misto_predani")
    val placeHandover: String,
    @field:SerializedName("token")
    val token: String,
    val authToken: String?
)
