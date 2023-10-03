/**
 * File: Filter.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Filter which contains filter settings.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents filter settings. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Filter(
    @field:SerializedName("autor")
    val author: String?,
    @field:SerializedName("cena")
    val price: Int?,
    @field:SerializedName("dostupnost")
    val availability: MutableList<String>?,
    @field:SerializedName("moznosti_zapujceni")
    val borrow_options: MutableList<String>?,
    @field:SerializedName("zanr")
    val genre: MutableList<String>?
)



