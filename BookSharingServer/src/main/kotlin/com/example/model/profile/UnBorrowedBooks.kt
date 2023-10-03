/**
 * File: UnBorrowedBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called UnBorrowedBooks which represents an unborrowed books entity in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Unborrowed books. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class UnBorrowedBooks (
    @field:SerializedName("id_knihy")
    val bookId : Int? = null,
    @field:SerializedName("nazev")
    val bookName : String? = null
)
