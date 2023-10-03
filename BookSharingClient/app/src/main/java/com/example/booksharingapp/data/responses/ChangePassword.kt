/**
 * File: ChangePassword.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called ChangePassword which contains some user's old and new password.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's old and new password for password change. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class ChangePassword(
    @field:SerializedName("heslo")
    val password: String,
    @field:SerializedName("nove_heslo")
    val newPassword: String
)
