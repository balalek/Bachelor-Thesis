/**
 * File: MyReviews.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyReviews which contains user's reviews on his profile.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
/**
 * This data class represents user's reviews on his profile. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyReviews (
    @field:SerializedName("vytvoril")
    val createdBy : Int,
    @field:SerializedName("uziv_jmeno")
    val userName : String,
    @field:SerializedName("hodnoceni")
    val score : Int,
    @field:SerializedName("datum_vytvoreni")
    val dateCreated : String,
    @field:SerializedName("obsah")
    val content : String,
    @field:SerializedName("profilovka")
    val profilePicture: String
) : Parcelable