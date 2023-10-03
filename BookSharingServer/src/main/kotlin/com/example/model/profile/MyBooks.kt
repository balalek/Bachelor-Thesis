/**
 * File: MyBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called MyBooks which represents user's books entity in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents User's books. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class MyBooks (
    @field:SerializedName("volne_knihy")
    val unborrowedBooks: List<UnBorrowedBooks>? = null,
    @field:SerializedName("vypujcene_knihy")
    val borrowedBooks: List<BorrowedBooks>? = null,
    @field:SerializedName("knihy_k_navraceni")
    var booksToReturn: List<BooksToReturn>? = null
)
