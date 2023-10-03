/**
 * File: Profile.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Profile which represents user's profile entity in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents User's profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Profile (
    @field:SerializedName("moje_knihy")
    val myBooks: MyBooks? = null,
    @field:SerializedName("moje_recenze")
    val myReviews: List<MyReviews>? = null,
    @field:SerializedName("moje_kontakty")
    val myContacts: MyContacts? = null
)