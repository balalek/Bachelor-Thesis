/**
 * File: MyReviews.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyReviews which represents user's reviews entity in the system.
 * 				It is used to serialize and deserialize JSON data to and from the API requests and responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents User's reviews. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyReviews (
    @field:SerializedName("id_zaznamu")
    val recordId: Int? = null,
    @field:SerializedName("vytvoril")
    val createdBy : Int? = null,
    @field:SerializedName("uziv_jmeno")
    val userName : String? = null,
    @field:SerializedName("hodnoceni")
    val score : Int? = null,
    @field:SerializedName("datum_vytvoreni")
    val dateCreated : String? = null,
    @field:SerializedName("obsah")
    val content : String? = null,
    @field:SerializedName("profilovka")
    val profilePicture: String? = null
)
