/**
 * File: BookCard.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookCard which represents a book card entity in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Book card. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BookCard(
    @field:SerializedName("id_knihy")
    val bookId: Int? = null,
    @field:SerializedName("nazev")
    val name: String? = null,
    @field:SerializedName("fotka")
    val picture: String? = null,
    @field:SerializedName("autor")
    val author: String? = null,
    @field:SerializedName("cena")
    val price: Int? = null,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int? = null,
    @field:SerializedName("datum_zapujceni")
    val borrowedDate: String? = null,
    @field:SerializedName("uziv_jmeno")
    val username: String? = null,
    @field:SerializedName("hodnoceni")
    val score: Double? = null
)
