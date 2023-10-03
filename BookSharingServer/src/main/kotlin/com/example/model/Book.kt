/**
 * File: Book.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called Book which represents a book entity in the system.
 * 				It is used to serialize and deserialize JSON data to and from the API requests and responses.
 */

package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Book. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class Book(
    @field:SerializedName("id_knihy")
    val bookId: Int? = null,
    @field:SerializedName("zapujcuje")
    val borrowedFrom: Int? = null,
    @field:SerializedName("je_pujcena")
    val borrowedTo: Int? = null,
    @field:SerializedName("nazev")
    val name: String? = null,
    @field:SerializedName("fotka")
    val picture: String? = null,
    @field:SerializedName("autor")
    val author: String? = null,
    @field:SerializedName("stav")
    val condition: String? = null,
    @field:SerializedName("popis")
    val info: String? = null,
    @field:SerializedName("cena")
    val price: Int? = null,
    @field:SerializedName("pristupnost")
    val accessibility: String? = null,
    @field:SerializedName("dostupnost")
    val availability: String? = null,
    @field:SerializedName("misto_predani")
    val place: String? = null,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int? = null,
    @field:SerializedName("datum_zapujceni")
    val borrowedDate: String? = null,
    @field:SerializedName("zanr")
    val genre: List<String>? = null,
    @field:SerializedName("moznosti_zapujceni")
    val option: List<String>? = null
)
