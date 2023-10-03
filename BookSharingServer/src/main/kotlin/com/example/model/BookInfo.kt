/**
 * File: BookInfo.kt
 * Author: Martin Baláž
 * Description: This file contains a data class called BookInfo which represents entity about book information in the system.
 * 				It is used to serialize JSON data to the API responses.
 */

package com.example.model

import com.google.gson.annotations.SerializedName

/**
 * This data class represents Book information. Properties have annotations, which specifies the name of the corresponding JSON field.
 */
data class BookInfo(
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
    @field:SerializedName("misto_predani")
    val place: String? = null,
    @field:SerializedName("max_delka_zapujceni")
    val maxBorrowedTime: Int? = null,
    @field:SerializedName("zanr")
    val genres: List<String?> = emptyList(),
    @field:SerializedName("moznosti_zapujceni")
    val borrowOptions: List<String?> = emptyList(),
    @field:SerializedName("uziv_jmeno")
    val userName: String? = null,
    @field:SerializedName("profilovka")
    val profilePicture: String? = null,
    @field:SerializedName("hodnoceni")
    val score: Double? = null,
    @field:SerializedName("moje_kniha")
    var myBook: Boolean? = false,
    @field:SerializedName("pozaduju")
    var requesting: Boolean? = false,
    @field:SerializedName("pozaduju_oznameni")
    var requestingNotification: Boolean? = false
)
