/**
 * File: LoginUser.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called LoginUser which contains user's login data.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's login data. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class LoginUser(
    @field:SerializedName("email")
    val email: String,
    @field:SerializedName("heslo")
    val password: String,
    @field:SerializedName("token")
    val token: String?
)
