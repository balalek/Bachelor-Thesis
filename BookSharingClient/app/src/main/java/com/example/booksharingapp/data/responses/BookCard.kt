/**
 * File: BookCard.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookCard which contains information to book card.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents book card information. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BookCard(
    @field:SerializedName("autor")
    val author: String,
    @field:SerializedName("cena")
    val price: Int,
    @field:SerializedName("datum_zapujceni")
    val borrowedDate: String?,
    @field:SerializedName("fotka")
    val picture: String,
    @field:SerializedName("hodnoceni")
    val score: Double,
    @field:SerializedName("id_knihy")
    val bookId: Int,
    @field:SerializedName("id_uzivatele")
    val userId: Int,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int,
    @field:SerializedName("nazev")
    val name: String,
    @field:SerializedName("uziv_jmeno")
    val username: String
)