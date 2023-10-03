/**
 * File: Book.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Book which contains book information to store on server.
 * 				It is used to serialize JSON data to the API requests.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents book information to be stored on server. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Book(
    @field:SerializedName("nazev")
    val name: String,
    @field:SerializedName("fotka")
    val picture: String,
    @field:SerializedName("autor")
    val author: String,
    @field:SerializedName("stav")
    val state: String,
    @field:SerializedName("popis")
    val info: String,
    @field:SerializedName("cena")
    val price: Int,
    @field:SerializedName("pristupnost")
    val accessibility: String,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int,
    @field:SerializedName("zanr")
    val genre: List<String>,
    @field:SerializedName("moznosti_zapujceni")
    val borrowOptions: List<String>,
    @field:SerializedName("misto_predani")
    val handoverPlace: String
)
