/**
 * File: BooksToReturn.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BooksToReturn which represents entity about books, which borrow time expired in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Books to return. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BooksToReturn(
    @field:SerializedName("id_knihy")
    val bookId : Int? = null,
    @field:SerializedName("nazev")
    val bookName : String? = null,
    @field:SerializedName("pocet_dni")
    val days : Int? = null
)
