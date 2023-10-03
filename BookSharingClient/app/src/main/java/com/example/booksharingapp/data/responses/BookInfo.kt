/**
 * File: BookInfo.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookInfo which contains all book information.
 * 				It is used to deserialize JSON data from the API responses.
 */

package com.example.booksharingapp.data.responses

import com.google.gson.annotations.SerializedName

/**
 * This data class represents all book information. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BookInfo(
    @field:SerializedName("zapujcuje")
    val borrowedFrom: Int,
    @field:SerializedName("je_pujcena")
    val borrowedTo: Int?,
    @field:SerializedName("nazev")
    val name: String,
    @field:SerializedName("fotka")
    val picture: String,
    @field:SerializedName("autor")
    val author: String,
    @field:SerializedName("stav")
    val condition: String,
    @field:SerializedName("popis")
    val info: String?,
    @field:SerializedName("cena")
    val price: Int,
    @field:SerializedName("pristupnost")
    val accessibility: String,
    @field:SerializedName("misto_predani")
    val place: String?,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int,
    @field:SerializedName("zanr")
    val genres: List<String>,
    @field:SerializedName("moznosti_zapujceni")
    val borrowOptions: List<String>,
    @field:SerializedName("uziv_jmeno")
    val userName: String,
    @field:SerializedName("profilovka")
    val profilePicture: String?,
    @field:SerializedName("hodnoceni")
    val score: Double?,
    @field:SerializedName("moje_kniha")
    val myBook: Boolean = false,
    @field:SerializedName("pozaduju")
    val requesting: Boolean = false,
    @field:SerializedName("pozaduju_oznameni")
    var requestingNotification: Boolean = false
)
