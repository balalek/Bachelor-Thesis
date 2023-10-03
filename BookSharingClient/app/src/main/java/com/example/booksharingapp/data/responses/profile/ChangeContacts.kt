/**
 * File: ChangeContacts.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called ChangeContacts which contains user's contacts on his profile to be stored on server.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's contacts on his profile to be stored on server. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class ChangeContacts(
    @field:SerializedName("uziv_jmeno")
    val myUserName : String,
    @field:SerializedName("sekundarni_email")
    val secondEmail : String,
    @field:SerializedName("tel_cislo")
    val phoneNumber : Int?,
    @field:SerializedName("profilovka")
    val myProfilePicture: String?,
    @field:SerializedName("misto_predani")
    val handoverPlace: String?,
    @field:SerializedName("PSC")
    val psc : Int?
)
