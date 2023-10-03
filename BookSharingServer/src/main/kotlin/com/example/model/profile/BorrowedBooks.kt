/**
 * File: BorrowedBooks.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BorrowedBooks which represents a borrowed books entity in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model.profile

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Borrowed books. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BorrowedBooks (
    @field:SerializedName("id_knihy")
    val bookId : Int? = null,
    @field:SerializedName("nazev")
    val bookName : String? = null
)

