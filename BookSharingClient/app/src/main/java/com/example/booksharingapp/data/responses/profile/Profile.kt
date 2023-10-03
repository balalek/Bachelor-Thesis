/**
 * File: Profile.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Profile which contains user's profile information.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents user's profile information. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Profile (
    @field:SerializedName("moje_knihy")
    val myBooks: MyBooks,
    @field:SerializedName("moje_recenze")
    val myReviews: List<MyReviews>,
    @field:SerializedName("moje_kontakty")
    val myContacts: MyContacts
)