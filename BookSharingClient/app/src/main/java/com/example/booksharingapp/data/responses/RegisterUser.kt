/**
 * File: RegisterUser.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called RegisterUser which contains user's registration data.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's registration data. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class RegisterUser(
    @field:SerializedName("email")
    val email: String,
    @field:SerializedName("heslo")
    val password: String,
    @field:SerializedName("uziv_jmeno")
    val name: String,
    @field:SerializedName("tel_cislo")
    val phoneNumber: Int?,
    @field:SerializedName("dat_narozeni")
    val birthday: String
)
